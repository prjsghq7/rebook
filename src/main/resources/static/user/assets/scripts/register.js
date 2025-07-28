const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $registerForm = document.getElementById('registerForm');
const $step = $registerForm.querySelector(':scope > .step');
const $stepItems = Array.from($step.querySelectorAll(':scope > .item'));
const $containers = Array.from($registerForm.querySelectorAll(':scope > .container'));

let currentStep = 0;

if (new URL(location.href).searchParams.get('registerType') === 'local') {
    $registerForm['emailCodeSendButton'].addEventListener('click', () => {
        const $emailLabel = $registerForm.querySelector('.-object-label:has(input[name="email"])');
        $emailLabel.setInValid(false);
        if ($registerForm['email'].validity.valueMissing) {
            $emailLabel.setInValid(true, '이메일을 입력해 주세요.');
        } else if (!$registerForm['email'].validity.valid) {
            $emailLabel.setInValid(true, '올바른 이메일을 입력해 주세요.');
        }
        if ($emailLabel.isInValid()) {
            return;
        }

        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('email', $registerForm['email'].value)
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            loading.hide();
            if (xhr.status < 200 || xhr.status >= 300) {
                dialog.showSimpleOk('인증번호 전송', '요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.');
                return;
            }
            const response = JSON.parse(xhr.responseText);
            switch (response.result) {
                case 'failure_duplicate':
                    dialog.showSimpleOk('인증번호 전송', `입력하신 이메일 [${$registerForm['email'].value}]은/는 이미 사용 중입니다.`, {
                        onOkCallback: () => {
                            $registerForm['email'].focus();
                            $registerForm['email'].select();
                        }
                    });
                    break;
                case 'success':
                    $registerForm['emailSalt'].value = response.salt;
                    $registerForm['email'].setDisabled(true);
                    $registerForm['emailCodeSendButton'].setDisabled(true);
                    $registerForm['emailCode'].setDisabled(false);
                    $registerForm['emailCodeVerifyButton'].setDisabled(false);
                    $registerForm['emailCode'].focus();
                    dialog.showSimpleOk('인증번호 전송', '입력하신 이메일로 인증번호를 전송하였습니다.\n해당 인증번호는 10분간 유효하니 유의해 주세요.');
                    break;
                default:
                    dialog.showSimpleOk('인증번호 전송', '알 수 없는 이유로 인증번호를 전송하지 못하였습니다.');
            }
        };
        xhr.open('POST', '/user/register-email');
        xhr.setRequestHeader(header, token);
        xhr.send(formData);
        loading.show('인증번호 전송 중');
    });

    $registerForm['emailCodeVerifyButton'].addEventListener('click', () => {
        const $emailLabel = $registerForm.querySelector('.-object-label:has(input[name="email"])');
        $emailLabel.setInValid(false);
        if ($registerForm['emailCode'].validity.valueMissing) {
            $emailLabel.setInValid(true, '인증번호를 입력해주세요.');
        } else if (!$registerForm['emailCode'].validity.valid) {
            $emailLabel.setInValid(true, '올바른 인증번호를 입력해 주세요.');
        }
        if ($emailLabel.isInValid()) {
            return;
        }

        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('email', $registerForm['email'].value);
        formData.append('code', $registerForm['emailCode'].value);
        formData.append('salt', $registerForm['emailSalt'].value);
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            loading.hide();
            if (xhr.status < 200 || xhr.status >= 300) {
                dialog.showSimpleOk('인증번호 확인', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
                return;
            }
            const response = JSON.parse(xhr.responseText);
            switch (response.result) {
                case 'failure_expired':
                    $registerForm['emailSalt'].value = '';
                    $registerForm['email'].setDisabled(false);
                    $registerForm['emailCodeSendButton'].setDisabled(false);
                    $registerForm['emailCode'].value = '';
                    $registerForm['emailCode'].setDisabled(true);
                    $registerForm['emailCodeVerifyButton'].setDisabled(true);
                    $registerForm['email'].focus();
                    dialog.showSimpleOk('인증번호 확인', '인증 정보가 만료되었습니다.\n이메일 인증을 다시 진행해 주세요.');
                    break;
                case 'success':
                    $registerForm['emailCode'].setDisabled(true);
                    $registerForm['emailCodeVerifyButton'].setDisabled(true);
                    dialog.showSimpleOk('인증번호 확인', '이메일이 정상적으로 인증되었습니다.');
                    break;
                default:
                    dialog.showSimpleOk('인증번호 확인', '인증번호가 올바르지 않습니다.\n다시 확인해 주세요.', () => {
                        $registerForm['emailCode'].focus();
                        $registerForm['emailCode'].select();
                    });
            }
        };
        xhr.open('PATCH', '/user/register-email');
        xhr.setRequestHeader(header, token);
        xhr.send(formData);
        loading.show('인증번호 확인 중');
    });
}

