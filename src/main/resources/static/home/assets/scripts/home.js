document.addEventListener('DOMContentLoaded', () => {
    const $bestsellerTrack = document.querySelector('[data-type="bestseller"] .book-track');
    const $categoryForm  = document.getElementById('categoryForm');
    const $categoryTrack = $categoryForm ? $categoryForm.querySelector('.book-track') : null;
    const $newBookTrack  = document.querySelector('.new-book-cover .book-track');

    function renderBookCard(book, $container) {
        const card = document.createElement('div');
        card.className = 'book-card';
        card.classList.add('id', book.id);

        const cover = document.createElement('div');
        cover.className = 'book-cover';

        const img = document.createElement('img');
        img.src = book.cover;
        img.alt = book.title;

        const title = document.createElement('div');
        title.className = 'book-title';
        title.textContent = book.title;

        cover.appendChild(img);
        card.appendChild(cover);
        card.appendChild(title);

        card.addEventListener('click', () => {
            if (book.id) location.href = `${window.origin}/book/?id=${book.id}`;
        });

        $container.appendChild(card);
    }

    async function fetchAndRenderBestSeller() {
        loading.show('베스트 셀러 불러오는 중...');
        try {
            const res = await fetch('/api/aladin/bestseller');
            if (!res.ok) throw new Error('API 응답 오류');
            const books = await res.json();
            if ($bestsellerTrack) books.forEach(book => renderBookCard(book, $bestsellerTrack));
        } catch (err) {
            console.error('베스트셀러 불러오기 실패', err);
        } finally {
            loading.hide();
        }
    }

    async function fetchAndRendercategoryBooks() {
        if (!$categoryForm || !$categoryTrack) return;
        const selected = $categoryForm.querySelector('input[name="categoryId"]:checked');
        if (!selected) return;

        loading.show('추천 도서 불러오는 중...');
        try {
            const res = await fetch(`/api/aladin/category?categoryId=${selected.value}`);
            if (!res.ok) throw new Error('API 응답 오류');
            const json = await res.json();

            if (json.result === 'FAILURE') {
                dialog.showSimpleOk('오류', '도서 리스트를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
                return;
            }

            const books = json.payload || [];
            $categoryTrack.innerHTML = '';
            if (books.length === 0) {
                $categoryTrack.classList.add('empty');
            } else {
                $categoryTrack.classList.remove('empty');
                books.forEach(book => renderBookCard(book, $categoryTrack));
            }
        } catch (err) {
            console.error('추천 도서 불러오기 실패', err);
        } finally {
            loading.hide();
        }
    }

    if ($categoryForm) {
        $categoryForm.addEventListener('change', (e) => {
            if (e.target.name === 'categoryId') fetchAndRendercategoryBooks();
        });
    }

    async function fetchAndRenderNewBook() {
        try {
            const res = await fetch('/api/aladin/new-top-book');
            if (!res.ok) {
                dialog.showSimpleOk('오류', '오류로 인하여 신간 리스트 책 정보를 불러 올 수 없습니다. 잠시 후 다시 시도해주세요.');
                return;
            }

            const books = await res.json(); // 배열
            if (!$newBookTrack) return;
            $newBookTrack.innerHTML = '';

            if (books.length === 0) {
                $newBookTrack.classList.add('empty');
            } else {
                $newBookTrack.classList.remove('empty');
                books.forEach(book => renderBookCard(book, $newBookTrack));
            }
        } catch (e) {
            console.log(e);
            dialog.showSimpleOk('오류', '책 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
        }
    }

    async function getUserPopularBooks() {
        try {
            const res = await fetch('/book/popular-book');
            if (!res.ok) {
                dialog.showSimpleOk('오류', '책 정보를 불러 올 수 없습니다. 잠시 후 다시 시도해주세요.');
                return;
            }

            const books = await res.json();
            const $root = document.querySelector('.user-book-cover');
            if (!$root) return;
            $root.innerHTML = '';

            // Left: 1~2위
            const $left = document.createElement('div');
            $left.className = 'left-column';

            books.slice(0, 2).forEach((book, idx) => {
                const card = document.createElement('div');
                card.className = 'rank-book-featured';
                card.dataset.bookId = book.bookId;

                const img = document.createElement('img');
                img.src = book.cover;
                img.alt = book.title;

                const info = document.createElement('div');
                info.className = 'rank-info';

                const num = document.createElement('span');
                num.className = 'rank-number';
                num.textContent = String(idx + 1);

                const title = document.createElement('span');
                title.className = 'rank-title';
                title.textContent = book.title;

                info.append(num, title);
                card.append(img, info);
                card.onclick = () => location.href = `${window.origin}/book/?id=${book.bookId}`;
                $left.append(card);
            });

            // Right grid: 3~10 (순차로 append)
            const $right = document.createElement('div');
            $right.className = 'right-column';
            const $grid = document.createElement('ul');
            $grid.className = 'rank-grid';

            const rest = books.slice(2, Math.min(10, books.length)); // 최대 8개
            rest.forEach((bk, i) => {
                const li = document.createElement('li');
                li.className = 'rank-book';
                li.dataset.bookId = bk.bookId;

                const num = document.createElement('span');
                num.className = 'rank-number';
                num.textContent = String(3 + i); // 3..10

                const title = document.createElement('span');
                title.className = 'rank-title';
                title.textContent = bk.title;

                li.append(num, title);
                li.onclick = () => location.href = `${window.origin}/book/?id=${bk.bookId}`;
                $grid.append(li);
            });

            $right.append($grid);
            $root.append($left, $right);
        } catch (e) {
            dialog.showSimpleOk('오류', '책 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
        }
    }

    fetchAndRenderBestSeller();
    fetchAndRendercategoryBooks();
    fetchAndRenderNewBook();
    getUserPopularBooks();
});
