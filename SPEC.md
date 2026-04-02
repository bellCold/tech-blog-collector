# API 스펙

## 도메인 모델

### BlogSource
| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long | PK, auto increment |
| name | String | NOT NULL |
| url | String | NOT NULL, UNIQUE |
| rssUrl | String? | nullable |
| type | SourceType(RSS, CRAWL) | NOT NULL |
| description | String? | max 500 |
| createdAt | LocalDateTime | NOT NULL |
| updatedAt | LocalDateTime | NOT NULL, @PreUpdate |

### BlogPost
| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long | PK, auto increment |
| blogSource | BlogSource | NOT NULL, FK, LAZY |
| title | String | NOT NULL, max 500 |
| content | String? | TEXT |
| summary | String? | max 1000 |
| url | String | NOT NULL, UNIQUE |
| author | String? | max 100 |
| publishedAt | LocalDateTime? | INDEX |
| collectedAt | LocalDateTime | NOT NULL |
| createdAt | LocalDateTime | NOT NULL |

### VisitorLog
| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long | PK, auto increment |
| ipAddress | String | NOT NULL, max 45 |
| visitDate | LocalDate | NOT NULL |
| createdAt | LocalDateTime | NOT NULL |

- `UNIQUE(ipAddress, visitDate)`

---

## API 엔드포인트

### 블로그 소스 `/api/sources`
| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | / | `{name, url, rssUrl?, type, description?}` | BlogSourceResponse (201) |
| GET | / | - | List\<BlogSourceResponse\> |
| GET | /{id} | - | BlogSourceResponse |
| PUT | /{id} | `{name?, url?, rssUrl?, type?, description?}` | BlogSourceResponse |
| DELETE | /{id} | - | 204 |

### 블로그 글 `/api/posts`
| Method | Path | Params | Response |
|--------|------|--------|----------|
| GET | / | sourceId?, page, size | PageResponse\<BlogPostListResponse\> |
| GET | /{id} | - | BlogPostDetailResponse |
| GET | /search | keyword, page, size | PageResponse\<BlogPostListResponse\> |

### 방문자 `/api/visitors`
| Method | Path | 설명 | Response |
|--------|------|------|----------|
| POST | / | 방문 기록 (IP 기반, 하루 1회) | 201 |
| GET | / | 오늘 방문자수 + 총 방문자수 | VisitorResponse |

### 수집 `/api/collector`
| Method | Path | 설명 |
|--------|------|------|
| POST | /run | 전체 수집 트리거 |
| POST | /run/{sourceId} | 특정 소스 수집 |

---

## DTO

**BlogSourceResponse**: `id, name, url, rssUrl, type, description, createdAt, updatedAt`

**BlogPostListResponse**: `id, title, summary, url, author, sourceName, publishedAt`

**BlogPostDetailResponse**: `id, title, content, summary, url, author, source(BlogSourceResponse), publishedAt, collectedAt`

**PageResponse\<T\>**: `content, page, size, totalElements, totalPages`

**VisitorResponse**: `todayCount, totalCount`

**ErrorResponse**: `status, message, timestamp`

---

## 수집 로직

**RSS**: rssUrl 피드 파싱 → HTML 제거 → URL 중복 체크 → 저장
**크롤링**: CSS 셀렉터로 목록/상세 파싱 → URL 중복 체크 → 저장
**스케줄링**: 2시간 주기 (`0 0 */2 * * *`), 실패 시 로그 후 계속

---

## 구현 체크리스트

### Phase 1: 빌드 설정
- [x] build.gradle.kts — plugin.jpa, 의존성 추가
- [x] application.yml — MySQL, JPA 설정

### Phase 2: 도메인/리포지토리
- [x] SourceType enum
- [x] BlogSource 엔티티
- [x] BlogPost 엔티티
- [x] BlogSourceRepository
- [x] BlogPostRepository

### Phase 3: 소스 CRUD API
- [x] DTO (CreateRequest, UpdateRequest, Response, ErrorResponse)
- [x] NotFoundException
- [x] BlogSourceService
- [x] BlogSourceController (POST/GET/PUT/DELETE)
- [x] GlobalExceptionHandler

### Phase 4: 글 조회/검색 API
- [x] DTO (ListResponse, DetailResponse, PageResponse)
- [x] BlogPostService
- [x] BlogPostController (목록/상세/검색)

### Phase 5: RSS 수집기
- [x] RssCollector
- [x] SchedulingConfig
- [x] CollectorScheduler
- [x] CollectorController (수동 트리거)

### Phase 6: 웹 크롤러
- [x] BlogSource 크롤링 셀렉터 필드 추가
- [x] WebCrawlCollector

### Phase 7: 방문자 추적
- [x] VisitorLog 엔티티
- [x] VisitorLogRepository
- [x] VisitorDto (VisitorResponse)
- [x] VisitorService
- [x] VisitorController (POST/GET)