$registerForm['previous'].addEventListener('click', () => {
    currentStep--;
    updateRegisterStep();
});

const registerUser = () => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();

    const registerType = new URL(location.href).searchParams.get('registerType')
    formData.append('registerType', registerType);
    if (registerType === 'local') {
        formData.append('name', $registerForm['name'].value);
        formData.append('email', $registerForm['email'].value);
        formData.append('code', $registerForm['emailCode'].value);
        formData.append('salt', $registerForm['emailSalt'].value);
        formData.append('password', $registerForm['password'].value);
    }
    formData.append('nickname', $registerForm['nickname'].value);
    formData.append('birth', $registerForm['birth'].value);
    formData.append('gender', $registerForm['gender'].value);
    formData.append('contactMvnoCode', $registerForm['contactMvno'].value);
    formData.append('contactFirst', $registerForm['contactFirst'].value);
    formData.append('contactSecond', $registerForm['contactSecond'].value);
    formData.append('contactThird', $registerForm['contactThird'].value);
    formData.append('addressPostal', $registerForm['addressPostal'].value);
    formData.append('addressPrimary', $registerForm['addressPrimary'].value);
    formData.append('addressSecondary', $registerForm['addressSecondary'].value);
    formData.append('categoryId', $registerForm['categoryId'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('회원가입', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_duplicate_email':
                dialog.showSimpleOk('회원가입', `입력하신 이메일 [${$registerForm['email'].value}]은/는 이미 사용 중입니다.`);
                break;
            case 'failure_duplicate_nickname':
                dialog.showSimpleOk('회원가입', `입력하신 닉네임 [${$registerForm['nickname'].value}]은/는 이미 사용 중입니다.`);
                break;
            case 'failure_duplicate_contact':
                dialog.showSimpleOk('회원가입', `입력하신 연락처 [${$registerForm['contactFirst'].value}-${$registerForm['contactSecond'].value}-${$registerForm['contactThird'].value}]은/는 이미 사용 중입니다.`);
                break;
            case 'success':
                currentStep++;
                updateRegisterStep();
                break;
            default:
                dialog.showSimpleOk('회원가입', '알 수 없는 이유로 회원가입에 실패하였습니다.\n잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('POST', '/user/register');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
    loading.show('회원가입 처리 중');
}

$registerForm.addEventListener('submit', (e) => {
    e.preventDefault();

    if (currentStep === 0) {
        if (!isFirstStepValid()) {
            return;
        }
        currentStep++;
    } else if (currentStep === 1) {
        if (!isSecondStepValid()) {
            return;
        }
        currentStep++;
    } else if (currentStep === 2) {
        if (!isThirdStepValid()) {
            return;
        }
        registerUser();
    } else if (currentStep === 3) {
        location.href = `${origin}/user/login`
    }

    updateRegisterStep();
});

const updateRegisterStep = () => {
    $registerForm['previous'].setDisabled(false);
    switch (currentStep) {
        case 0:
            $registerForm['previous'].setDisabled(true);
            $registerForm.querySelector('a[href="/user/login"]').setVisible(true);
            $registerForm['submit'].querySelector(':scope > .--caption').innerText = '다음';
            break;
        case 1:
        case 2:
            $registerForm.querySelector('a[href="/user/login"]').setVisible(true);
            $registerForm['submit'].querySelector(':scope > .--caption').innerText = '다음';
            $registerForm['previous'].setDisabled(false);
            $registerForm['previous'].setVisible(true);
            break;
        case 3:
            $registerForm.querySelector('a[href="/user/login"]').setVisible(false);
            $registerForm['submit'].querySelector(':scope > .--caption').innerText = '로그인하러 가기';
            $registerForm['previous'].setVisible(false);
            break;
        default:
            location.href = '/user/login';
    }
    $stepItems.forEach(($item) => {
        $item.classList.remove('-selected');
    });
    $stepItems[currentStep].classList.add('-selected');
    $containers.forEach(($item) => {
        $item.setVisible(false);
    });
    $containers[currentStep].setVisible(true);
}

const isFirstStepValid = () => {
    if (new URL(location.href).searchParams.get('registerType') === 'local') {
        const $nameLabel = $registerForm.querySelector('.-object-label:has(input[name="name"])');
        const $emailLabel = $registerForm.querySelector('.-object-label:has(input[name="email"])');
        const $passwordLabel = $registerForm.querySelector('.-object-label:has(input[name="password"])');

        const $labels = [$emailLabel, $passwordLabel, $nameLabel];
        $labels.forEach(($label) => {
            $label.setInValid(false);
        });

        if (!$registerForm['emailCodeSendButton'].hasAttribute('disabled')
            || !$registerForm['emailCodeVerifyButton'].hasAttribute('disabled')) {
            $emailLabel.setInValid(true, '이메일 인증을 완료해 주세요.');
        }

        if ($registerForm['name'].validity.valueMissing) {
            $nameLabel.setInValid(true, '이름을 입력해 주세요.');
        } else if (!$registerForm['name'].validity.valid) {
            $nameLabel.setInValid(true, '올바른 이름을 입력해 주세요.');
        }

        if ($registerForm['password'].validity.valueMissing) {
            $passwordLabel.setInValid(true, '비밀번호를 입력해 주세요.');
        } else if (!$registerForm['password'].validity.valid) {
            $passwordLabel.setInValid(true, '올바른 비밀번호를 입력해 주세요.');
        } else if ($registerForm['passwordCheck'].validity.valueMissing) {
            $passwordLabel.setInValid(true, '비밀번호를 한번 더 입력해 주세요.');
        } else if ($registerForm['password'].value !== $registerForm['passwordCheck'].value) {
            $passwordLabel.setInValid(true, '비밀번호가 일치하지 않습니다.');
        }

        if ($labels.some($label => $label.isInValid())) {
            return false;
        }
    }
    return true;
}

const isSecondStepValid = () => {
    const $nicknameLabel = $registerForm.querySelector('.-object-label:has(input[name="nickname"])');
    const $birthLabel = $registerForm.querySelector('.-object-label:has(input[name="birth"])');
    const $contactLabel = $registerForm.querySelector('.-object-label:has(input[name="contactSecond"])');
    const $addressLabel = $registerForm.querySelector('.-object-label:has(input[name="addressPostal"])');
    const $categoryLabel = $registerForm.querySelector('.-object-label:has(input[name="categoryId"])');

    const $labels = [$nicknameLabel, $birthLabel, $contactLabel, $addressLabel, $categoryLabel];
    $labels.forEach(($label) => {
        $label.setInValid(false);
    });

    if (!$registerForm['nicknameCheckButton'].hasAttribute('disabled')) {
        $nicknameLabel.setInValid(true, '닉네임 중복 확인해 주세요.');
    }
    if (!$registerForm['contactCheckButton'].hasAttribute('disabled')) {
        $contactLabel.setInValid(true, '연락처 중복 확인해 주세요.');
    }

    const birthDate = new Date($registerForm['birth'].value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if ($registerForm['birth'].validity.valueMissing) {
        $birthLabel.setInValid(true, '생년월일을 입력해 주세요.');
    } else if (!$registerForm['birth'].validity.valid) {
        $birthLabel.setInValid(true, '올바른 이름을 입력해 주세요.');
    } else if (birthDate > today) {
        $birthLabel.setInValid(true, '생년월일은 미래일 수 없습니다.');
    }

    if ($registerForm['addressPostal'].value.trim() === ''
        || $registerForm['addressPrimary'].value.trim() === '') {
        $addressLabel.setInValid(true, '주소 찾기 버튼을 통해 주소를 입력해 주세요.');
    } else if ($registerForm['addressSecondary'].validity.valueMissing) {
        $addressLabel.setInValid(true, '상세 주소를 입력해 주세요.');
    }

    if ($registerForm['categoryId'].value === '') {
        $categoryLabel.setInValid(true, '관심 카테고리를 선택해 주세요.');
    }

    if ($labels.some($label => $label.isInValid())) {
        return false;
    }
    return true;
}

const isThirdStepValid = () => {
    const $serviceTermLabel = $registerForm.querySelector('.-object-label:has(input[name="agreeServiceTerm"])');
    const $privacyLabel = $registerForm.querySelector('.-object-label:has(input[name="agreePrivacy"])');

    const $labels = [$serviceTermLabel, $privacyLabel];
    $labels.forEach(($label) => {
        $label.setInValid(false);
    });

    if (!$registerForm['agreeServiceTerm'].checked) {
        $serviceTermLabel.setInValid(true, '서비스 이용약관에 동의해주세요.');
    }
    if (!$registerForm['agreePrivacy'].checked) {
        $privacyLabel.setInValid(true, '개인정보 수집 및 이용 동의 약관에 동의해주세요.');
    }

    if ($labels.some($label => $label.isInValid())) {
        return false;
    }
    return true;
}


$registerForm['nicknameCheckButton'].addEventListener('click', () => {
    const $nicknameLabel = $registerForm.querySelector('.-object-label:has(input[name="nickname"])');
    $nicknameLabel.setInValid(false);
    if ($registerForm['nickname'].validity.valueMissing) {
        $nicknameLabel.setInValid(true, '닉네임을 입력해 주세요.');
        $registerForm['nickname'].focus();
    } else if (!$registerForm['nickname'].validity.valid) {
        $nicknameLabel.setInValid(true, '올바른 닉네임을 입력해 주세요.');
        $registerForm['nickname'].focus();
    }
    if ($nicknameLabel.isInValid()) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('nickname', $registerForm['nickname'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('닉네임 중복 확인', '요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.');
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_duplicate':
                dialog.showSimpleOk('닉네임 중복 확인', `${$registerForm['nickname'].value} 은/는 이미 사용 중입니다.`, () => {
                    $registerForm['nickname'].focus();
                });
                break;
            case 'success':
                dialog.show({
                    title: '닉네임 중복 확인',
                    content: `입력하신 닉네임 '${$registerForm['nickname'].value}'은/는 사용가능합니다. 이 닉네임을 사용하시겠습니까?`,
                    buttons: [
                        {
                            caption: '아니요',
                            color: 'gray',
                            onClickCallback: ($modal) => dialog.hide($modal)
                        },
                        {
                            caption: '네',
                            color: 'blue',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                                $registerForm['nickname'].setDisabled(true);
                                $registerForm['nicknameCheckButton'].setDisabled(true)
                            }
                        }
                    ]
                });
                break;
            default:
                dialog.showSimpleOk('닉네임 중복 확인', '알 수 없는 이유로 닉네임 중복을 확인 하지 못하였습니다.\n잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('POST', '/user/nickname-check');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
    loading.show('닉네임 중복 확인 중')
});

$registerForm['contactCheckButton'].addEventListener('click', () => {
    const $contactLabel = $registerForm.querySelector('.-object-label:has(input[name="contactSecond"])');
    $contactLabel.setInValid(false);
    if ($registerForm['contactMvno'].value === '-1') {
        $contactLabel.setInValid(true, '통신사를 선택해 주세요.');
    } else if ($registerForm['contactSecond'].validity.valueMissing
        || $registerForm['contactThird'].validity.valueMissing) {
        $contactLabel.setInValid(true, '전화번호를 입력해 주세요.');
    } else if (!$registerForm['contactSecond'].validity.valid
        || !$registerForm['contactThird'].validity.valid) {
        $contactLabel.setInValid(true, '올바른 전화번호를 입력해 주세요.');
    }
    if ($contactLabel.isInValid()) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('contactFirst', $registerForm['contactFirst'].value);
    formData.append('contactSecond', $registerForm['contactSecond'].value);
    formData.append('contactThird', $registerForm['contactThird'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('연락처 중복 확인', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        const contact = $registerForm['contactFirst'].value
            + '-' + $registerForm['contactSecond'].value
            + '-' + $registerForm['contactThird'].value;
        switch (response.result) {
            case 'failure_duplicate':
                dialog.showSimpleOk('연락처 중복 확인', `[${contact}] 은/는 이미 사용 중입니다.`, () => {
                    $registerForm['contactSecond'].focus();
                });
                break;
            case 'success':
                dialog.show({
                    title: '연락처 중복 확인',
                    content: `입력하신 연락처 '${contact}'은/는 사용가능합니다. 이 연락처를 사용하시겠습니까?`,
                    buttons: [
                        {
                            caption: '아니요',
                            color: 'gray',
                            onClickCallback: ($modal) => dialog.hide($modal)
                        },
                        {
                            caption: '네',
                            color: 'blue',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                                $registerForm['contactMvno'].setDisabled(true);
                                $registerForm['contactFirst'].setDisabled(true);
                                $registerForm['contactSecond'].setDisabled(true);
                                $registerForm['contactThird'].setDisabled(true);
                                $registerForm['contactCheckButton'].setDisabled(true)
                            }
                        }
                    ]
                });
                break;
            default:
                dialog.showSimpleOk('연락처 확인', '알 수 없는 이유로 연락처 중복을 확인 하지 못하였습니다.\n잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('POST', '/user/contact-check');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
    loading.show('연락처 확인 중');
});


$registerForm['addressFindButton'].addEventListener('click', () => {
    const $addressFindDialog = document.getElementById('addressFindDialog');
    const $modal = $addressFindDialog.querySelector(':scope > .modal');
    $addressFindDialog.onclick = () => {
        $addressFindDialog.setVisible(false);
    }
    new daum.Postcode({
        width: '100%',
        height: '100%',
        oncomplete: (data) => {
            $registerForm['addressPostal'].value = data['zonecode'];
            $registerForm['addressPrimary'].value = data['roadAddress'];
            $registerForm['addressSecondary'].focus()
            $registerForm['addressSecondary'].select();
            $addressFindDialog.setVisible(false);
        }
    }).embed($modal);
    $addressFindDialog.setVisible(true);
});