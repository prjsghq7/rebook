export class Loading {
    #$element;
    #$caption;

    constructor() {
        this.#$element = document.createElement('div');
        this.#$element.className = '-object-loading';
        this.#$element.setVisible(false); // 초기 숨김

        const $icon = document.createElement('img');
        $icon.className = 'icon';
        $icon.src = '/common/assets/images/loading.gif';

        this.#$caption = document.createElement('div');
        this.#$caption.className = 'caption';
        this.#$caption.textContent = '잠시만 기다려주세요.';

        this.#$element.append($icon, this.#$caption);
        document.body.appendChild(this.#$element);
    }

    /**
     * 로딩 표시
     * @param {string} message
     */
    show(message = '잠시만 기다려주세요.') {
        this.#$caption.textContent = message;
        this.#$element.setVisible(true);
    }

    /**
     * 로딩 숨김
     */
    hide() {
        this.#$element.setVisible(false);
    }

    /**
     * 로딩 표시 여부
     * @returns {boolean}
     */
    isVisible() {
        return this.#$element.isVisible();
    }
}
