const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $modifyForm = document.getElementById('modifyForm');

$modifyForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const $nicknameLabel = $modifyForm.querySelector('.-object-label:has(input[name="nickname"])');
    const $birthLabel = $modifyForm.querySelector('.-object-label:has(input[name="birth"])');
    const $contactLabel = $modifyForm.querySelector('.-object-label:has(input[name="contactSecond"])');
    const $addressLabel = $modifyForm.querySelector('.-object-label:has(input[name="addressPostal"])');
    const $categoryLabel = $modifyForm.querySelector('.-object-label:has(input[name="categoryId"])');

    const $labels = [$nicknameLabel, $birthLabel, $contactLabel, $addressLabel];
    $labels.forEach(($label) => {
        $label.setInValid(false);
    });

    if (!$modifyForm['nicknameCheckButton'].hasAttribute('disabled')) {
        $nicknameLabel.setInValid(true, '닉네임 중복 확인해 주세요.');
    }
    if (!$modifyForm['contactCheckButton'].hasAttribute('disabled')) {
        $contactLabel.setInValid(true, '연락처 중복 확인해 주세요.');
    }

    if ($modifyForm['birth'].validity.valueMissing) {
        $birthLabel.setInValid(true, '생년월일을 입력해 주세요.');
    } else if (!$modifyForm['birth'].validity.valid) {
        $birthLabel.setInValid(true, '올바른 이름을 입력해 주세요.');
    }

    if ($modifyForm['addressPostal'].value.trim() === ''
        || $modifyForm['addressPrimary'].value.trim() === '') {
        $addressLabel.setInValid(true, '주소 찾기 버튼을 통해 주소를 입력해 주세요.');
    } else if ($modifyForm['addressSecondary'].validity.valueMissing) {
        $addressLabel.setInValid(true, '상세 주소를 입력해 주세요.');
    }

    if ($modifyForm['categoryId'].value === '') {
        $categoryLabel.setInValid(true, '관심 카테고리를 선택해 주세요.');
    }

    if ($labels.some($label => $label.isInValid())) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('nickname', $modifyForm['nickname'].value);
    formData.append('birth', $modifyForm['birth'].value);
    formData.append('gender', $modifyForm['gender'].value);
    formData.append('contactMvnoCode', $modifyForm['contactMvno'].value);
    formData.append('contactFirst', $modifyForm['contactFirst'].value);
    formData.append('contactSecond', $modifyForm['contactSecond'].value);
    formData.append('contactThird', $modifyForm['contactThird'].value);
    formData.append('addressPostal', $modifyForm['addressPostal'].value);
    formData.append('addressPrimary', $modifyForm['addressPrimary'].value);
    formData.append('addressSecondary', $modifyForm['addressSecondary'].value);
    formData.append('categoryId', $modifyForm['categoryId'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('회원 정보 수정', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_session_expired':
                dialog.showSimpleOk('회원 정보 수정', '세션이 만료되었습니다.\n로그인 후 회원 정보 수정 해주세요.');
                break;
            case 'failure_suspended':
                dialog.showSimpleOk('회원 정보 수정', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'failure_duplicate_nickname':
                dialog.showSimpleOk('회원 정보 수정', `입력하신 닉네임 [${$modifyForm['nickname'].value}]은/는 이미 사용 중입니다.`);
                break;
            case 'failure_duplicate_contact':
                dialog.showSimpleOk('회원 정보 수정', `입력하신 연락처 [${$modifyForm['contactFirst'].value}-${$modifyForm['contactSecond'].value}-${$modifyForm['contactThird'].value}]은/는 이미 사용 중입니다.`);
                break;
            case 'success':
                dialog.showSimpleOk('회원 정보 수정', '회원 정보 수정이 완료되었습니다.', () => {
                    location.href = `${origin}/user/info`
                });
                break;
            default:
                dialog.showSimpleOk('회원 정보 수정', '알 수 없는 이유로 회원 정보 수정에 실패하였습니다.\n잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('PATCH', '/user/modify');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
    loading.show('회원 정보 수정 중');
});

$modifyForm['addressFindButton'].addEventListener('click', () => {
    const $addressFindDialog = document.getElementById('addressFindDialog');
    const $modal = $addressFindDialog.querySelector(':scope > .modal');
    $addressFindDialog.onclick = () => {
        $addressFindDialog.setVisible(false);
    }
    new daum.Postcode({
        width: '100%',
        height: '100%',
        oncomplete: (data) => {
            console.log(data);
            $modifyForm['addressPostal'].value = data['zonecode'];
            $modifyForm['addressPrimary'].value = data['roadAddress'];
            $modifyForm['addressSecondary'].focus()
            $modifyForm['addressSecondary'].select();
            $addressFindDialog.setVisible(false);
        }
    }).embed($modal);
    $addressFindDialog.setVisible(true);
});

const originalNickname = $modifyForm['nickname'].value;
$modifyForm['nickname'].addEventListener('input', () => {
    if ($modifyForm['nickname'].value !== originalNickname) {
        $modifyForm['nicknameCheckButton'].removeAttribute('disabled');
    } else {
        $modifyForm['nicknameCheckButton'].setAttribute('disabled', '');
    }
});

$modifyForm['nicknameCheckButton'].addEventListener('click', () => {
    const $nicknameLabel = $modifyForm.querySelector('.-object-label:has(input[name="nickname"])');
    $nicknameLabel.setInValid(false);
    if ($modifyForm['nickname'].validity.valueMissing) {
        $nicknameLabel.setInValid(true, '닉네임을 입력해 주세요.');
        $modifyForm['nickname'].focus();
    } else if (!$modifyForm['nickname'].validity.valid) {
        $nicknameLabel.setInValid(true, '올바른 닉네임을 입력해 주세요.');
        $modifyForm['nickname'].focus();
    }
    if ($nicknameLabel.isInValid()) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('nickname', $modifyForm['nickname'].value);
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
                dialog.showSimpleOk('닉네임 중복 확인', `${$modifyForm['nickname'].value} 은/는 이미 사용 중입니다.`, () => {
                    $modifyForm['nickname'].focus();
                });
                break;
            case 'success':
                dialog.show({
                    title: '닉네임 중복 확인',
                    content: `입력하신 닉네임 '${$modifyForm['nickname'].value}'은/는 사용가능합니다. 이 닉네임을 사용하시겠습니까?`,
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
                                $modifyForm['nickname'].setDisabled(true);
                                $modifyForm['nicknameCheckButton'].setDisabled(true)
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
    loading.show('닉네임 중복 확인 중');
});


const setDisableContactButton = () => {
    if ($modifyForm['contactFirst'].value !== originalContactFirst
        || $modifyForm['contactSecond'].value !== originalContactSecond
        || $modifyForm['contactThird'].value !== originalContactThird) {
        $modifyForm['contactCheckButton'].removeAttribute('disabled');
    } else {
        $modifyForm['contactCheckButton'].setAttribute('disabled', '');
    }
};

const originalContactFirst = $modifyForm['contactFirst'].value;
const originalContactSecond = $modifyForm['contactSecond'].value;
const originalContactThird = $modifyForm['contactThird'].value;
$modifyForm['contactFirst'].addEventListener('input', setDisableContactButton);
$modifyForm['contactSecond'].addEventListener('input', setDisableContactButton);
$modifyForm['contactThird'].addEventListener('input', setDisableContactButton);

$modifyForm['contactCheckButton'].addEventListener('click', () => {
    const $contactLabel = $modifyForm.querySelector('.-object-label:has(input[name="contactSecond"])');
    $contactLabel.setInValid(false);
    if ($modifyForm['contactMvno'].value === '-1') {
        $contactLabel.setInValid(true, '통신사를 선택해 주세요.');
    } else if ($modifyForm['contactSecond'].validity.valueMissing
        || $modifyForm['contactThird'].validity.valueMissing) {
        $contactLabel.setInValid(true, '전화번호를 입력해 주세요.');
    } else if (!$modifyForm['contactSecond'].validity.valid
        || !$modifyForm['contactThird'].validity.valid) {
        $contactLabel.setInValid(true, '올바른 전화번호를 입력해 주세요.');
    }
    if ($contactLabel.isInValid()) {
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('contactFirst', $modifyForm['contactFirst'].value);
    formData.append('contactSecond', $modifyForm['contactSecond'].value);
    formData.append('contactThird', $modifyForm['contactThird'].value);
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
        const contact = $modifyForm['contactFirst'].value
            + '-' + $modifyForm['contactSecond'].value
            + '-' + $modifyForm['contactThird'].value;
        switch (response.result) {
            case 'failure_duplicate':
                dialog.showSimpleOk('연락처 중복 확인', `[${contact}] 은/는 이미 사용 중입니다.`, () => {
                    $modifyForm['contactSecond'].focus();
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
                                $modifyForm['contactMvno'].setDisabled(true);
                                $modifyForm['contactFirst'].setDisabled(true);
                                $modifyForm['contactSecond'].setDisabled(true);
                                $modifyForm['contactThird'].setDisabled(true);
                                $modifyForm['contactCheckButton'].setDisabled(true)
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

