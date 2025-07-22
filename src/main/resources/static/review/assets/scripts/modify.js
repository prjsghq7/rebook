import { Scope } from './scope.js';

const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $reviewModifyForm = document.getElementById('reviewModifyForm');

window.scope = new Scope($reviewModifyForm.querySelector('.scope-area'));

$reviewModifyForm.addEventListener('submit', (e) => {
    e.preventDefault();

    if ($reviewModifyForm['scope'].value < 1
        || $reviewModifyForm['scope'].value > 5) {
        dialog.showSimpleOk('리뷰 등록', '리뷰 점수로는 1점 이상 5점 이하로 입력해주세요.');
        return;
    }
    if ($reviewModifyForm['comment'].validity.valueMissing) {
        dialog.showSimpleOk('리뷰 등록', '리뷰 코멘트를 입력해주세요.');
        return;
    }
    if ($reviewModifyForm['comment'].validity.tooLong) {
        dialog.showSimpleOk('리뷰 등록', '리뷰 코멘트는 100자 이내로 작성해 주세요.');
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('id', $reviewModifyForm['id'].value);
    formData.append('scope', $reviewModifyForm['scope'].value);
    formData.append('comment', $reviewModifyForm['comment'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('리뷰 수정', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요.`);
            return;
        }

        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_no_permission':
                dialog.showSimpleOk('리뷰 수정', '해당 리뷰글에 대한 삭제 권한이 없습니다.');
                break;
            case 'failure_absent':
                dialog.showSimpleOk('리뷰 수정', '해당 리뷰글이 이미 삭제되었거나 존재 하지 않습니다.');
                break;
            case 'failure_session_expired':
                dialog.showSimpleOk('리뷰 수정', '세션이 만료되었습니다.\n로그인 후 리뷰 수정 해주세요.');
                break;
            case 'failure_suspended':
                dialog.showSimpleOk('리뷰 수정', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'success':
                dialog.show({
                    title: '리뷰 수정',
                    content: '리뷰가 성공적으로 수정되었습니다.',
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
                dialog.showSimpleOk('리뷰 등록', '리뷰수정에 실패하였습니다.\n다시 확인해 주세요.')
        }
    };
    xhr.open('PATCH', '/review/modify');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
});

const updateTextCount = () => {
    const length = $reviewModifyForm['comment'].value.length;
    const $count = $reviewModifyForm.querySelector('.count');
    $count.textContent = length +'/100';
}
$reviewModifyForm['comment'].addEventListener('input', updateTextCount);
updateTextCount();