const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $userProvider = document.getElementById('userProvider');
const $userGender = document.getElementById('userGender');
const $userRegister = document.getElementById('userRegister');
const $reviewRegister = document.getElementById('reviewRegister');

const loadDashBoard = () => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('관리자 통계', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        console.log(response);
    };
    xhr.open('GET', '/admin/dashboard/all');
    xhr.send(formData);
};

loadDashBoard();