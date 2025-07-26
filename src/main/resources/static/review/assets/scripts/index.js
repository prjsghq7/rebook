const $buttonForm = document.getElementById('button-form');

$buttonForm['sortType'].forEach(function ($button) {
    $button.addEventListener('change', function () {
        if ($buttonForm['mine'] === undefined) {
            location.href = `${window.origin}/review/?sortType=${$button.value}`;
        } else {
            location.href = `${window.origin}/review/?sortType=${$button.value}&mine=${$buttonForm['mine'].checked}`;
        }
    });
});

if ($buttonForm['mine'] !== undefined) {
    $buttonForm['mine'].addEventListener('change', function () {
        location.href = `${window.origin}/review/?sortType=${$buttonForm['sortType'].value}&mine=${$buttonForm['mine'].checked}`;
    });
}

$buttonForm['sort'].addEventListener('change', function () {
    let sortText = "";
    if ($buttonForm['sort'].checked) {
        sortText = "ASC";
    } else {
        sortText = "DESC";
    }

    if ($buttonForm['mine'] !== undefined) {
        location.href = `${window.origin}/review/?sortType=${$buttonForm['sortType'].value}&mine=${$buttonForm['mine'].checked}&sort=${sortText}`;
        } else {
        location.href = `${window.origin}/review/?sortType=${$buttonForm['sortType'].value}&sort=${sortText}`;
    }
});
