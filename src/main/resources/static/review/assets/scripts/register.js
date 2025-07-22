import { Scope } from './scope.js';

const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $reviewRegisterForm = document.getElementById('reviewRegisterForm');

window.scope = new Scope($reviewRegisterForm.querySelector('.scope-area'));

$reviewRegisterForm.addEventListener('submit', (e) => {
    e.preventDefault();

    if ($reviewRegisterForm['scope'].value < 1
        || $reviewRegisterForm['scope'].value > 5) {
        dialog.showSimpleOk('리뷰 등록', '리뷰 점수로는 1점 이상 5점 이하로 입력해주세요.');
        return;
    }
    if ($reviewRegisterForm['comment'].validity.valueMissing) {
        dialog.showSimpleOk('리뷰 등록', '리뷰 코멘트를 입력해주세요.');
        return;
    }
    if ($reviewRegisterForm['comment'].validity.tooLong) {
        dialog.showSimpleOk('리뷰 등록', '리뷰 코멘트는 100자 이내로 작성해 주세요.');
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    const bookId = new URL(location.href).searchParams.get('id');
    formData.append('bookId', bookId);
    formData.append('scope', $reviewRegisterForm['scope'].value);
    formData.append('comment', $reviewRegisterForm['comment'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('리뷰 등록', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요.`);
            return;
        }

        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_duplicate':
                dialog.showSimpleOk('리뷰 등록', '책 한권당 리뷰는 한번만 등록 가능합니다.\n기존 리뷰를 삭제하거나 수정하여 사용해주세요.');
                break;
            case 'failure_session_expired':
                dialog.showSimpleOk('리뷰 등록', '세션이 만료되었습니다.\n로그인 후 리뷰 등록 해주세요.');
                break;
            case 'failure_suspended':
                dialog.showSimpleOk('리뷰 등록', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'success':
                dialog.show({
                    title: '리뷰 등록',
                    content: '리뷰가 성공적으로 작성되었습니다.',
                    buttons: [
                        {
                            caption: '확인',
                            color: 'blue',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                                window.close();
                            }
                        }
                    ]
                });
                break;
            default:
                dialog.showSimpleOk('리뷰 등록', '리뷰작성에 실패하였습니다.\n다시 확인해 주세요.')
        }
    };
    xhr.open('POST', '/review/register');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
});

$reviewRegisterForm['comment'].addEventListener('input', () => {
    const length = $reviewRegisterForm['comment'].value.length;
    const $count = $reviewRegisterForm.querySelector('.count');
    $count.textContent = length +'/100';
});
