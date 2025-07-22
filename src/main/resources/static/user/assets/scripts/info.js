const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

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

const requestDeleteReview = (reviewId) => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('id', reviewId)
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('리뷰 삭제', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요.`);
            return;
        }

        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_no_permission':
                dialog.showSimpleOk('리뷰 삭제', '해당 리뷰글에 대한 삭제 권한이 없습니다.');
                break;
            case 'failure_absent':
                dialog.showSimpleOk('리뷰 삭제', '해당 리뷰글이 이미 삭제되었거나 존재 하지 않습니다.');
                break;
            case 'failure_session_expired':
                dialog.showSimpleOk('리뷰 삭제', '세션이 만료되었습니다.\n로그인 후 리뷰 수정 해주세요.');
                break;
            case 'failure_suspended':
                dialog.showSimpleOk('리뷰 삭제', '해당 계정은 정지된 상태입니다.\n관리자에게 문의 해주세요.');
                break;
            case 'success':
                dialog.show({
                    title: '리뷰 삭제',
                    content: '리뷰가 성공적으로 삭제되었습니다.',
                    buttons: [
                        {
                            caption: '확인',
                            color: 'blue',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                                window.close();
                            }
                        }
                    ]
                });
                break;
            default:
                dialog.showSimpleOk('리뷰 등록', '리뷰 삭제에 실패하였습니다.\n다시 확인해 주세요.')
        }
    };
    xhr.open('DELETE', '/review/delete');
    xhr.setRequestHeader(header, token);
    xhr.send(formData);
}

$reviewList.querySelectorAll(':scope .delete').forEach(($delete) => {
    $delete.addEventListener('click', () => {
        const reviewId = $delete.getAttribute('data-rb-id');
        dialog.show({
            title: '리뷰 삭제',
            content: '리뷰 삭제를 진행 하시겠습니까?\n리뷰 삭제시 기록이 삭제되며 복구 불가능합니다.',
            buttons: [
                {
                    caption: '계속 진행', color: 'red', onClickCallback: ($dialog) => {
                        dialog.hide($dialog);
                        requestDeleteReview(reviewId);
                    }
                },
                {
                    caption: '취소', color: 'gray', onClickCallback: ($dialog) => {
                        dialog.hide($dialog);
                    }
                }
            ]
        });
    });
});