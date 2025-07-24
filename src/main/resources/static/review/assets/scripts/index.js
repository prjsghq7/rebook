const $buttonForm = document.getElementById('button-form');

$buttonForm['sort'].forEach(function ($button) {
    $button.addEventListener('change', function () {
        if($buttonForm['mine'] === undefined) {
            location.href = `${window.origin}/review/?sort=${$button.value}`;
        } else {
            location.href = `${window.origin}/review/?sort=${$button.value}&mine=${$buttonForm['mine'].checked}`;
        }
    });
});

if ($buttonForm['mine'] !== undefined) {
    $buttonForm['mine'].addEventListener('change', function () {
        location.href = `${window.origin}/review/?sort=${$buttonForm['sort'].value}&mine=${$buttonForm['mine'].checked}`;
    });
}