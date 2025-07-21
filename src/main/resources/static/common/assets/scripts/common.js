HTMLElement.INVALID_ATTR_NAME = 'data-rb-invalid';
HTMLElement.VALID_ATTR_NAME = 'data-rb-valid';
HTMLElement.VISIBLE_ATTR_NAME = 'data-rb-visible';


/**
 * @param {boolean} b
 * @returns {HTMLElement} */
HTMLElement.prototype.setDisabled = function (b) {
    if (b === true) {
        this.setAttribute('disabled', '');
    } else if (b === false) {
        this.removeAttribute('disabled');
    }
    return this;
}

/**@returns {boolean} */
HTMLElement.prototype.isInValid = function () {
    return this.hasAttribute(HTMLElement.INVALID_ATTR_NAME);
}
/**
 * @param {boolean} b
 * @param {string|undefined} warningText
 * @returns {HTMLElement} */
HTMLElement.prototype.setInValid = function (b, warningText = undefined) {
    this.removeAttribute(HTMLElement.VALID_ATTR_NAME);
    if (b === true) {
        const $warning = this.querySelector(':scope > .--warning');
        if ($warning != null && warningText != null) {
            $warning.innerText = warningText;
        }
        this.setAttribute(HTMLElement.INVALID_ATTR_NAME, '');
    } else if (b === false) {
        this.removeAttribute(HTMLElement.INVALID_ATTR_NAME);
    }
    return this;
}


/**@returns {boolean} */
HTMLElement.prototype.isValid = function () {
    return this.hasAttribute(HTMLElement.VALID_ATTR_NAME);
}
/**
 * @param {boolean} b
 * @returns {HTMLElement} */
HTMLElement.prototype.setValid = function (b) {
    this.removeAttribute(HTMLElement.INVALID_ATTR_NAME);
    if (b === true) {
        this.setAttribute(HTMLElement.VALID_ATTR_NAME, '');
    } else if (b === false) {
        this.removeAttribute(HTMLElement.VALID_ATTR_NAME);
    }
    return this;
}

/**@returns {boolean} */
HTMLElement.prototype.isVisible = function () {
    return this.hasAttribute(HTMLElement.VISIBLE_ATTR_NAME);
}
/**
 * @param {boolean} b
 * @returns {HTMLElement} */
HTMLElement.prototype.setVisible = function (b) {
    if (b === true) {
        this.setAttribute(HTMLElement.VISIBLE_ATTR_NAME, '');
    } else if (b === false) {
        this.removeAttribute(HTMLElement.VISIBLE_ATTR_NAME);
    }
    return this;
}