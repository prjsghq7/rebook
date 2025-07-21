const CARD_REM = 12; // 책 너비
const GAP_REM  = 2;  // 간격
const VIEW_CNT = 4;  // 한 페이지 카드 수
const AUTO_MS  = 3500; // 자동 슬라이드 간격 (ms)

const rem2px = rem => rem * parseFloat(getComputedStyle(document.documentElement).fontSize);

function initCarousel(section) {
    if (!section) return;

    const track = section.querySelector('.carousel-track');
    const list  = section.querySelector('.book-track');
    const prev  = section.querySelector('.carousel-control.prev');
    const next  = section.querySelector('.carousel-control.next');

    const originals = [...list.children];
    if (!originals.length) return;

    const CARD = rem2px(CARD_REM);
    const GAP  = rem2px(GAP_REM);
    const STEP = (CARD + GAP) * VIEW_CNT;

    // 앞뒤 클론
    originals.slice(-VIEW_CNT).forEach(n => list.prepend(n.cloneNode(true)));
    originals.slice(0,  VIEW_CNT).forEach(n => list.append(n.cloneNode(true)));

    list.style.width = `${list.children.length * (CARD + GAP) - GAP}px`;

    const realPages  = Math.ceil(originals.length / VIEW_CNT);
    const FIRST      = 1;
    const LAST       = realPages;
    const LAST_CLONE = realPages + 1;
    let pageIdx      = FIRST;
    let timer;

    const go = (idx, instant = false) => {
        list.style.transition = instant ? 'none' : 'transform .6s ease';
        list.style.transform  = `translateX(${-idx * STEP}px)`;
        pageIdx = idx;
    };

    const nextPage = () => go(pageIdx + 1);
    const prevPage = () => go(pageIdx - 1);

    list.addEventListener('transitionend', () => {
        if (pageIdx === 0) go(LAST, true);
        else if (pageIdx === LAST_CLONE) go(FIRST, true);
    });

    const start = () => (timer = setInterval(nextPage, AUTO_MS));
    const stop  = () => clearInterval(timer);

    next.onclick = () => { stop(); nextPage(); start(); };
    prev.onclick = () => { stop(); prevPage(); start(); };

    go(FIRST, true);
    start();

    // 캐러셀 다시 접근했을 때 위치 리셋 (뒤로가기 대비)
    section.dataset.carouselStep = STEP;
}

function observeAndInitCarousel(sectionSelector) {
    const section = document.querySelector(sectionSelector);
    if (!section) return;

    const list = section.querySelector('.book-track');
    if (list && list.children.length) return initCarousel(section);

    const ob = new MutationObserver((_, o) => {
        if (list.children.length) {
            o.disconnect();
            initCarousel(section);
        }
    });
    ob.observe(list, { childList: true });
}

function resetCarousel(sectionSelector) {
    const section = document.querySelector(sectionSelector);
    if (!section) return;
    const list = section.querySelector('.book-track');
    if (!list || !list.children.length) return;

    const STEP = section.dataset.carouselStep || (rem2px(CARD_REM + GAP_REM) * VIEW_CNT);
    list.style.transition = 'none';
    list.style.transform  = `translateX(${-STEP}px)`;
}

document.addEventListener('DOMContentLoaded', () => {
    observeAndInitCarousel('[data-type="bestseller"]');
    observeAndInitCarousel('[data-type="keyword"]');
});

// 뒤로가기/앞으로가기 복귀 시 캐러셀 위치 리셋
window.addEventListener('pageshow', e => {
    if (e.persisted) {
        resetCarousel('[data-type="bestseller"]');
        resetCarousel('[data-type="keyword"]');
    }
});
