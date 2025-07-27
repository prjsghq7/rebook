const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

$loginForm = document.getElementById('loginForm');

$loginForm.addEventListener('submit', (e) => {
    e.preventDefault();

    if ($loginForm['email'].validity.valueMissing) {
        dialog.show({
            title: '로그인',
            content: '이메일을 입력해 주세요.',
            buttons: [
                {
                    caption: '확인',
                    color: 'blue',
                    onClickCallback: ($modal) => {
                        dialog.hide($modal);
                        $loginForm['email'].focus();
                    }
                }
            ]
        });
        return;
    }
    if (!$loginForm['email'].validity.valid) {
        dialog.show({
            title: '로그인',
            content: '올바른 이메일을 입력해 주세요.',
            buttons: [
                {
                    caption: '확인',
                    color: 'blue',
                    onClickCallback: ($modal) => {
                        dialog.hide($modal);
                        $loginForm['email'].focus();
                        $loginForm['email'].select();
                    }
                }
            ]
        });
        return;
    }

    if ($loginForm['password'].validity.valueMissing) {
        dialog.show({
            title: '로그인',
            content: '비밀번호를 입력해 주세요.',
            buttons: [
                {
                    caption: '확인',
                    color: 'blue',
                    onClickCallback: ($modal) => {
                        dialog.hide($modal);
                        $loginForm['password'].focus();
                    }
                }
            ]
        });
        return;
    }
    if (!$loginForm['password'].validity.valid) {
        dialog.show({
            title: '로그인',
            content: '올바른 비밀번호를 입력해 주세요.',
            buttons: [
                {
                    caption: '확인',
                    color: 'blue',
                    onClickCallback: ($modal) => {
                        dialog.hide($modal);
                        $loginForm['password'].focus();
                        $loginForm['password'].select();
                    }
                }
            ]
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $loginForm['email'].value);
    formData.append('password', $loginForm['password'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('로그인', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_suspended':
                dialog.showSimpleOk('로그인', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'success':
                if ($loginForm['remember'].checked) {
                    localStorage.setItem('loginEmail', $loginForm['email'].value)
                } else {
                    localStorage.removeItem('loginEmail')
                }
                location.href = `${origin}/`
                break;
            default:
                dialog.showSimpleOk('로그인', '이메일 혹은 비밀번호가 올바르지 않습니다.\n다시 확인해 주세요.')
        }
    };
    xhr.open('POST', '/user/login');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
    loading.show('로그인 시도 중');
});

$loginForm['email'].value = localStorage.getItem('loginEmail') ?? '';
$loginForm['remember'].checked = localStorage.getItem('loginEmail') ?? '';