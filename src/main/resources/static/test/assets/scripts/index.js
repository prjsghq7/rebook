import { Loading } from '/common/assets/scripts/loading.js';

document.addEventListener('DOMContentLoaded', () => {
    const loading = new Loading();

    const $bestsellerTrack = document.querySelector('[data-type="bestseller"] .book-track');
    const $keywordForm     = document.getElementById('keywordForm');
    const $keywordTrack    = $keywordForm.querySelector('.book-track');
    const $newBookTrack = document.querySelector('.new-book-cover .book-track');
    const $popularBookTopTrack = document.querySelector('.user-book-cover .book-track-top');
    const $popularBookBottomTrack = document.querySelector('.user-book-cover .book-track-bottom');

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
            if (book.id) {
                location.href = `${window.origin}/book/?id=${book.id}`;
            }
        });

        $container.appendChild(card);
    }

    async function fetchAndRenderBestSeller() {
        loading.show('베스트 셀러 불러오는 중...');
        try {
            const res = await fetch('/api/aladin/bestseller');
            if (!res.ok) throw new Error('API 응답 오류');
            const books = await res.json();
            books.forEach(book => renderBookCard(book, $bestsellerTrack));
        } catch (err) {
            console.error('베스트셀러 불러오기 실패', err);
        } finally {
            loading.hide();
        }
    }

    async function fetchAndRenderKeywordBooks() {
        const selected = $keywordForm.querySelector('input[name="categoryId"]:checked');
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
            $keywordTrack.innerHTML = '';
            if (books.length === 0) {
                $keywordTrack.classList.add('empty');
            } else {
                $keywordTrack.classList.remove('empty');
                books.forEach(book => renderBookCard(book, $keywordTrack));
            }
        } catch (err) {
            console.error('추천 도서 불러오기 실패', err);
        } finally {
            loading.hide();
        }
    }

    $keywordForm?.addEventListener('change', (e) => {
        if (e.target.name === 'categoryId') {
            fetchAndRenderKeywordBooks();
        }
    });

    async function fetchAndRenderNewBook() {
        try {
            const res = await fetch('/api/aladin/new-top-book');
            if (!res.ok) {
                dialog.showSimpleOk('오류', '오류로 인하여 신간 리스트 책 정보를 불러 올 수 없습니다. 잠시 후 다시 시도해주세요.');
                return;
            }

            const books = await res.json(); // 그냥 배열임
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
            console.log(res);
            if (!res.ok) {
                dialog.showSimpleOk('오류', '책 정보를 불러 올 수 없습니다. 잠시 후 다시 시도해주세요.');
                return;
            }

            const books = await res.json();
            console.log(books);
            const $root = document.querySelector('.user-book-cover');
            $root.innerHTML = '';


            const $left = document.createElement('div');
            $left.className = 'left-column';

            books.slice(0, 2).forEach((book, idx) => {
                const card = document.createElement('div');
                console.log(card);
                card.className = 'rank-book-featured';
                card.dataset.bookId = book.bookId;

                const img = document.createElement('img');
                img.src = book.cover;
                img.alt = book.title;

                const info = document.createElement('div');
                info.className = 'rank-info';

                info.innerHTML = `
        <span class="rank-number">${idx + 1}</span>
        <span class="rank-title">${book.title}</span>
      `;

                card.append(img, info);
                card.onclick = () => location.href = `${window.origin}/book/?id=${book.bookId}`;
                $left.append(card);
            });
            const $right   = document.createElement('div');
            const $grid    = document.createElement('ul');
            $right.className = 'right-column';
            $grid.className  = 'rank-grid';

            const leftRanks  = books.slice(2, 6);
            const rightRanks = books.slice(6, 10);

            for (let i = 0; i < 4; i++) {
                [leftRanks[i], rightRanks[i]].forEach((bk, colIdx) => {
                    if (!bk) return;
                    const li = document.createElement('li');
                    li.className = 'rank-book';
                    li.dataset.bookId = bk.bookId;
                    li.innerHTML = `
          <span class="rank-number">${bk.rank ?? (colIdx ? 7 + i : 3 + i)}</span>
          <span class="rank-title">${bk.title}</span>
        `;
                    li.onclick = () => location.href = `${window.origin}/book/?id=${bk.id}`;
                    $grid.append(li);
                });
            }

            $right.append($grid);
            $root.append($left, $right);

        } catch (e) {
            dialog.showSimpleOk('오류', '책 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
        }
    }





    fetchAndRenderBestSeller();
    fetchAndRenderKeywordBooks();
    fetchAndRenderNewBook();
    getUserPopularBooks();
});
