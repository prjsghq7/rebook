/* carousel.js — click‑lock safe */
const CARD_REM = 12;
// 카드 한 장의 너비 12rem
const GAP_REM  = 2;
// 카드 사이 여백이 2rem
const VIEW_CNT = 4;
// 한 화면에 보이는 카드 개수
const AUTO_MS  = 3500;
//3.5초

const rem2px = rem =>
    rem * parseFloat(getComputedStyle(document.documentElement).fontSize);
// js에서는 rem 단위를 이해 못하기때문에

function initCarousel(section) {
    if (!section || section._c) return;

    const track     = section.querySelector('.book-track');
    const originals = [...track.children];
    if (!originals.length) return;

    const CARD = rem2px(CARD_REM);
    const GAP  = rem2px(GAP_REM);
    const STEP = (CARD + GAP) * VIEW_CNT;

    originals.slice(-VIEW_CNT).forEach(n => track.prepend(n.cloneNode(true)));
    // 마지막 4개 카드 복제해서 앞에 붙임
    originals.slice(0,  VIEW_CNT).forEach(n => track.append(n.cloneNode(true)));
    // 처음 4개 카드 복제해서 뒤에 붙임 그래야 슬라이드를 했을때 무한루프로 돌 수 있기때문
    track.style.width = `${track.children.length * (CARD + GAP) - GAP}px`;
    // 마지막 갭을 빼는 이유는 마지막 카드에는 갭이 없기때문에

    const pages      = Math.ceil(originals.length / VIEW_CNT);
    // 예를들어 총 북이8개이고 한 페이지에 보여야될 북이4개라고하면 pages = 2
    const FIRST      = 1;
    const LAST       = pages;
    const LAST_CLONE = pages + 1;

    let page   = FIRST;
    let timer  = null;
    let busy   = false;             // ← 클릭 잠금 플래그

    const move = idx => {
        if (busy) return;             // 애니 중이면 무시
        busy = true;
        track.style.transition = 'transform .6s ease';
        track.style.transform  = `translateX(${-idx * STEP}px)`;
        page = idx;
    };

    const snap = idx => {
        track.style.transition = 'none';
        track.style.transform  = `translateX(${-idx * STEP}px)`;
        page  = idx;
    };

    track.addEventListener('transitionend', () => {
        if (page === 0)        snap(LAST);
        else if (page === LAST_CLONE) snap(FIRST);
        busy = false;          // 애니 끝났으니 잠금 해제
    });

    const start = () => {
        stop();
        timer = setInterval(() => move(page + 1), AUTO_MS);
    };
    const stop  = () => timer && clearInterval(timer);

    section.querySelectorAll('.carousel-control').forEach(btn => {
        btn.addEventListener('click', e => {
            e.preventDefault();
            if (busy) return;           // 애니 중이면 무시
            stop();
            btn.classList.contains('next') ? move(page + 1) : move(page - 1);
            start();
        });
    });

    snap(FIRST);  // 처음 위치
    start();

    // 노출(visibility) 관리용 최소 API
    section._c = { stop, start, snapCurrent: () => snap(page) };
}

/* 초기 세팅 */
document.addEventListener('DOMContentLoaded', () => {
    ['[data-type="bestseller"]', '[data-type="category"]']
        .forEach(sel => {
            const sec = document.querySelector(sel);
            if (!sec) return;
            const track = sec.querySelector('.book-track');
            if (track.children.length) {
                initCarousel(sec);
            } else {
                new MutationObserver((_, ob) => {
                    if (track.children.length) {
                        ob.disconnect();
                        initCarousel(sec);
                    }
                }).observe(track, { childList: true });
            }
        });
});

/* BFCache 복원 */
window.addEventListener('pageshow', e => {
    if (e.persisted) {
        document.querySelectorAll('[data-type]').forEach(sec => sec._c?.snapCurrent());
    }
});

/* 탭 전환 */
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        document.querySelectorAll('[data-type]').forEach(sec => sec._c?.stop());
    } else {
        document.querySelectorAll('[data-type]').forEach(sec => {
            sec._c?.snapCurrent();
            sec._c?.start();
        });
    }
});
