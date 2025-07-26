package com.dev.rebook.utils;

import com.dev.rebook.entities.UserEntity;
import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Period;

@UtilityClass
public class Utils {
    public static Boolean isAdult(UserEntity signedUser) {
        if (signedUser == null) {
            return false;
        }

        boolean age_result = false;
        int age = LocalDate.now().getYear() - signedUser.getBirth().getYear();

        if (age >= 19) {
            age_result = true;
        }

        return age_result;
    }
    /*
    * 1. 로그인이 안되었을 경우 false
    * 2. 로그인이 되어 있으면서 성인이 아닐경우 false
    * 3. 로그인이 되어 있으면서 성인인 경우 true
    * */
}
