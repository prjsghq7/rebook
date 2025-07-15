import { Scope } from './scope.js';

const $reviewRegisterForm = document.getElementById('reviewRegisterForm');

window.scope = new Scope($reviewRegisterForm.querySelector('.scope-area'));

$reviewRegisterForm.addEventListener('submit', () => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    const bookId = new URL(location.href).searchParams.get('id');
    formData.append('id', bookId);
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

    };
    xhr.open('POST', '/review/register');
    xhr.send(formData);
});
