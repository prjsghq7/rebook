const $searchForm = document.getElementById('searchForm');
const $searchInput = $searchForm.querySelector('input[name="keyword"]');
const $gridContainer = document.getElementById('grid-container');

$searchForm.onsubmit =  (e) => {
    e.preventDefault();

    if($searchForm['keyword'].value === '') {
        alert('검색어를 입력하세요.');
        return;
    }

    const xhr = new XMLHttpRequest();
    const url = new URL(`${origin}/home/search-list`);
    url.searchParams.set('keyword', $searchInput.value);
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
                        <div class="box">
                            <img class="img" src="${book['cover']}" alt="${book.title} 표지">
                        </div>
                        <span class="title">${book['title']}</span>
                    </div>
                `;
                console.log("gridHtml\n" + gridHtml);
            }
            $gridContainer.innerHTML = gridHtml;
        } else {
            console.log("검색 결과가 없습니다.");
        }
    };
    console.log(url);
    xhr.open('GET', url);
    xhr.send();
};
