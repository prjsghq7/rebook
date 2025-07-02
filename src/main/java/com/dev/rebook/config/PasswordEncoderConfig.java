package com.dev.rebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    private static final int SALT_LENGTH = 16;  // SALT 값 (16 byte)
    private static final int HASH_LENGTH = 64;  // 해시 결과 길이 (64 byte): 3byte당 4문자로 인코딩되는 Base64 기준으로 약 86자리 문자열 생성
    private static final int PARALLELISM = 1;   // 병렬 처리 수: 1(단일 스레드 처리)
    private static final int MEMORY_KB = 65536; // 해시 1회당 메모리 사용 강제 (공격 대응): 64MB
    private static final int ITERATIONS = 5;    // 해쉬 반복 횟수: 5

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY_KB, ITERATIONS);
    }
}
