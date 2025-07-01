package com.dev.rebook.regexes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserRegex {
    // 이름: 한글 2~5자
    public static final Regex _name = new Regex("^([가-힣]{2,5})$");
    // 이메일
    public static final Regex email = new Regex("^(?=.{8,50}$)([\\da-z\\-_.]{4,})@([\\da-z][\\da-z\\-]*[\\da-z]\\.)?([\\da-z][\\da-z\\-]*[\\da-z])\\.([a-z]{2,15})(\\.[a-z]{2,3})?$");

    // 패스워드:
    public static final Regex password = new Regex("^([\\da-zA-Z`~!@#$%^&*()\\-_=+\\[{\\]}\\\\|;:'\",<.>/?]{8,50})$");

    // 닉네임: 한글, 영문, 숫자 2~15자
    public static final Regex nickname = new Regex("^([\\da-zA-Z가-힣]{2,15})$");
    // 생년월일: YYYY-MM-DD 형식 (1900~2099년 범위)
    public static final Regex birth = new Regex("^(19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");
    // 성별: "M" 또는 "F"
    public static final Regex gender = new Regex("^[MF]$");

    // 전화번호 두번째: 숫자, 3~4자리
    public static final Regex contactSecondRegex = new Regex("^(\\d{3,4})$");
    // 전화번호 세번째: 숫자, 4자리
    public static final Regex contactThirdRegex = new Regex("^(\\d{4})$");
}
