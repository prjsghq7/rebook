const CARD_REM = 12;
const GAP_REM  = 2;
const VIEW_CNT = 4;
const AUTO_MS  = 3500;
const DUR_MS   = 600;
const EASE     = 'ease';

const rem2px = rem =>
    rem * parseFloat(getComputedStyle(document.documentElement).fontSize);

function initCarousel(section) {
    if (!section) return;
    if (section._c) section._c.destroy();

    const track = section.querySelector('.book-track');
    if (!track) return;

    const originals = [...track.children].filter(n => !n.dataset.clone);
    if (!originals.length) return;

    let CARD = rem2px(CARD_REM);
    let GAP  = rem2px(GAP_REM);
    let STEP = (CARD + GAP) * VIEW_CNT;

    let pages   = Math.max(1, Math.ceil(originals.length / VIEW_CNT));
    const FIRST = 1;
    const LAST  = pages;
    const LAST_CLN = pages + 1;

    let page  = FIRST;
    let busy  = false;
    let timer = null;

    const setWidth = () => {
        track.style.width = `${track.children.length * (CARD + GAP) - GAP}px`;
    };

    const buildClones = () => {
        originals.slice(-VIEW_CNT).forEach(n => {
            const c = n.cloneNode(true);
            c.dataset.clone = '1';
            track.prepend(c);
        });
        originals.slice(0, VIEW_CNT).forEach(n => {
            const c = n.cloneNode(true);
            c.dataset.clone = '1';
            track.append(c);
        });
    };

    const clearClones = () => {
        [...track.children].forEach(n => { if (n.dataset.clone) n.remove(); });
    };

    const move = idx => {
        if (busy) return;
        busy = true;
        track.style.transition = `transform ${DUR_MS}ms ${EASE}`;
        track.style.transform  = `translateX(${-idx * STEP}px)`;
        page = idx;
        clearTimeout(move._fuse);
        move._fuse = setTimeout(() => { busy = false; }, DUR_MS + 80);
    };

    const snap = idx => {
        track.style.transition = 'none';
        track.style.transform  = `translateX(${-idx * STEP}px)`;
        page = idx;
    };

    const onEnd = () => {
        if (page === 0) snap(LAST);
        else if (page === LAST_CLN) snap(FIRST);
        busy = false;
    };

    const start = () => {
        stop();
        timer = setInterval(() => move(page + 1), AUTO_MS);
    };

    const stop = () => {
        if (timer) clearInterval(timer);
        timer = null;
    };

    buildClones();
    setWidth();
    snap(FIRST);

    track.addEventListener('transitionend', onEnd);
    section.querySelectorAll('.carousel-control').forEach(btn => {
        btn.addEventListener('click', e => {
            e.preventDefault();
            if (busy) return;
            stop();
            btn.classList.contains('next') ? move(page + 1) : move(page - 1);
            start();
        });
    });

    start();

    section._c = {
        stop,
        start,
        snapCurrent: () => snap(page),
        destroy() {
            stop();
            track.removeEventListener('transitionend', onEnd);
            clearTimeout(move._fuse);
            clearClones();
            track.style.transition = 'none';
            track.style.transform  = '';
            track.style.width      = '';
            delete section._c;
        },
        refresh() {
            stop();
            clearTimeout(move._fuse);
            clearClones();
            const fresh = [...track.children].filter(n => !n.dataset.clone);
            if (!fresh.length) { this.destroy(); return; }
            CARD = rem2px(CARD_REM);
            GAP  = rem2px(GAP_REM);
            STEP = (CARD + GAP) * VIEW_CNT;
            pages = Math.max(1, Math.ceil(fresh.length / VIEW_CNT));
            buildClones();
            setWidth();
            snap(FIRST);
            start();
        }
    };

    const mo = new MutationObserver(records => {
        const changedByExternal = records.some(rec =>
            [...rec.addedNodes, ...rec.removedNodes].some(n => !(n instanceof Element && n.dataset && n.dataset.clone))
        );
        if (changedByExternal) section._c?.refresh();
    });
    mo.observe(track, { childList: true });

    const _destroy = section._c.destroy.bind(section._c);
    section._c.destroy = () => { mo.disconnect(); _destroy(); };
}

document.addEventListener('DOMContentLoaded', () => {
    ['[data-type="bestseller"]', '[data-type="category"]'].forEach(sel => {
        const sec = document.querySelector(sel);
        if (!sec) return;
        const track = sec.querySelector('.book-track');
        if (track && track.children.length) {
            initCarousel(sec);
        } else if (track) {
            const ob = new MutationObserver((records, self) => {
                if (track.children.length) {
                    self.disconnect();
                    initCarousel(sec);
                }
            });
            ob.observe(track, { childList: true });
        }
    });
});

window.addEventListener('pageshow', e => {
    if (e.persisted) {
        document.querySelectorAll('[data-type]').forEach(sec => sec._c?.snapCurrent());
    }
});

document.addEventListener('visibilitychange', () => {
    const all = document.querySelectorAll('[data-type]');
    if (document.hidden) {
        all.forEach(sec => sec._c?.stop());
    } else {
        all.forEach(sec => { sec._c?.snapCurrent(); sec._c?.start(); });
    }
});

(() => {
    let raf = 0;
    const onResize = () => {
        cancelAnimationFrame(raf);
        raf = requestAnimationFrame(() => {
            document.querySelectorAll('[data-type]').forEach(sec => sec._c?.refresh());
        });
    };
    window.addEventListener('resize', onResize, { passive: true });
})();
