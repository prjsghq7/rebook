# Re:Book

독서 경험을 리뷰로 나누고, 리뷰를 바탕으로 책을 찾아볼 수 있는 웹 플랫폼입니다. 알라딘 API로 도서 정보를 불러오고, 회원 · 리뷰 · 관리자 통계 기능을 갖췄습니다.

- 사이트: https://rebook-kimgh.onrender.com/
- 포트폴리오: https://prjsghq7.github.io/ (직접 코딩 › Re:Book)

## 주요 기능

- **도서:** 알라딘 API 기반 베스트셀러 · 신간 · 카테고리별 목록, 검색, 도서 상세, 인기 도서
- **리뷰:** 도서별 리뷰 작성 · 수정 · 삭제
- **회원:** 이메일 인증 회원가입, Google · Kakao · Naver 소셜 로그인, 아이디 · 비밀번호 찾기, 프로필 이미지, 회원 탈퇴
- **독서 챗봇:** OpenAI API 기반 대화방 (방 만들기 · 목록 · 삭제 · 대화 기록)
- **관리자 대시보드:** 일별 가입 · 로그인 · 리뷰 등록 추이, 연령대 · 성별 · 가입 경로 통계 차트

## 맡은 부분

회원 관리, 도서 상세, 리뷰, 관리자 통계를 담당했습니다.

- MariaDB 테이블 설계
- OAuth 2.0 소셜 로그인 (Google · Kakao · Naver)
- 비밀번호 Argon2 암호화 (`Argon2PasswordEncoder`)
- 관리자 통계 API와 차트 연동

## 기술 스택

Java 17 · Spring Boot 3.4 · Spring Security · OAuth2 Client · MyBatis · MariaDB · Thymeleaf · JavaScript · HTML/CSS · Maven

## 실행 방법

`src/main/resources/application.properties`는 저장소에 포함되어 있지 않습니다. 아래 항목을 채운 설정 파일을 만든 뒤 실행합니다.

| 항목 | 설정 키 |
|---|---|
| DB | `spring.datasource.*` |
| 알라딘 API | `api-aladin-api-key` |
| OpenAI | `openai.apiKey` |
| 소셜 로그인 | `spring.security.oauth2.client.registration.{google,kakao,naver}.*` |
| 메일 발송 | `spring.mail.*` |
| 파일 업로드 경로 | `file.upload-dir` |

```bash
./mvnw spring-boot:run
```

## 구조

```text
src/main/java/com/dev/rebook/
├─ controllers/   화면 · API 컨트롤러 (도서 · 리뷰 · 회원 · 관리자 · 챗봇)
├─ services/      비즈니스 로직
├─ mappers/       MyBatis 매퍼 인터페이스
├─ auth/          소셜 로그인 처리
├─ config/        Security · 암호화 설정
└─ entities, dtos, vos, results
src/main/resources/
├─ mappers/       MyBatis SQL
├─ templates/     Thymeleaf 화면 (book · review · user · admin · home)
└─ static/        JS · CSS · 이미지
```

## 팀

3인 팀 프로젝트입니다.
