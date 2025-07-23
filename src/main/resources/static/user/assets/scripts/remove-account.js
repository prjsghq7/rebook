const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $deleteForm = document.getElementById('deleteForm');

$deleteForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const $emailLabel = $deleteForm.querySelector('.-object-label:has(input[name="email"])');
    const $agreeLabel = $deleteForm.querySelector('.-object-label:has(input[name="agreeRemoveAccount"])');

    const $labels = [$emailLabel, $agreeLabel];
    $labels.forEach(($label) => {
        $label.setInValid(false);
    });

    if (!$deleteForm['emailCodeSendButton'].hasAttribute('disabled')
        || !$deleteForm['emailCodeVerifyButton'].hasAttribute('disabled')) {
        $emailLabel.setInValid(true, '이메일 인증을 완료해 주세요.');
    }

    if (!$deleteForm['agreeRemoveAccount'].checked) {
        $agreeLabel.setInValid(true, '회원 탈퇴에 동의해 주세요.');
    }

    if ($labels.some($label => $label.isInValid())) {
        return false;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $deleteForm['email'].value);
    formData.append('code', $deleteForm['emailCode'].value);
    formData.append('salt', $deleteForm['emailSalt'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('회원 탈퇴', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_no_permission':
                dialog.showSimpleOk('회원 탈퇴', '현재 로그인된 계정과 이메일이 일치하지 않습니다.');
                break;
            case 'failure_session_expired':
                dialog.showSimpleOk('회원 탈퇴', '세션이 만료되었습니다.\n로그인 후 재시도 해주세요.');
                break;
            case 'failure_suspended':
                dialog.showSimpleOk('회원 탈퇴', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'success':
                dialog.show({
                    title: '회원 탈퇴',
                    content: '회원 탈퇴가 완료되었습니다.\n그 동안 ReBook 서비스를 이용해주셔서 감사합니다.',
                    buttons: [
                        {
                            caption: '확인',
                            color: 'blue',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                                location.href = `${origin}/`
                            }
                        }
                    ]
                });
                break;
            default:
                dialog.showSimpleOk('회원 탈퇴', '알 수 없는 이유로 회원 탈퇴에 실패하였습니다.\n잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('DELETE', '/user/remove-account');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
});

$deleteForm['emailCodeSendButton'].addEventListener('click', () => {
    const $emailLabel = $deleteForm.querySelector('.-object-label:has(input[name="email"])');
    $emailLabel.setInValid(false);
    if ($deleteForm['email'].validity.valueMissing) {
        $emailLabel.setInValid(true, '이메일을 입력해 주세요.');
    } else if (!$deleteForm['email'].validity.valid) {
        $emailLabel.setInValid(true, '올바른 이메일을 입력해 주세요.');
    }
    if ($emailLabel.isInValid()) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $deleteForm['email'].value)
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('인증번호 전송', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }

        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_no_permission':
                dialog.showSimpleOk('인증번호 전송', '현재 로그인된 계정과 이메일이 일치하지 않습니다.');
                break;
            case 'failure_session_expired':
                dialog.showSimpleOk('인증번호 전송', '세션이 만료되었습니다.\n로그인 후 재시도 해주세요.');
                break;
            case 'failure_suspended':
                dialog.showSimpleOk('인증번호 전송', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'success':
                $deleteForm['emailSalt'].value = response.salt;
                $deleteForm['emailCodeSendButton'].setDisabled(true);
                $deleteForm['emailCode'].setDisabled(false);
                $deleteForm['emailCodeVerifyButton'].setDisabled(false);
                $deleteForm['emailCode'].focus();
                dialog.showSimpleOk('인증번호 전송', '입력하신 이메일로 인증번호를 전송하였습니다.\n해당 인증번호는 10분간 유효하니 유의해 주세요.');
                break;
            default:
                dialog.showSimpleOk('인증번호 전송', '알 수 없는 이유로 인증번호를 전송하지 못하였습니다.');
        }
    };
    xhr.open('POST', '/user/remove-account-email');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
});

$deleteForm['emailCodeVerifyButton'].addEventListener('click', () => {
    const $emailLabel = $deleteForm.querySelector('.-object-label:has(input[name="email"])');
    if ($deleteForm['emailCode'].validity.valueMissing) {
        $emailLabel.setInValid(true, '인증번호를 입력해주세요.');
    } else if (!$deleteForm['emailCode'].validity.valid) {
        $emailLabel.setInValid(true, '올바른 인증번호를 입력해 주세요.');
    }
    if ($emailLabel.isInValid()) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $deleteForm['email'].value);
    formData.append('code', $deleteForm['emailCode'].value);
    formData.append('salt', $deleteForm['emailSalt'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('인증번호 확인', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_expired':
                $deleteForm['emailSalt'].value = '';
                $deleteForm['emailCodeSendButton'].setDisabled(false);
                $deleteForm['emailCode'].value = '';
                $deleteForm['emailCode'].setDisabled(true);
                $deleteForm['emailCodeVerifyButton'].setDisabled(true);
                $deleteForm['email'].focus();
                dialog.showSimpleOk('인증번호 확인', '인증 정보가 만료되었습니다.\n이메일 인증을 다시 진행해 주세요.');
                break;
            case 'success':
                $deleteForm['emailCode'].setDisabled(true);
                $deleteForm['emailCodeVerifyButton'].setDisabled(true);
                dialog.showSimpleOk('인증번호 확인', '이메일이 정상적으로 인증되었습니다.');
                break;
            default:
                dialog.showSimpleOk('인증번호 확인', '인증번호가 올바르지 않습니다.\n다시 확인해 주세요.', () => {
                    $deleteForm['emailCode'].focus();
                    $deleteForm['emailCode'].select();
                });
        }
    };
    xhr.open('PATCH', '/user/remove-account-email');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
});