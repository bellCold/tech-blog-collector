# 방문자수 추적 기능 설계

## 개요

기술블로그 수집 서비스에 오늘 방문자수 / 총 방문자수 기능을 추가한다.
IP 기반으로 하루 1회만 카운트하며, DB(PostgreSQL)에 저장한다.

## 도메인 모델

### VisitorLog

| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long | PK, auto increment |
| ipAddress | String | NOT NULL, max 45 (IPv6 대응) |
| visitDate | LocalDate | NOT NULL |
| createdAt | LocalDateTime | NOT NULL |

- `UNIQUE(ipAddress, visitDate)` — 같은 IP는 하루 1회만 기록

## API 엔드포인트

### 방문자 `/api/visitors`

| Method | Path | 설명 | Response |
|--------|------|------|----------|
| POST | `/api/visitors` | 방문 기록 (IP 추출, 중복 시 무시) | 201 (신규) 또는 200 (이미 존재) |
| GET | `/api/visitors` | 오늘 방문자수 + 총 방문자수 조회 | `VisitorResponse` |

## DTO

**VisitorResponse**: `{ todayCount: Long, totalCount: Long }`

## IP 추출 방식

`X-Forwarded-For` 헤더 우선 → 없으면 `request.remoteAddr` 사용.
Render는 리버스 프록시 뒤이므로 `X-Forwarded-For`의 첫 번째 IP를 파싱한다.

## 컴포넌트 구성

- `VisitorLog` — JPA 엔티티 (일반 class, data class 금지)
- `VisitorLogRepository` — Spring Data JPA
- `VisitorService` — 방문 기록 + 조회 로직
- `VisitorController` — POST/GET 엔드포인트

## 결정 사항

- **식별 기준**: IP 기반 (공유 IP 부정확성은 서비스 규모상 허용)
- **저장소**: PostgreSQL (Render 환경에서 서버 슬립/재배포 시 데이터 유실 방지)
- **API 형태**: 단일 조회 엔드포인트 (`todayCount` + `totalCount` 한 번에 응답)
- **기록 시점**: 프론트에서 `POST /api/visitors` 명시적 호출 (내부 API 노이즈 방지)
