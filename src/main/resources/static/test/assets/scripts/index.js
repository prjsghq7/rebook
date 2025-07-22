import { Loading } from '/common/assets/scripts/loading.js';

document.addEventListener('DOMContentLoaded', () => {
    const loading = new Loading();

    const $bestsellerTrack = document.querySelector('[data-type="bestseller"] .book-track');
    const $keywordForm     = document.getElementById('keywordForm');
    const $keywordTrack    = $keywordForm.querySelector('.book-track');

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

    fetchAndRenderBestSeller();
    fetchAndRenderKeywordBooks();
});
