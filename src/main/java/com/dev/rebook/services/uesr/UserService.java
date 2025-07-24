package com.dev.rebook.services.uesr;

import com.dev.rebook.dtos.dashboard.DailyUserRegisterStatsDto;
import com.dev.rebook.dtos.dashboard.GenderStatsDto;
import com.dev.rebook.dtos.dashboard.ProviderStatsDto;
import com.dev.rebook.entities.CategoryEntity;
import com.dev.rebook.entities.ContactMvnoEntity;
import com.dev.rebook.entities.EmailTokenEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.CategoryMapper;
import com.dev.rebook.mappers.ContactMvnoMapper;
import com.dev.rebook.mappers.EmailTokenMapper;
import com.dev.rebook.mappers.UserMapper;
import com.dev.rebook.regexes.EmailTokenRegex;
import com.dev.rebook.regexes.UserRegex;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.results.user.ModifyResult;
import com.dev.rebook.results.user.RegisterResult;
import com.dev.rebook.results.user.RemoveAccountResult;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private static EmailTokenEntity generateEmailToken(String email, String userAgent, int expMin) {
        String code = RandomStringUtils.randomNumeric(6);   // "000000" ~ "999999"
        String salt = RandomStringUtils.randomAlphanumeric(128); // a-z A~Z 0~9

        return UserService.generateEmailToken(email, userAgent, code, salt, expMin);
    }

    private static EmailTokenEntity generateEmailToken(String email, String userAgent, String code, String salt, int expMin) {
        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setEmail(email);
        emailToken.setCode(code);
        emailToken.setSalt(salt);
        emailToken.setUserAgent(userAgent);
        emailToken.setUsed(false);
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(expMin));
        return emailToken;
    }

    public static boolean isInvalidUser(UserEntity user) {
        return (user == null || user.isDeleted() || user.isSuspended());
    }

    private final UserMapper userMapper;
    private final EmailTokenMapper emailTokenMapper;
    private final ContactMvnoMapper contactMvnoMapper;
    private final CategoryMapper categoryMapper;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, EmailTokenMapper emailTokenMapper, ContactMvnoMapper contactMvnoMapper, CategoryMapper categoryMapper, JavaMailSender javaMailSender, SpringTemplateEngine springTemplateEngine, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.emailTokenMapper = emailTokenMapper;
        this.contactMvnoMapper = contactMvnoMapper;
        this.categoryMapper = categoryMapper;
        this.javaMailSender = javaMailSender;
        this.springTemplateEngine = springTemplateEngine;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ProviderStatsDto> getProviderStats() {
        return this.userMapper.selectProviderStats();
    }

    public List<GenderStatsDto> getGenderStats() {
        return this.userMapper.selectGenderStats();
    }

    public List<DailyUserRegisterStatsDto> getDailyUserRegisterStats() {
        return this.userMapper.selectDailyUserRegisterStats();
    }

    public Result removeAccount(UserEntity signedUser, EmailTokenEntity emailToken) {
        if (UserService.isInvalidUser(signedUser)) {
            if (signedUser == null || signedUser.isDeleted()) {
                return CommonResult.FAILURE_SESSION_EXPIRED;
            }
            return CommonResult.FAILURE_SUSPENDED;
        }

        if (emailToken == null
                || !EmailTokenRegex.emailCode.matches(emailToken.getCode())
                || !EmailTokenRegex.emailSalt.matches(emailToken.getSalt())) {
            return CommonResult.FAILURE;
        }
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
        if (dbEmailToken == null
                || !dbEmailToken.isUsed()
                || !dbEmailToken.getUserAgent().equals(emailToken.getUserAgent())) {
            return CommonResult.FAILURE;
        }

        if (!signedUser.getEmail().equals(emailToken.getEmail())) {
            return RemoveAccountResult.FAILURE_NO_PERMISSION;
        }

        signedUser.setDeleted(true);
        signedUser.setModifiedAt(LocalDateTime.now());
        return this.userMapper.update(signedUser) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public Result modify(UserEntity user, UserEntity signedUser) {
        if (UserService.isInvalidUser(signedUser)) {
            if (signedUser == null || signedUser.isDeleted()) {
                return CommonResult.FAILURE_SESSION_EXPIRED;
            }
            return CommonResult.FAILURE_SUSPENDED;
        }

        if (!UserRegex.nickname.matches(user.getNickname())
                || !UserRegex.birth.matches(user.getBirth().toString())
                || !UserRegex.gender.matches(user.getGender())
                || !UserRegex.contactSecondRegex.matches(user.getContactSecond())
                || !UserRegex.contactThirdRegex.matches(user.getContactThird())
                || user.getAddressPostal() == null || user.getAddressPostal().isEmpty()
                || user.getAddressPrimary() == null || user.getAddressPrimary().isEmpty()
                || user.getAddressSecondary() == null || user.getAddressSecondary().isEmpty()) {
            return CommonResult.FAILURE;
        }
        if (!signedUser.getNickname().equals(user.getNickname())) {
            if (this.userMapper.selectCountByNickname(user.getNickname()) > 0) {
                return ModifyResult.FAILURE_DUPLICATE_NICKNAME;
            }
        }
        if (!signedUser.getContactFirst().equals(user.getContactFirst())
                || !signedUser.getContactSecond().equals(user.getContactSecond())
                || !signedUser.getContactThird().equals(user.getContactThird())) {
            if (this.userMapper.selectCountByContact(user.getContactFirst(), user.getContactSecond(), user.getContactThird()) > 0) {
                return ModifyResult.FAILURE_DUPLICATE_CONTACT;
            }
        }

        signedUser.setNickname(user.getNickname());
        signedUser.setBirth(user.getBirth());
        signedUser.setGender(user.getGender());
        signedUser.setContactMvnoCode(user.getContactMvnoCode());
        signedUser.setContactFirst(user.getContactFirst());
        signedUser.setContactSecond(user.getContactSecond());
        signedUser.setContactThird(user.getContactThird());
        signedUser.setAddressPostal(user.getAddressPostal());
        signedUser.setAddressPrimary(user.getAddressPrimary());
        signedUser.setAddressSecondary(user.getAddressSecondary());
        signedUser.setCategoryId(user.getCategoryId());
        signedUser.setModifiedAt(LocalDateTime.now());

        return userMapper.update(signedUser) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public Result recoverPassword(EmailTokenEntity emailToken, String password) {
        if (emailToken == null
                || !UserRegex.email.matches(emailToken.getEmail())
                || !UserRegex.password.matches(password)) {
            return CommonResult.FAILURE;
        }

        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
        if (dbEmailToken == null
                || !dbEmailToken.isUsed()
                || !dbEmailToken.getUserAgent().equals(emailToken.getUserAgent())) {
            return CommonResult.FAILURE;
        }

        UserEntity user = this.userMapper.selectLocalUserByEmail(emailToken.getEmail());
        if (user == null || user.isDeleted()) {
            return CommonResult.FAILURE;
        }
        if (user.isSuspended()) {
            return CommonResult.FAILURE_SUSPENDED;
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setModifiedAt(LocalDateTime.now());
        return this.userMapper.update(user) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public Result recoverEmail(UserEntity user) {
        if (user == null
                || !UserRegex._name.matches(user.getName())
                || !UserRegex.birth.matches(user.getBirth().toString())
                || !UserRegex.contactSecondRegex.matches(user.getContactSecond())
                || !UserRegex.contactThirdRegex.matches(user.getContactThird())
                || this.contactMvnoMapper.selectCountByCode(user.getContactMvnoCode()) < 1) {
            return CommonResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectLocalUserByContact(user.getContactMvnoCode(), user.getContactFirst(), user.getContactSecond(), user.getContactThird());
        if (dbUser == null
                || dbUser.isDeleted()
                || !dbUser.getName().equals(user.getName())
                || !dbUser.getBirth().equals(user.getBirth())) {
            return CommonResult.FAILURE_ABSENT;
        }

        user.setEmail(dbUser.getEmail());
        if (dbUser.isSuspended()) {
            return CommonResult.FAILURE_SUSPENDED;
        }

        return CommonResult.SUCCESS;
    }

    public UserEntity selectByProviderId(String provider, String providerId) {
        return this.userMapper.selectUserByProviderId(provider, providerId);
    }

    public ResultTuple<EmailTokenEntity> sendRemoveAccountEmail(UserEntity signedUser, String email, String userAgent) throws MessagingException {
        if (!UserRegex.email.matches(email) || userAgent == null) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }

        if (UserService.isInvalidUser(signedUser)) {
            if (signedUser == null || signedUser.isDeleted()) {
                return ResultTuple.<EmailTokenEntity>builder()
                        .result(CommonResult.FAILURE_SESSION_EXPIRED)
                        .build();
            }
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE_SUSPENDED)
                    .build();
        }

        if (!signedUser.getEmail().equals(email)) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(RemoveAccountResult.FAILURE_NO_PERMISSION)
                    .build();
        }

        String code = RandomStringUtils.randomNumeric(6);   // "000000" ~ "999999"
        String salt = RandomStringUtils.randomAlphanumeric(128); // a-z A~Z 0~9
        EmailTokenEntity emailToken = UserService.generateEmailToken(email, userAgent, code, salt, 10);

        if (this.emailTokenMapper.insert(emailToken) < 1) {
            return ResultTuple.<EmailTokenEntity>builder().result(CommonResult.FAILURE).build();
        }

        //이메일 전송
        Context context = new Context();
        // thymeleaf 에서 사용할 데이터 입력
        context.setVariable("code", emailToken.getCode());
        // 보낼 html 파일 경로
        String mailText = this.springTemplateEngine.process("user/removeAccountEmail", context);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("prjsghq7@gmail.com");
        mimeMessageHelper.setTo(emailToken.getEmail());
        mimeMessageHelper.setSubject("[ReBook] 회원탈퇴 인증번호");
        mimeMessageHelper.setText(mailText, true);
        this.javaMailSender.send(mimeMessage);

        return ResultTuple.<EmailTokenEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(emailToken)
                .build();
    }

    public ResultTuple<EmailTokenEntity> sendRecoverPasswordEmail(String email, String userAgent) throws MessagingException {
        if (!UserRegex.email.matches(email) || userAgent == null) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        if (this.userMapper.selectLocalUserCountByEmail(email) == 0) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE_ABSENT)
                    .build();
        }
        String code = RandomStringUtils.randomNumeric(6);   // "000000" ~ "999999"
        String salt = RandomStringUtils.randomAlphanumeric(128); // a-z A~Z 0~9
        EmailTokenEntity emailToken = UserService.generateEmailToken(email, userAgent, code, salt, 10);

        if (this.emailTokenMapper.insert(emailToken) < 1) {
            return ResultTuple.<EmailTokenEntity>builder().result(CommonResult.FAILURE).build();
        }

        //이메일 전송
        Context context = new Context();
        // thymeleaf 에서 사용할 데이터 입력
        context.setVariable("code", emailToken.getCode());
        // 보낼 html 파일 경로
        String mailText = this.springTemplateEngine.process("user/recoverEmail", context);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("prjsghq7@gmail.com");
        mimeMessageHelper.setTo(emailToken.getEmail());
        mimeMessageHelper.setSubject("[ReBook] 계정 복구(비밀번호 재설정) 인증번호");
        mimeMessageHelper.setText(mailText, true);
        this.javaMailSender.send(mimeMessage);

        return ResultTuple.<EmailTokenEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(emailToken)
                .build();
    }

    public ResultTuple<EmailTokenEntity> sendRegisterEmail(String email, String userAgent) throws MessagingException {
        if (!UserRegex.email.matches(email) || userAgent == null) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        if (this.userMapper.selectLocalUserCountByEmail(email) > 0) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE_DUPLICATE)
                    .build();
        }
        String code = RandomStringUtils.randomNumeric(6);   // "000000" ~ "999999"
        String salt = RandomStringUtils.randomAlphanumeric(128); // a-z A~Z 0~9
        EmailTokenEntity emailToken = UserService.generateEmailToken(email, userAgent, code, salt, 10);

        if (this.emailTokenMapper.insert(emailToken) < 1) {
            return ResultTuple.<EmailTokenEntity>builder().result(CommonResult.FAILURE).build();
        }

        //이메일 전송
        Context context = new Context();
        // thymeleaf 에서 사용할 데이터 입력
        context.setVariable("code", emailToken.getCode());
        // 보낼 html 파일 경로
        String mailText = this.springTemplateEngine.process("user/registerEmail", context);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("prjsghq7@gmail.com");
        mimeMessageHelper.setTo(emailToken.getEmail());
        mimeMessageHelper.setSubject("[ReBook] 회원가입 인증번호");
        mimeMessageHelper.setText(mailText, true);
        this.javaMailSender.send(mimeMessage);

        return ResultTuple.<EmailTokenEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(emailToken)
                .build();
    }

    public Result checkNickname(String nickname) {
        if (!UserRegex.nickname.matches(nickname)) {
            return CommonResult.FAILURE;
        }
        return this.userMapper.selectCountByNickname(nickname) > 0
                ? CommonResult.FAILURE_DUPLICATE
                : CommonResult.SUCCESS;
    }

    public Result checkContact(String contactFirst, String contactSecond, String contactThird) {
        if (!UserRegex.contactSecondRegex.matches(contactSecond)
                || !UserRegex.contactThirdRegex.matches(contactThird)) {
            return CommonResult.FAILURE;
        }
        return this.userMapper.selectCountByContact(contactFirst, contactSecond, contactThird) > 0
                ? CommonResult.FAILURE_DUPLICATE
                : CommonResult.SUCCESS;
    }

    public ContactMvnoEntity[] getContactMvnos() {
        return contactMvnoMapper.selectAll();
    }

    public CategoryEntity getCategory(String id) {
        return this.categoryMapper.selectById(id);
    }

    public List<CategoryEntity> getCategories() {
        return categoryMapper.selectAll();
    }

    public Result register(EmailTokenEntity emailToken,
                           UserEntity user,
                           String registerType,
                           OAuth2User oauthUser) {
        if (user == null) {
            return CommonResult.FAILURE;
        }
        if (registerType.equals("local")) {
            if (emailToken == null
                    || !EmailTokenRegex.emailCode.matches(emailToken.getCode())
                    || !EmailTokenRegex.emailSalt.matches(emailToken.getSalt())) {
                return CommonResult.FAILURE;
            }
            EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
            if (dbEmailToken == null
                    || !dbEmailToken.isUsed()
                    || !dbEmailToken.getUserAgent().equals(emailToken.getUserAgent())) {
                return CommonResult.FAILURE;
            }

            if (!UserRegex._name.matches(user.getName())
                    || !UserRegex.email.matches(user.getEmail())
                    || !UserRegex.password.matches(user.getPassword())) {
                return CommonResult.FAILURE;
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setProvider("local");
            user.setProviderId(user.getEmail());
        } else if (registerType.equals("oauth")) {
            if (oauthUser == null) {
                return RegisterResult.FAILURE_OAUTH_SESSION_EXPIRED;
            }
            user.setName(oauthUser.getAttribute("name"));
            user.setEmail(oauthUser.getAttribute("email"));
            user.setPassword(oauthUser.getAttribute("provider"));
            user.setProvider(oauthUser.getAttribute("provider"));
            user.setProviderId(oauthUser.getAttribute("providerId"));
            user.setProfileImg(oauthUser.getAttribute("picture"));
        } else {
            return CommonResult.FAILURE;
        }

        if (!UserRegex.nickname.matches(user.getNickname())
                || !UserRegex.birth.matches(user.getBirth().toString())
                || !UserRegex.gender.matches(user.getGender())
                || !UserRegex.contactSecondRegex.matches(user.getContactSecond())
                || !UserRegex.contactThirdRegex.matches(user.getContactThird())
                || user.getAddressPostal() == null || user.getAddressPostal().isEmpty()
                || user.getAddressPrimary() == null || user.getAddressPrimary().isEmpty()
                || user.getAddressSecondary() == null || user.getAddressSecondary().isEmpty()) {
            return CommonResult.FAILURE;
        }
        if (categoryMapper.selectCountById(user.getCategoryId()) < 1
                || contactMvnoMapper.selectCountByCode(user.getContactMvnoCode()) < 1) {
            return CommonResult.FAILURE;
        }

        if (this.userMapper.selectCountByNickname(user.getNickname()) > 0) {
            return RegisterResult.FAILURE_DUPLICATE_NICKNAME;
        }
        if (this.userMapper.selectCountByContact(user.getContactFirst(), user.getContactSecond(), user.getContactThird()) > 0) {
            return RegisterResult.FAILURE_DUPLICATE_CONTACT;
        }

        user.setTermAgreedAt(LocalDate.now());
        user.setAdmin(false);
        user.setDeleted(false);
        user.setSuspended(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setModifiedAt(LocalDateTime.now());
        user.setLastSignedAt(LocalDateTime.now());
        user.setLastSignedUa(emailToken.getUserAgent());

        return this.userMapper.insert(user) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public ResultTuple<UserEntity> login(String email, String password, String userAgent) {
        if (!UserRegex.email.matches(email)
                || !UserRegex.password.matches(password)) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        UserEntity dbUser = this.userMapper.selectLocalUserByEmail(email);
        if (dbUser == null || dbUser.isDeleted()) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        if (dbUser.isSuspended()) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE_SUSPENDED)
                    .build();
        }
        if (!passwordEncoder.matches(password, dbUser.getPassword())) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }

        dbUser.setLastSignedAt(LocalDateTime.now());
        dbUser.setLastSignedUa(userAgent);

        Result result = this.userMapper.update(dbUser) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;

        return ResultTuple.<UserEntity>builder()
                .result(result)
                .payload(dbUser)
                .build();
    }
}
