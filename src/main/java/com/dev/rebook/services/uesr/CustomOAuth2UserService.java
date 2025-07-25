package com.dev.rebook.services.uesr;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId(); // 예: "google"
        if (provider == null) {
            throw new IllegalArgumentException("OAuth2 응답에 'provider'가 존재하지 않습니다.");
        }

        OAuth2User oauthUser = delegate.loadUser(userRequest); // 실제 사용자 정보 요청
        String nameAttributeKey;
        String providerId;
        String email;
        String name;
        String picture;
        switch (provider) {
            case "google":
                nameAttributeKey = "sub";
                providerId = oauthUser.getAttribute(nameAttributeKey);
                email = oauthUser.getAttribute("email");
                name = oauthUser.getAttribute("name");
                picture = oauthUser.getAttribute("picture");
                break;
            case "kakao":
                nameAttributeKey = "id";
                providerId = Long.toString(oauthUser.getAttribute(nameAttributeKey));
                Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
                email = (String) kakaoAccount.get("email");
                Map<String, Object> properties = oauthUser.getAttribute("properties");
                name = (String) properties.get("nickname");
                picture = (String) properties.get("profile_image");
                break;
            case "naver":
                nameAttributeKey = "response";
                Map<String, Object> response = oauthUser.getAttribute(nameAttributeKey);
                providerId = (String) response.get("id");
                email = (String) response.get("email");
                name = (String) response.get("name");
                picture = (String) response.get("profile_image");
                break;
            default:
                nameAttributeKey = null;
                providerId = null;
                email = null;
                name = null;
                picture = null;
        }

        if (providerId == null) {
            throw new IllegalArgumentException("OAuth2 응답에 'providerId'가 존재하지 않습니다.");
        }

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
        // provider 값 추가 (회원 가입시 저장용, OAuth2User의 attribute에 해당값 없음 -> 추가 필요)
        attributes.put("provider", provider);
        attributes.put("providerId", providerId);
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("picture", picture);

        HttpServletRequest httpRequest = getCurrentHttpRequest();
        attributes.put("userAgent", httpRequest.getHeader("User-Agent"));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_TEMP")), //임시회원(추가 입력필요)
                attributes,
                nameAttributeKey
        );
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }
}
