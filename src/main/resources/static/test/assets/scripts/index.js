import { Loading } from '/common/assets/scripts/loading.js';

const loading = new Loading();
const $bestForm = document.getElementById('bestForm');
const $bestBook = document.getElementById('bestBook');
const $keywordForm = document.getElementById('keywordForm');
const $keywordMap = $keywordForm.querySelectorAll(':scope > .-object-label');
const $keywordBook = document.getElementById('keyword-book');

document.addEventListener('DOMContentLoaded', () => {
    function renderBestSellerBook(book) {
        const card = document.createElement('div');
        card.className = 'book-card';

        const front = document.createElement('div');
        front.className = 'book-cover';

        const img = document.createElement('img');
        img.src = book.cover;
        img.alt = book.title;

        const titleDiv = document.createElement('div');
        titleDiv.className = 'book-title';
        titleDiv.textContent = book.title;

        front.appendChild(img);
        card.appendChild(front);
        card.appendChild(titleDiv);

        $bestBook.appendChild(card);
    }

    async function fetchBestSeller() {
        const response = await fetch('/api/aladin/bestseller');
        if (!response.ok) throw new Error("API 응답 오류");

        const books = await response.json();
        return books;
    }

    loading.show('베스트 셀러 불러오는 중...');

    fetchBestSeller()
        .then(books => {
            books.forEach(renderBestSellerBook);
        })
        .catch(err => {
            console.error('베스트셀러 불러오기 실패', err);
        })
        .finally(() => {
            loading.hide();
        });
});
