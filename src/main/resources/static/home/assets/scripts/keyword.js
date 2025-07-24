const $keywordForm = document.getElementById('keywordForm');
const $keywordBookResult = $keywordForm.querySelector(':scope > .keyword-book > .keyword-results');

import { Loading } from '/common/assets/scripts/loading.js';
const loading = new Loading();

const $keywordInputs = $keywordForm.querySelectorAll('input[name="categoryId"]');

$keywordInputs.forEach(($input) => {
    $input.addEventListener('change', () => {
        fetchKeywordRecommendations();
    });
});

async function fetchKeywordRecommendations() {
    const selected = $keywordForm.querySelector('input[name="categoryId"]:checked');
    if (!selected) return;

    loading.show();
    try {
        const categoryId = selected.value;
        const response = await fetch(`/api/aladin/category?categoryId=${categoryId}`);
        if (!response.ok) {
            dialog.showSimpleOk('경고', '오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            return;
        }

        const books = await response.json();
        $keywordBookResult.innerHTML = ''; // 기존 카드 제거
        books.forEach(renderBookCard); // 여기만 고치면 됨
    } catch (e) {
        dialog.showSimpleOk('오류', '에라발생');
    } finally {
        loading.hide();
    }
}

function renderBookCard(book) {
    const card = document.createElement('div');
    card.className = 'book-card';

    const front = document.createElement('div');
    front.className = 'book-cover';

    const img = document.createElement('img');
    img.className = 'book-img';
    img.src = book.cover;
    img.alt = book.title;

    front.appendChild(img);
    card.appendChild(front);

    card.addEventListener('click', () => {
        window.open(book.link, '_blank');
    });

    $keywordBookResult.appendChild(card);
}
