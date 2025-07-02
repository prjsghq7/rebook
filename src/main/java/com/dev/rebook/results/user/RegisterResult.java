package com.dev.rebook.results.user;

import com.dev.rebook.results.Result;

public enum RegisterResult implements Result {
    FAILURE_OAUTH_SESSION_EXPIRED,
    FAILURE_DUPLICATE_NICKNAME,
    FAILURE_DUPLICATE_CONTACT
}
