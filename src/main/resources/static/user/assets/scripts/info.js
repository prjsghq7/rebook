const $reviewList = document.querySelector(':scope .review-list');

$reviewList.querySelectorAll(':scope .modify').forEach(($modify) => {
    $modify.addEventListener('click', () => {
        const reviewId = $modify.getAttribute('data-rb-id');
        window.open(
            `/review/modify?id=${reviewId}`,
            'reviewWindow',
            'width=480,height=544,top=0,left=200,resizable=no,scrollbars=no'
        );
    });
});