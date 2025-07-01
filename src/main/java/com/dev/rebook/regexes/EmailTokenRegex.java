package com.dev.rebook.regexes;

public class EmailTokenRegex {
    // 이메일 코드: 숫자, 6자
    public static final Regex emailCode = new Regex("^(\\d{6})$");

    public static final Regex emailSalt = new Regex("^([\\da-zA-Z]{128})$");
}
