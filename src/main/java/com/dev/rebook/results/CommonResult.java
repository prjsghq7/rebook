package com.dev.rebook.results;

public enum CommonResult implements Result{
    FAILURE,
    FAILURE_ABSENT,         //없음
    FAILURE_DUPLICATE,      //중복
    FAILURE_SESSION_EXPIRED,      //로그인 필요 | 권한 없음
    SUCCESS
}
