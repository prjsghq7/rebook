package com.dev.rebook.services.uesr;

import com.dev.rebook.entities.EmailTokenEntity;
import com.dev.rebook.mappers.EmailTokenMapper;
import com.dev.rebook.regexes.EmailTokenRegex;
import com.dev.rebook.regexes.UserRegex;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.email_token.VerifyEmailTokenResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailTokenService {
    private final EmailTokenMapper emailTokenMapper;

    @Autowired
    public EmailTokenService(EmailTokenMapper emailTokenMapper) {
        this.emailTokenMapper = emailTokenMapper;
    }

    public Result verityEmailToken(EmailTokenEntity emailToken) {
        if (!UserRegex.email.matches(emailToken.getEmail())
                || !EmailTokenRegex.emailCode.matches(emailToken.getCode())
                || !EmailTokenRegex.emailSalt.matches(emailToken.getSalt())) {
            return CommonResult.FAILURE;
        }
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
        if (dbEmailToken == null) {
            return CommonResult.FAILURE;
        }
        if (!emailToken.getUserAgent().equals(dbEmailToken.getUserAgent()) || dbEmailToken.isUsed()) {
            return CommonResult.FAILURE;
        }
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return VerifyEmailTokenResult.FAILURE_EXPIRED;
        }
        dbEmailToken.setUsed(true);
        return this.emailTokenMapper.update(dbEmailToken) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }
}
