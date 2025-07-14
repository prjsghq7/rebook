const $searchForm = document.getElementById('searchForm');
const $searchInput = $searchForm.querySelector('input[name="keyword"]');
const $resultContainer = document.getElementById('result-container');
const $initial = $resultContainer.querySelector(':scope > .result.initial');
const $noResult = $resultContainer.querySelector(':scope > .result.noResult');
const $gridContainer = document.getElementById('grid-container');

$searchForm.onsubmit = (e) => {
    e.preventDefault();

    if ($searchForm['keyword'].value === '') {
        alert('검색어를 입력하세요.');
        return;
    }

    const xhr = new XMLHttpRequest();
    const url = new URL(`${origin}/book/search-list`);
    url.searchParams.set('keyword', $searchInput.value);
    url.searchParams.set('searchType', $searchForm['searchType'].value);
    url.searchParams.set('searchTarget', $searchForm['searchTarget'].value);
    url.searchParams.set('sort', $searchForm['sort'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            alert('요청 실패');
            return;
        }
        const response = JSON.parse(xhr.responseText);
        console.log(response);
        if (response.length > 0) {
            console.log(response); // 여러 개의 책 정보 배열
            $gridContainer.innerHTML = '';
            let gridHtml = '';

            for (const book of response) {
                console.log("book\n" + book);
                gridHtml += `
                <div class="item">
                    <div class="item-bg-area"></div>
                    <a href="/book/?id=${book['id']}" class="detail">
                        <div class="box">
                                <img class="img" src="${book['cover']}" alt="${book['title']} 표지">
                            </div>
                            <span class="title">${book['title']}</span>
                            <span class="author">${book['author']}</span>
                        </a>
                    </div>
                `;
                console.log("gridHtml\n" + gridHtml);
            }
            $gridContainer.innerHTML = gridHtml;

            $resultContainer.setVisible(false);
            $initial.setVisible(false);
            $noResult.setVisible(false);
            $gridContainer.setVisible(true);
        } else {
            // result-container 표출 + noResult 표출
            $resultContainer.setVisible(true);
            $initial.setVisible(false);
            $noResult.setVisible(true);
            $gridContainer.setVisible(false);
            console.log("검색 결과가 없습니다.");
        }
    };
    console.log(url);
    xhr.open('GET', url);
    xhr.send();
};


$resultContainer.setVisible(true);
$initial.setVisible(true);
$noResult.setVisible(false);
$gridContainer.setVisible(false);