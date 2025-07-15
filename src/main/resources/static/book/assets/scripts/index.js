document.querySelector('[name="reviewRegisterButton"]').addEventListener('click', () => {
    const bookId = new URL(location.href).searchParams.get('id');
    window.open(
        `/review/register?id=${bookId}`,
        'reviewWindow',
        'width=480,height=544,top=0,left=200,resizable=no,scrollbars=no'
    );
});