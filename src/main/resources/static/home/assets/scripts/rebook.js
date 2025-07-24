// rebook.js – 키워드 랜덤 배치, 책 추천 로직, 카드 flip, 캐러셀 이동 포함

document.addEventListener("DOMContentLoaded", () => {
    const submitBtn = document.getElementById("submitcategorys");
    const responseList = document.getElementById("bookSlider");
    const categoryForm = document.getElementById("categoryForm");
    const categoryList = document.querySelector(".category-list");
    const categoryCounter = document.getElementById("categoryCounter");
    const categorySection = document.querySelector(".category-recommend");
    const carouselSection = document.getElementById("bookCarouselWrapper");

    // 랜덤 배치
    function randomlyPlacecategorys() {
        const usedCoords = [];
        const items = categoryList.querySelectorAll(".item");

        items.forEach(item => {
            let x, y, overlap;
            let attempts = 0;

            do {
                x = Math.random() * 80 + 10;
                y = Math.random() * 60 + 10;
                overlap = usedCoords.some(coord => Math.abs(coord.x - x) < 10 && Math.abs(coord.y - y) < 10);
                attempts++;
            } while (overlap && attempts < 50); // 무한 루프 방지

            usedCoords.push({ x, y });
            item.style.left = `${x}%`;
            item.style.top = `${y}%`;
            item.style.transform = `translate(-${x}%, -${y}%)`;
        });
    }


    randomlyPlacecategorys();

    // 카운터 반영
    categoryForm.addEventListener("change", () => {
        const count = categoryForm.querySelectorAll('input[name="categorys"]:checked').length;
        categoryCounter.textContent = `${count}/5`;
        if (count > 5) {
            alert("최대 5개까지만 선택할 수 있습니다.");
            categoryForm.querySelector('input[name="categorys"]:checked:last-child').checked = false;
        }
    });

    // 책 추천 로직
    submitBtn.addEventListener("click", async () => {
        const selectedcategorys = Array.from(document.querySelectorAll('input[name="categorys"]:checked'))
            .map(el => el.value);
        if (selectedcategorys.length === 0) {
            alert("관심 키워드를 선택해주세요.");
            return;
        }

        categorySection.classList.add("hidden");
        carouselSection.classList.remove("hidden");

        try {
            const gptTitles = await fetchGPTBookTitles(selectedcategorys);
            const bookInfos = await Promise.all(gptTitles.map(title => fetchAladinBookInfo(title)));
            const validBooks = bookInfos.filter(Boolean);
            responseList.innerHTML = "";
            validBooks.forEach(renderBookCard);
        } catch (err) {
            console.error("에러 발생:", err);
        }
    });

    // GPT로 책 제목 요청
    async function fetchGPTBookTitles(categorys) {
        const prompt = `아래 관심 키워드를 가진 사용자에게 어울리는 책 제목을 각 키워드별로 3개씩 추천해줘.
관심 키워드: ${categorys.join(", ")}

반드시 JSON 배열 형식으로만 응답해.
예시: ["제목1", "제목2", "제목3"]
마크 다운 없이 배열만 출력해.
응답은 철저히 JSON형식으로 응답해. 십새야
배열에 배열 감싸지말고 책 제목만 한 배열에 담아줘
`;

        const res = await fetch("https://api.openai.com/v1/chat/completions", {
            method: "POST",
            headers: {
                Authorization: "Bearer {apikey}",
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                model: "gpt-4o-mini",
                messages: [{ role: "user", content: prompt }],
                temperature: 0.7,
            }),
        });
        const data = await res.json();
        const raw = data.choices[0].message.content;
        return JSON.parse(raw);
    }

    // 알라딘 API 요청
    async function fetchAladinBookInfo(title) {
        const encoded = encodeURIComponent(title);
        const response = await fetch(`/test/api/aladin?category=${encoded}`);
        const xml = await response.text();
        const doc = new DOMParser().parseFromString(xml, "text/xml");
        const item = doc.querySelector("item");
        if (!item) return null;

        return {
            title: item.querySelector("title")?.textContent,
            author: item.querySelector("author")?.textContent,
            pubDate: item.querySelector("pubDate")?.textContent,
            cover: item.querySelector("cover")?.textContent,
            link: item.querySelector("link")?.textContent,
        };
    }

    // 카드 렌더링
    function renderBookCard(book) {
        const card = document.createElement("div");
        card.className = "book-card";

        const front = document.createElement("div");
        front.className = "book-cover";
        const img = document.createElement("img");
        img.src = book.cover;
        front.appendChild(img);

        const back = document.createElement("div");
        back.className = "book-info";
        back.innerHTML = `
      <h3>${book.title}</h3>
      <p>${book.author}</p>
      <p>${book.pubDate}</p>
      <a href="${book.link}" target="_blank">자세히 보기</a>
    `;

        card.appendChild(front);
        card.appendChild(back);
        card.addEventListener("click", () => card.classList.toggle("flipped"));

        responseList.appendChild(card);
    }
});
