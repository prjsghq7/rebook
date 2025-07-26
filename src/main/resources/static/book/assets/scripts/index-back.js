// 뒤로가기 버튼 이벤트 리스너 추가
const $verificationArea = document.getElementById('verification-area');
$verificationArea.querySelector(`:scope > .button-container > .back`).addEventListener('click', () => {
    history.back();
});