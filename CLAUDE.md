# 기술블로그 수집 API 서버

## 스펙
구현 전 반드시 확인: @SPEC.md

## 빌드 & 실행
- 빌드: `./gradlew build`
- 실행: `./gradlew bootRun`
- 테스트: `./gradlew test`

## 코드 컨벤션
- JPA 엔티티는 일반 class 사용 (data class 금지 — equals/hashCode, lazy proxy 문제)
- DTO는 data class 사용

## API 규칙
- 기본 경로: `/api`
- 생성 응답: 201 Created
- 삭제 응답: 204 No Content
- 에러 응답: `ErrorResponse(status, message, timestamp)` 형식 통일

## 주의사항
- RSS 피드의 HTML 태그는 `Jsoup.parse(html).text()`로 제거
- 블로그 글 수집 시 URL 중복 체크 필수 (`existsByUrl`)
- 수집 실패 시 해당 소스만 스킵하고 나머지 계속 처리
- 크롤링 시 User-Agent 설정 및 요청 간 딜레이 필수
