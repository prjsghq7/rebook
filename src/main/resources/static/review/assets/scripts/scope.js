export class Scope {
    $element;
    startX;
    endX;
    currentX;
    elementWidth;

    $scope;
    $stars;
    $scopeGuide;
    scopeNum;

    isProgressDragging = false;

    ratingDesc = {
        1: "별로예요...",
        2: "조금 아쉬워요",
        3: "보통이에요",
        4: "좋았어요",
        5: "최고예요!"
    };

    constructor($element) {
        this.$element = $element;
        this.startX = $element.getBoundingClientRect().left + parseFloat(getComputedStyle($element).paddingLeft);
        this.endX = $element.getBoundingClientRect().right - parseFloat(getComputedStyle($element).paddingRight);
        this.currentX = this.endX;
        this.elementWidth = this.endX - this.startX;

        this.$scope = $element.querySelector('[name="scope"]');
        this.$stars = $element.querySelectorAll('.star');
        this.$scopeGuide = $element.parentElement.querySelector('.scope-guide');
        this.scopeNum = 5

        document.addEventListener('mousemove', (e) => this.#documentOnMousemove(e));
        document.addEventListener('mouseup', (e) => this.#documentOnMouseup());
        this.$element.addEventListener('mousedown', (e) => this.#PointerOnMousedown(e));
    }

    #documentOnMousemove = (e) => {
        if (this.isProgressDragging === true) {
            this.currentX = e.clientX - this.startX;
            if (this.currentX < 0) {
                this.currentX = 0;
            } else if (this.currentX > this.elementWidth) {
                this.currentX = this.elementWidth;
            }

            this.#updateStars();
        }
    }

    #documentOnMouseup = (e) => {
        if (this.isProgressDragging === true) {
            this.isProgressDragging = false;

            this.$scope.value = this.scopeNum;

            this.#updateStars();
        }
    }

    #PointerOnMousedown = (e) => {
        e.preventDefault();
        this.isProgressDragging = true;

        this.currentX = e.clientX - this.startX;
        if (this.currentX < 0) {
            this.currentX = 0;
        } else if (this.currentX > this.elementWidth) {
            this.currentX = this.elementWidth;
        }

        this.#updateStars();
    }

    #updateStars = () => {
        const ratio = this.currentX / this.elementWidth;
        this.scopeNum = Math.max(1, Math.round(ratio * 5));

        for (let i = 0; i < this.$stars.length; i++) {
            this.$stars[i].src = i < this.scopeNum
                ? "/review/assets/images/star-fill.png"
                : "/review/assets/images/star-empty.png";
        }

        this.$scopeGuide.innerText = this.ratingDesc[this.scopeNum];
    }
}