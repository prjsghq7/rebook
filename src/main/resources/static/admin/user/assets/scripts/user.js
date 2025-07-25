const $tbody = document.getElementById('userTableBody');
const $form = document.getElementById('userEditForm');
const $modal = document.getElementById('userEditModal');
const $cancelEditBtn = document.getElementById('cancelEditBtn');
const $editBtn = document.getElementById('editBtn');
const CSRF_TOKEN = document.querySelector('meta[name="_csrf"]').content;
const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]').content;


let currentUserId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const flagCell = v => `<td class="${v ? 'yes' : 'no'}">${v ? 'O' : 'X'}</td>`;

    try {
        const res = await fetch('/admin/user/get');
        const json = await res.json();
        const users = json.payload;


        users.forEach((user, index) => {
            const createdAt = formatDate(user.createdAt);
            const lastSignedAt = formatDate(user.lastSignedAt);
            const row = document.createElement('tr');

            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${user.email}</td>
                <td>${user.name}</td>
                <td>${user.nickname}</td>
                <td>${createdAt}</td>
                ${flagCell(user.deleted)}
                ${flagCell(user.suspended)}
                <td>${lastSignedAt}</td>
            `;

            // dataset에 저장
            Object.entries(user).forEach(([key, value]) => {
                row.dataset[key] = value;
            });
            console.log(row.dataset);

            row.addEventListener('click', () => {
                Object.entries(row.dataset).forEach(([key, value]) => {
                    if ($form.elements[key]) {
                        $form.elements[key].value = value == null ? '' : value;
                        console.log(row.dataset);

                    }
                    console.log(row.dataset);

                });
                currentUserId = row.dataset.id;
                $modal.classList.remove('hidden');
            });

            console.log(row);
            $tbody.appendChild(row);
            console.log(row);
        });
    } catch (err) {
        console.error('회원 정보 조회 실패:', err);
    }
});

$editBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    try {
        const formData = new FormData($form);

        const res = await fetch('/admin/user/edit', {
            method: 'POST',
            body: formData,
            headers: {
                [CSRF_HEADER]: CSRF_TOKEN,
            },
            credentials: 'include'
        });

        const result = await res.json();

        if (result === 'FAILURE_SESSION_EXPIRED') {
            dialog.showSimpleOk('오류', '로그인이 만료되었거나 권한이 없습니다.');
            return;
        }

        if (result === 'SUCCESS') {
            dialog.showSimpleOk('회원수정', `${currentUserId}번 회원의 정보가 수정되었습니다.`);
            $modal.classList.add('hidden');
        } else {
            dialog.showSimpleOk('실패', '회원 수정에 실패했습니다.');
        }
    } catch (e) {
        dialog.showSimpleOk('오류', '회원 수정 요청 중 에러가 발생했습니다.');
        console.error(e);
    }
});

$cancelEditBtn.addEventListener('click', () => {
    $modal.classList.add('hidden');
});
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const mi = String(date.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
}