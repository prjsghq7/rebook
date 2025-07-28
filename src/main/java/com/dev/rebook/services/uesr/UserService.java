package com.dev.rebook.services.uesr;

import com.dev.rebook.dtos.dashboard.AgeGroupStatsDto;
import com.dev.rebook.dtos.dashboard.DailyUserRegisterStatsDto;
import com.dev.rebook.dtos.dashboard.GenderStatsDto;
import com.dev.rebook.dtos.dashboard.ProviderStatsDto;
import com.dev.rebook.dtos.user.UserDto;
import com.dev.rebook.entities.*;
import com.dev.rebook.mappers.*;
import com.dev.rebook.regexes.EmailTokenRegex;
import com.dev.rebook.regexes.UserRegex;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.results.user.ModifyResult;
import com.dev.rebook.results.user.RegisterResult;
import com.dev.rebook.results.user.RemoveAccountResult;
import com.dev.rebook.vos.ReviewPageVo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {

    @Value("${file.upload-dir}")
    private String uploadDir;

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
    private final DailyLoginStatsMapper dailyLoginStatsMapper;

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, EmailTokenMapper emailTokenMapper, ContactMvnoMapper contactMvnoMapper, CategoryMapper categoryMapper, DailyLoginStatsMapper dailyLoginStatsMapper, JavaMailSender javaMailSender, SpringTemplateEngine springTemplateEngine, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.emailTokenMapper = emailTokenMapper;
        this.contactMvnoMapper = contactMvnoMapper;
        this.categoryMapper = categoryMapper;
        this.dailyLoginStatsMapper = dailyLoginStatsMapper;
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

    public List<AgeGroupStatsDto> getAgeGroupStats() {
        return this.userMapper.selectAgeGroupStats();
    }

    public List<DailyLoginStatsEntity> getDailyUserLoginStats(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return null;
        }
        if (from.isAfter(to)) {
            return null;
        }
        return this.dailyLoginStatsMapper.selectDailyLoginStats(from, to);
    }

    public List<DailyUserRegisterStatsDto> getDailyUserRegisterStats(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return null;
        }
        if (from.isAfter(to)) {
            return null;
        }
        return this.userMapper.selectDailyUserRegisterStats(from, to);
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

    public HttpStatus updateProfileImage(UserEntity signedUser, MultipartFile image) {
        if (UserService.isInvalidUser(signedUser)) {
            // CommonResult.FAILURE_SESSION_EXPIRED
            return HttpStatus.UNAUTHORIZED;
        }

        if (image.isEmpty() || !image.getContentType().startsWith("image/")) {
            // CommonResult.FAILURE;
            return HttpStatus.BAD_REQUEST;
        }

        String profileUploadDir = uploadDir + "profile/";
        File dir = new File(profileUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String extension = Objects.requireNonNull(image.getOriginalFilename())
                .substring(image.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;

        File file = new File(profileUploadDir, fileName);
        try {
            image.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String imageUrl = "/uploads/profile/" + fileName;

        signedUser.setProfileImg(imageUrl);
        signedUser.setModifiedAt(LocalDateTime.now());
        return this.userMapper.update(signedUser) > 0
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;
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
        user.setLastSignedAt(null);
        user.setLastSignedUa(null);

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

        boolean bUpdateDailyLoginStats = updateDailyLoginStats(dbUser);
        if (!bUpdateDailyLoginStats) {
            //log 처리
        }

        Result result = updateLoginHistory(dbUser, userAgent);

        return ResultTuple.<UserEntity>builder()
                .result(result)
                .payload(dbUser)
                .build();
    }

    public boolean updateDailyLoginStats(UserEntity user) {
        LocalDate today = LocalDate.now();
        if (user.getLastSignedAt() == null || !user.getLastSignedAt().toLocalDate().isEqual(today)) {
            try {
                return this.dailyLoginStatsMapper.incrementUserCount(today) > 0;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Result updateLoginHistory(UserEntity user,
                                     String lastSignedUa) {
        user.setLastSignedAt(LocalDateTime.now());
        user.setLastSignedUa(lastSignedUa);

        return this.userMapper.update(user) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public ResultTuple<Pair<UserDto[], ReviewPageVo>> getUserAll(UserEntity signedUser, int page) {
        if (signedUser == null || !signedUser.isAdmin()) {
            return ResultTuple.<Pair<UserDto[], ReviewPageVo>>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }

        if (page < 1) page = 1;

        int totalCount = this.userMapper.selectCountAllUsers();
        ReviewPageVo reviewPageVo = new ReviewPageVo(10, page, totalCount);
        List<UserDto> list = this.userMapper.selectAllUser(reviewPageVo);
        Pair<UserDto[], ReviewPageVo> data = Pair.of(list.toArray(new UserDto[0]), reviewPageVo);

        return ResultTuple.<Pair<UserDto[], ReviewPageVo>>builder()
                .result(CommonResult.SUCCESS)
                .payload(data)
                .build();
    }


    public Result editUserInfo(UserEntity signedUser, UserDto userDto) {
        if (UserService.isInvalidUser(signedUser)) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        UserDto dbUser = this.userMapper.selectUserById(userDto.getId());
        System.out.println("수정 요청된 ID: " + userDto.getId());
        System.out.println(dbUser);
        if (dbUser == null) {
            System.out.println("1");
            return CommonResult.FAILURE_ABSENT;
        }
        dbUser.setName(userDto.getName());
        dbUser.setNickname(userDto.getNickname());
        dbUser.setGender(userDto.getGender());
        dbUser.setBirth(userDto.getBirth());
        dbUser.setContactMvnoCode(userDto.getContactMvnoCode());
        System.out.println("1" + userDto.getContactMvnoCode());
        dbUser.setContactFirst(userDto.getContactFirst());
        dbUser.setContactSecond(userDto.getContactSecond());
        dbUser.setContactThird(userDto.getContactThird());
        dbUser.setAdmin(userDto.isAdmin());
        dbUser.setDeleted(userDto.isDeleted());
        dbUser.setSuspended(userDto.isSuspended());

        return this.userMapper.editUser(dbUser) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }
}
