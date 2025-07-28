const $buttonForm = document.getElementById('button-form');
const $searchInput = $buttonForm.querySelector(`:scope > .button-container > .search > .button-label > .button-input`);

$buttonForm['sortType'].forEach(($button) => {
    $button.addEventListener('change', () => {
        if ($buttonForm['mine'] === undefined) {
            location.href = `${window.origin}/review/?sortType=${$button.value}&sort=${$buttonForm['sort'].checked}&keyword=${$searchInput.value}`;
        } else {
            location.href = `${window.origin}/review/?sortType=${$button.value}&mine=${$buttonForm['mine'].checked}&sort=${$buttonForm['sort'].checked}&keyword=${$searchInput.value}`;
        }
    });
});


if ($buttonForm['mine'] !== undefined) {
    $buttonForm['mine'].addEventListener('change', () => {
        location.href = `${window.origin}/review/?sortType=${$buttonForm['sortType'].value}&mine=${$buttonForm['mine'].checked}&sort=${$buttonForm['sort'].checked}&keyword=${$searchInput.value}`;
    });
}

$buttonForm['sort'].addEventListener('change', () => {
    if ($buttonForm['mine'] !== undefined) {
        location.href = `${window.origin}/review/?sortType=${$buttonForm['sortType'].value}&mine=${$buttonForm['mine'].checked}&sort=${$buttonForm['sort'].checked}&keyword=${$searchInput.value}`;
    } else {
        location.href = `${window.origin}/review/?sortType=${$buttonForm['sortType'].value}&sort=${$buttonForm['sort'].checked}&keyword=${$searchInput.value}`;
    }
});

let isClick = $searchInput.classList.contains('visible');
$buttonForm.addEventListener('submit', (e) => {
    e.preventDefault();

    if (!isClick) {
        $searchInput.classList.add('visible');
        isClick = true;
    } else {
        $buttonForm.submit();
    }
});