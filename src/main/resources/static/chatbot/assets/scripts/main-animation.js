const CARD_REM = 12;        // 카드 가로 12rem
const GAP_REM  = 2;         // 카드 간격 2rem
const VIEW_CNT = 4;         // 화면에 4권
const AUTO_MS  = 3500;      // 자동 슬라이드 3.5초

/* ───────── 유틸 ───────── */
const rem2px = rem => rem * parseFloat(getComputedStyle(document.documentElement).fontSize);

/* ───────── 핵심 함수 ───────── */
function initBestSellerCarousel(){
    /* 요소 가져오기 */
    const track  = document.querySelector('#bestSeller .carousel-track');
    const list   = document.getElementById('bestBook');
    const prev   = document.querySelector('#bestSeller .carousel-control.prev');
    const next   = document.querySelector('#bestSeller .carousel-control.next');

    /* 카드 없으면 중단 */
    const originals = [...list.children];
    if (!originals.length) return;

    /* px 단위 계산 */
    const CARD = rem2px(CARD_REM);
    const GAP  = rem2px(GAP_REM);
    const STEP = (CARD + GAP) * VIEW_CNT;

    /* 앞뒤 클론 4장씩 */
    originals.slice(-VIEW_CNT).forEach(n => list.prepend(n.cloneNode(true)));
    originals.slice(0, VIEW_CNT).forEach(n => list.append(n.cloneNode(true)));

    /* 폭 고정(필수) */
    const totalCards = list.children.length;
    list.style.width = `${totalCards * (CARD + GAP) - GAP}px`;

    /* 페이지 지표 */
    const realPages  = Math.ceil(originals.length / VIEW_CNT); // 실제 페이지 수(3)
    const firstReal  = 1;                     // 0 = 앞 클론
    const lastReal   = realPages;             // 3
    const lastClone  = realPages + 1;         // 4
    let pageIdx      = firstReal;
    let timer;

    /* 이동 함수 */
    const go = (idx, instant = false) => {
        list.style.transition = instant ? 'none' : 'transform .6s ease';
        list.style.transform  = `translateX(${-idx * STEP}px)`;
        pageIdx = idx;
    };

    /* 다음/이전 */
    const nextPage = () => go(pageIdx + 1);
    const prevPage = () => go(pageIdx - 1);

    /* 무한 루프 처리 → clone 페이지에 **도착한 뒤** 순간점프 */
    list.addEventListener('transitionend', () => {
        if (pageIdx === 0)         go(lastReal, true); // 앞 클론 → 마지막 실제
        else if (pageIdx === lastClone) go(firstReal, true); // 뒤 클론 → 첫 실제
    });

    /* 자동 */
    const start = () => timer = setInterval(nextPage, AUTO_MS);
    const stop  = () => clearInterval(timer);

    /* 버튼 */
    next.onclick = () => { stop(); nextPage(); start(); };
    prev.onclick = () => { stop(); prevPage(); start(); };

    /* 초기 진입 */
    go(firstReal, true);
    start();
}

/* ───────── book-card가 DOM에 들어간 뒤 실행 ─────────
   (데이터를 Ajax로 넣는 경우까지 커버)                  */
document.addEventListener('DOMContentLoaded', () => {
    const list = document.getElementById('bestBook');

    /* 이미 렌더돼 있으면 바로 실행 */
    if (list.children.length) return initBestSellerCarousel();

    /* 아니라면 변화 감지 후 1회만 실행 */
    const ob = new MutationObserver((m,o)=>{
        if (list.children.length){
            o.disconnect();
            initBestSellerCarousel();
        }
    });
    ob.observe(list, {childList:true});
});