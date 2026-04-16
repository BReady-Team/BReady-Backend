# 🔄 BReady

<h3 align="center">
  계획이 틀어진 그 순간, B안으로 즉시 전환하는 스마트 일정 플래너
</h3>

<br />

<p align="center">
  <img width="800" alt="BReady Landing" src="https://github.com/user-attachments/assets/fbf0727a-492b-4c84-8332-24403e524613" />
</p>


<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring Boot-3.5.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring AI-1.1.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white" />
</p>

---

## 📚 목차

- [프로젝트 소개](#-프로젝트-소개)
- [핵심 개념](#-핵심-개념)
- [시스템 아키텍처](#️-시스템-아키텍처)
- [ERD](#-erd)
- [기술 스택](#️-기술-스택)
- [핵심 기능 소개](#-핵심-기능-소개)
- [팀원 구성](#-팀원-구성)

---

## 📝 프로젝트 소개

**BReady**는 *"하루는 항상 변수로 가득하다"* 는 전제에서 출발합니다.

비가 오거나, 장소가 혼잡하거나, 갑자기 피로해지거나, 영업이 중단되는 상황처럼
**계획이 틀어지는 트리거(Trigger)** 가 발생했을 때,
사람들은 급하게 검색하고 대충 찾고 이동을 반복하며 시간을 낭비합니다.

> BReady는 이 문제를 해결합니다.
> **A안과 B안(대체 플랜)을 함께 준비**해두고,
> 트리거가 발생하는 순간 AI 추천과 함께 **즉시 B안으로 전환**할 수 있는 서비스입니다.

### 💡 왜 BReady인가?

| 기존 방식 | BReady |
|:---:|:---:|
| 계획이 틀어지면 → 급검색 → 대충 선택 → 만족도 하락 | 트리거 발생 → 즉시 B안 전환 → AI 추천 장소 선택 |
| 장소/코스 추천 중심 | **코스 전환(Switch) 추천** 중심 |
| 대체 플랜이 없음 | A안 + B안 세트로 사전 준비 |
| 변수를 고려하지 않음 | **트리거 조건**이 데이터 모델에 존재 |

---

## 🧩 핵심 개념

| 개념 | 설명 |
|------|------|
| **PlanBundle** | 한 하루의 일정. 카테고리별 활동과 후보 장소들의 묶음 |
| **Trigger (트리거)** | 전환 사유 카테고리 — 날씨 악화 / 거리 부담 / 대기시간 과다 / 영업 중단 / 체력 저하 등 |
| **후보 장소** | 각 활동 카테고리에 미리 등록해두는 대체 장소 목록 |
| **트리거 대응** | 트리거 발생 시 그대로 유지 / 카테고리 변경 / 장소 변경 중 선택 |
| **SwitchLog** | 실제로 전환한 기록 (트리거 종류 + 전환 여부) → 통계 및 추천 고도화 기반 |

---

## 🖼️ 시스템 아키텍처


```
                        ┌─────────────────────────────┐
                        │    Client  (React)           │
                        └──────────────┬──────────────┘
                                  HTTPS│
                        ┌─────────────▼──────────────┐
                        │     Nginx Reverse Proxy      │
                        │  (Blue :8081 / Green :8082)  │
                        └──────────────┬──────────────┘
                                       │
               ┌───────────────────────▼──────────────────────────┐
               │              Spring Boot Application               │
               │                                                    │
               │  ┌─────────────────┐   ┌────────────────────────┐ │
               │  │ Spring Security  │   │      Spring AI         │ │
               │  │  JWT + OAuth2    │   │  → OpenAI API          │ │
               │  └─────────────────┘   └────────────────────────┘ │
               │  ┌─────────────────┐   ┌────────────────────────┐ │
               │  │ Spring Data JPA  │   │      WebClient         │ │
               │  │  → MySQL 8.0     │   │  → Kakao Map API       │ │
               │  └─────────────────┘   └────────────────────────┘ │
               │  ┌─────────────────┐   ┌────────────────────────┐ │
               │  │ Spring Data Redis│   │  Spring Boot Actuator  │ │
               │  │  → Redis 7.2    │   │  → Prometheus Metrics  │ │
               │  └─────────────────┘   └────────────────────────┘ │
               │  ┌─────────────────┐                               │
               │  │    AWS SDK      │                               │
               │  │    → S3         │                               │
               │  └─────────────────┘                               │
               └───────────────────────────────────────────────────┘
                      │              │               │
           ┌──────────▼──┐  ┌───────▼──────┐  ┌───▼──────────────┐
           │  MySQL 8.0   │  │  Redis 7.2   │  │    AWS S3        │
           │ (mem: 600m)  │  │ (mem: 192m)  │  │  Object Storage  │
           └─────────────┘  └──────────────┘  └──────────────────┘
                                   │
                        ┌──────────▼───────────┐
                        │ Grafana + Prometheus  │
                        │  (서버 메트릭 모니터링) │
                        └──────────────────────┘

  ──────────────────── CI/CD Pipeline ────────────────────
  GitHub Push → GitHub Actions → GHCR Image Build & Push
       → EC2 SSH → deploy.sh (Blue/Green Switch)
  ─────────────────────────────────────────────────────────
```

---

## 🗄️ ERD

> <img width="716" height="458" alt="ERD" src="https://github.com/user-attachments/assets/ea6ed8d3-a8d0-4859-96f5-9b956c3a70ab" />


---

## 🛠️ 기술 스택

### Backend

| Category | Stack | 도입 이유 |
|----------|-------|----------|
| Framework | ![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.7-6DB33F?style=flat-square&logo=springboot&logoColor=white) | REST API 기반 서비스 설계 및 안정적인 운영 환경 구성 |
| Language | ![Java](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white) | Record, Virtual Thread 등 최신 문법 활용 및 LTS 안정성 확보 |
| Security | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) | JWT 기반 인증 및 역할별 접근 제어 분리 |
| Authentication | ![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white) | 무상태(Stateless) 인증으로 확장성 확보 |
| ORM | ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white) | 객체 중심 DB 접근으로 생산성 향상 |
| Cache | ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | Refresh Token 저장 및 세션 관리 |
| AI | ![Spring AI](https://img.shields.io/badge/Spring_AI_1.1.0-6DB33F?style=flat-square&logo=spring&logoColor=white) | 트리거 발생 시 대체 활동/장소 AI 추천 생성 |
| HTTP Client | ![WebClient](https://img.shields.io/badge/WebClient_(WebFlux)-6DB33F?style=flat-square&logo=spring&logoColor=white) | 외부 API(카카오 지도 등) 비동기 호출 처리 |
| API Docs | ![Swagger](https://img.shields.io/badge/SpringDoc_OpenAPI_2.8.9-85EA2D?style=flat-square&logo=swagger&logoColor=black) | API 명세 자동화 및 팀 내 협업 효율화 |
| Monitoring | ![Actuator](https://img.shields.io/badge/Actuator_+_Prometheus-E6522C?style=flat-square&logo=prometheus&logoColor=white) | 서버 메트릭 수집 및 상태 모니터링 |
| Validation | ![Validation](https://img.shields.io/badge/Spring_Validation-6DB33F?style=flat-square&logo=spring&logoColor=white) | 요청 데이터 유효성 검증 표준화 |
| AOP | ![AOP](https://img.shields.io/badge/Spring_AOP-6DB33F?style=flat-square&logo=spring&logoColor=white) | 로깅·인증 처리 등 횡단 관심사 분리 |

### Database & Infrastructure

| Category | Stack | 도입 이유 |
|----------|-------|----------|
| RDBMS | ![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=flat-square&logo=mysql&logoColor=white) | 플랜·장소·전환 기록 등 정형 데이터 안정적 관리. Materialized View로 통계 성능 최적화 |
| In-Memory DB | ![Redis](https://img.shields.io/badge/Redis_7.2-DC382D?style=flat-square&logo=redis&logoColor=white) | Refresh Token 저장, LRU 정책으로 메모리 효율 관리 (`maxmemory 128mb`) |
| In-Memory DB (Test) | ![H2](https://img.shields.io/badge/H2_(Test)-004088?style=flat-square&logo=h2&logoColor=white) | 로컬 테스트 환경 빠른 DB 시뮬레이션 |
| Object Storage | ![S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white) | 이미지 등 정적 파일 저장 및 Presigned URL 제공 |
| Container | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) | Blue/Green 각 인스턴스를 독립 컨테이너로 격리, 환경 일관성 확보 |
| CI/CD | ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white) | Push 이벤트 감지 → GHCR 이미지 빌드·푸시 → EC2 SSH 배포 자동화 |
| Container Registry | ![GHCR](https://img.shields.io/badge/GHCR-181717?style=flat-square&logo=github&logoColor=white) | GitHub Container Registry로 이미지 중앙 관리 (`ghcr.io/bready-team/bready-backend`) |
| Reverse Proxy | ![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white) | HTTPS 처리 및 Blue/Green upstream 포트 전환 (`sed` 기반 무중단 reload) |
| Load Testing | ![k6](https://img.shields.io/badge/k6-7D64FF?style=flat-square&logo=k6&logoColor=white) | JOIN vs Materialized View 응답 성능 비교 부하테스트 (최대 80 VUs) |
| Monitoring | ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=flat-square&logo=prometheus&logoColor=white) ![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=grafana&logoColor=white) | Actuator 메트릭 수집 → Grafana 대시보드로 실시간 서버 상태 시각화 |

### Frontend & Auth

| Category | Stack |
|----------|-------|
| Frontend Framework | ![React](https://img.shields.io/badge/React_/_Next.js-000000?style=flat-square&logo=nextdotjs&logoColor=white) |
| Map | ![Kakao](https://img.shields.io/badge/Kakao_Map_API-FFCD00?style=flat-square&logo=kakao&logoColor=black) |
| Social Login | ![Kakao](https://img.shields.io/badge/Kakao_OAuth2-FFCD00?style=flat-square&logo=kakao&logoColor=black) ![Naver](https://img.shields.io/badge/Naver_OAuth2-03C75A?style=flat-square&logo=naver&logoColor=white) |

---

## ✨ 핵심 기능 소개

### 🔐 회원가입 & 로그인

이메일/비밀번호 로그인과 카카오·네이버 소셜 로그인을 지원합니다.

| 회원가입 | 로그인 |
|:---:|:---:|
| ![Landing](https://github.com/user-attachments/assets/3a9a3323-d056-4906-9b55-8393be9b1f88) | ![Login](https://github.com/user-attachments/assets/99f8a347-91e5-4c72-a0a6-6bdf11854a3c) |

---


### 📋 플랜 생성 & 관리

제목·날짜·지역을 입력해 새 플랜을 만들고, 활동 카테고리(식사, 카페 등)별로 **대표 장소와 후보 장소**를 등록합니다.
카카오 지도 API와 연동하여 주변 장소를 검색하고 바로 추가할 수 있습니다.

| 플랜 생성 | 플랜 상세 | 장소 검색 (카카오 지도) |
|:---:|:---:|:---:|
| ![New Plan](https://github.com/user-attachments/assets/1445e367-9105-44aa-850a-aee9c5475406) | ![Plan Detail](https://github.com/user-attachments/assets/589f0abf-05e7-4885-b60a-7ca2ca6d78bb) | ![Place Search](https://github.com/user-attachments/assets/50c7df10-f5e2-464c-a7e3-a4e898871168) |

---



### ⚡ 트리거 대응 — 핵심 기능

**트리거 발생** 버튼을 누르면 3단계 전환 플로우가 시작됩니다.

**① 상황 선택** — 날씨 악화 / 거리 부담 / 대기시간 과다 / 영업 중단 / 체력 저하 중 현재 상황을 선택합니다.

**② 대응 방식 선택** — 그대로 유지 / 카테고리 변경 / 장소 변경 중 원하는 전환 방식을 선택합니다.

**③ AI 추천** — 선택한 트리거와 상황을 바탕으로 AI가 최적의 대체 활동 또는 대체 장소를 추천합니다.

| ① 트리거 감지 | ② 카테고리 변경 + AI 추천 | ③ 장소 변경 (지도 + AI) |
|:---:|:---:|:---:|
| ![Trigger](https://github.com/user-attachments/assets/b08c71fa-a683-4f37-915c-c67c2e6d59a6) | ![Category AI](https://github.com/user-attachments/assets/d16038b3-15e1-4417-aea1-7a3278d7e4a5) | ![Map AI](https://github.com/user-attachments/assets/9e5533fb-c646-41ea-b9f2-6eaff50a9554) |

---


### 📊 통계 & 마이페이지

내 플랜들의 전환 기록을 분석해 **트리거 발생 빈도·패턴**을 시각화합니다.
이번 주 / 이번 달 / 전체 기간별 필터링을 지원하며, 최근 전환 활동을 한눈에 확인할 수 있습니다.

| 통계 (Stats) | 마이페이지 |
|:---:|:---:|
| ![Stats](https://github.com/user-attachments/assets/aa51fb46-d654-499a-8e55-102910dc5545) | ![Profile](https://github.com/user-attachments/assets/175f016f-655f-43b9-b4d9-3c44547a5a22) |

---

## ⚙️ 인프라 & 배포

### 🐳 Docker 컨테이너 구성

서비스는 역할별로 컨테이너를 분리해 관리합니다.

```
bready_default (Docker network)
│
├── bready-api-blue   (port 8081)  ─┐
├── bready-api-green  (port 8082)  ─┤ Blue/Green 교대 운영
│                                   │
├── bready-mysql      (port 3306)  ─┤ mem_limit: 600m
│   └── innodb-buffer-pool: 256M   │ volumes: mysql-data
│                                   │
└── bready-redis      (port 6379)  ─┘ mem_limit: 192m
    └── maxmemory: 128mb, LRU policy
```

**JVM 튜닝** — t계열 저사양 EC2 환경을 고려해 컨테이너당 메모리를 768m로 제한하고 JVM 옵션을 명시합니다.

```
JAVA_TOOL_OPTIONS: "-Xms256m -Xmx384m -XX:MaxMetaspaceSize=256m"
```

---

### 🔵🟢 블루/그린 무중단 배포

서비스 중단 없이 새 버전을 배포하기 위해 **Blue(8081) / Green(8082)** 두 슬롯을 교대로 사용합니다.

```
배포 플로우
─────────────────────────────────────────────────────────────
  GitHub Push
      │
      ▼
  GitHub Actions
  ├── Docker 이미지 빌드
  └── GHCR(ghcr.io/bready-team/bready-backend)에 Push
      │
      ▼ SSH
  EC2 deploy.sh 실행
      │
      ├── [STEP 1] 비활성 슬롯의 최신 이미지 Pull
      │
      ├── [STEP 2] 비활성 슬롯 컨테이너 Start
      │
      ├── [STEP 3] Health Check (/actuator/health)
      │             최대 30회 × 10s = 300s 대기
      │             → 실패 시 배포 중단 및 로그 출력
      │
      ├── [STEP 4] Nginx upstream 포트 전환 (sed)
      │             server 127.0.0.1:808X → 새 포트로 교체
      │
      ├── [STEP 5] nginx -t 설정 검증
      │
      ├── [STEP 6] systemctl reload nginx  ← 무중단 전환
      │
      ├── [STEP 7] 기존 슬롯 컨테이너 Stop & Remove
      │
      └── [STEP 8] dangling 이미지 정리
─────────────────────────────────────────────────────────────
```

> **핵심 포인트** — Nginx `reload`는 기존 커넥션을 끊지 않고 worker 프로세스만 교체합니다.
> Health Check를 통과한 이후에 upstream을 바꾸기 때문에 **요청 유실 없이 버전 전환**이 가능합니다.

---

### 📊 Prometheus & Grafana 모니터링

Spring Boot Actuator가 노출하는 `/actuator/prometheus` 엔드포인트를 Prometheus가 수집하고, Grafana 대시보드로 시각화합니다.

```
Spring Boot Actuator
  └── /actuator/prometheus
          │
          ▼
      Prometheus
    (메트릭 수집 & 저장)
          │
          ▼
       Grafana
    (JVM 힙 · GC · HTTP 응답시간 · DB 커넥션 풀 대시보드)
```

---

## 📈 성능 테스트 (k6)

### 테스트 배경

통계 API(`/api/v1/stats/plans`)의 **JOIN 쿼리 방식**과 **Materialized View 방식**의 성능 차이를 k6 부하테스트로 검증했습니다.

### 테스트 시나리오

두 시나리오를 동시에 실행해 같은 부하 조건에서 응답 시간을 비교합니다.

```
VUs
 80 ┤          ████████████
    │        ██            ██
 30 ┤      ██                ██
    │    ██                    ██
  0 ┼───┴──────────────────────┴───▶ time
     0s  30s      1m30s      2m
```

| 항목 | 내용 |
|------|------|
| 최대 VUs | 80 |
| 구간 | Ramp-up 30s → 유지 1m → Ramp-down 30s |
| 인증 | Bearer Token (환경변수 `ACCESS_TOKEN`) |

### 성능 임계값 (Thresholds)

| 시나리오 | 에러율 | p95 응답시간 목표 |
|---------|-------|----------------|
| JOIN 쿼리 | < 1% | **< 2,500ms** |
| Materialized View | < 1% | **< 200ms** |

> Materialized View에 대해 JOIN 대비 **12.5배** 엄격한 기준을 적용

### 테스트 결과 요약

```
┌─────────────────────────────────────────────────────────┐
│            k6 Load Test Result (80 VUs)                 │
├─────────────────────────┬───────────────────────────────┤
│  JOIN Query             │  Materialized View             │
│  p95: ~2,200ms          │  p95: ~85ms                    │
│  에러율: 0%               │  에러율: 0%                    │
│  threshold: PASS ✓      │  threshold: PASS ✓             │
└─────────────────────────┴───────────────────────────────┘
  → Materialized View가 JOIN 대비 약 25배 빠른 응답
```

### 개선 포인트

집계성 통계 쿼리(기간별 플랜·전환 횟수 집계)는 매 요청마다 대량의 JOIN·GROUP BY를 수행하는 구조입니다.
**Materialized View**를 도입해 미리 집계된 결과를 저장하고, 조회 시에는 단순 SELECT만 수행하도록 개선했습니다.

```sql
-- Before: 매 요청마다 실행되는 집계 JOIN
SELECT p.id, COUNT(sl.id) as switch_count
FROM plan p
LEFT JOIN switch_log sl ON sl.plan_id = p.id
WHERE sl.created_at >= :from
GROUP BY p.id
ORDER BY switch_count DESC;

-- After: Materialized View 단순 조회
SELECT * FROM stats_plan_materialized
WHERE period = :period
ORDER BY switch_count DESC
LIMIT :limit;
```
---

## 🔧 성능 개선

### 1. 통계 API 성능 개선 — JOIN → LIMIT Push Down → Materialized View

#### 📌 문제 상황

통계 API(`GET /api/v1/stats/plans`)가 80 VUs 부하 환경에서 **완전히 응답 불가** 상태에 빠지는 현상이 확인되었습니다.

```
k6 결과 (JOIN, 80 VUs)
─────────────────────────────────────
p95          : 10s
실패율       : 100% (493 / 493)
─────────────────────────────────────
HikariPool-1 - Connection is not available,
request timed out after 30002ms
(total=10, active=10, idle=0, waiting=2)
```

#### 🔍 원인 분석

Hibernate SQL 로그 및 `EXPLAIN ANALYZE` 결과, 핵심 병목은 **50만 row가 생성된 이후 GROUP BY가 수행되는 구조**였습니다.

```
plans 전체 조회 (~2,000건)
   ↓ LEFT JOIN triggers / decisions / switch_logs
   ↓ ~50만 row 생성
   ↓ GROUP BY
   ↓ 정렬
   ↓ LIMIT 10  ← LIMIT이 가장 마지막에 적용
```

```
MySQL EXPLAIN ANALYZE
─────────────────────────────────────
Nested loop left join  (actual rows=501,000)
Aggregate using temporary table  (actual time=4,990ms)
Table scan on <temporary>
```

#### ✅ 1차 개선 — LIMIT Push Down (Native Query)

서브쿼리로 먼저 `LIMIT 10`을 적용한 뒤 JOIN을 수행하도록 쿼리 구조를 변경했습니다.

```sql
-- Before: LIMIT이 GROUP BY 이후에 적용
SELECT p.id, p.title, COUNT(sl.id)
FROM plans p
LEFT JOIN triggers t ON t.plan_id = p.id
LEFT JOIN decisions d ON d.trigger_id = t.id
LEFT JOIN switch_logs sl ON sl.decision_id = d.id
WHERE p.owner_id = ?
GROUP BY p.id
ORDER BY p.plan_date DESC
LIMIT 10;

-- After: 서브쿼리로 LIMIT 먼저 적용
SELECT p.id, p.title, COALESCE(COUNT(sl.id), 0)
FROM (
    SELECT id, title, plan_date, region
    FROM plans
    WHERE owner_id = ?
    ORDER BY plan_date DESC
    LIMIT 10
) p
LEFT JOIN triggers t ON t.plan_id = p.id
LEFT JOIN decisions d ON d.trigger_id = t.id
LEFT JOIN switch_logs sl ON sl.decision_id = d.id
GROUP BY p.id, p.title, p.plan_date, p.region
ORDER BY p.plan_date DESC;
```

```
1차 개선 결과
─────────────────────────────────────
Before : 5,506ms
After  :   138ms  → 약 40배 개선
```

#### ✅ 2차 개선 — Materialized View 패턴 도입

1차 개선 후에도 부하 환경에서 **500ms ~ 2,000ms**의 변동이 남아 있었습니다.
실시간 집계 구조 자체를 제거하고, **사전 계산된 통계 테이블(`plan_stats`)을 조회**하는 방식으로 전환했습니다.

```
설계 방식
─────────────────────────────────────
1. plan_stats 통계 테이블 생성
2. 이벤트 기반 통계 갱신 구조
3. @TransactionalEventListener 적용
   → 트랜잭션 커밋 이후에만 통계 갱신
4. @Async 비동기 처리
```

```
2차 개선 결과 (동시 요청 상황)
─────────────────────────────────────
JOIN         : 1,843ms ~ 1,996ms
Materialized :    19ms ~    21ms  → 약 95배 개선
```

#### 📊 k6 최종 비교 결과 (80 VUs)

| 항목 | JOIN | Materialized View |
|------|------|-------------------|
| p95 응답시간 | 2,040ms | **< 200ms** |
| 에러율 | 0% | 0% |
| Threshold | ✅ PASS | ✅ PASS |

---

### 2. 추천 API 성능 개선 — Redis 캐시 도입

#### 📌 문제 상황

트리거 발생 시 호출되는 추천 API에서 **AI rerank 단계**가 전체 응답 지연의 대부분을 차지했습니다.

```
개선 전
─────────────────────────────────────
Category Recommendation : 3,646ms
Place Recommendation    : 7,673ms
```

#### 🔍 원인 분석

매 요청마다 OpenAI API를 호출해 AI rerank를 수행하는 구조로, 동일한 트리거·위치 조건의 반복 요청에도 AI 호출이 발생했습니다.

#### ✅ 개선 — Redis 캐시 적용

동일 조건의 요청은 Redis에서 즉시 반환하고, 캐시 미스 시에만 AI 호출을 수행하도록 변경했습니다.

```
캐시 키 설계
─────────────────────────────────────
Category 추천 : triggerId
Place 추천    : category + lat/lng (소수점 반올림)

적용 흐름
─────────────────────────────────────
요청 수신
   ↓
Redis 캐시 확인
   ├── Cache Hit  → 즉시 반환
   └── Cache Miss → AI 호출 후 결과 저장 → 반환
```

#### 📊 개선 결과

| 항목 | 개선 전 | 개선 후 | 개선율 |
|------|---------|---------|--------|
| Category 추천 | 3,646ms | **21ms** | 약 174배 |
| Place 추천 | 7,673ms | **29ms** | 약 264배 |

---

### 📁 프로젝트 구조
```
src/main/java/com/bready/server/
│
├── auth/                        # 소셜 로그인 (카카오·네이버 OAuth2)
│   ├── client/                  # 소셜 로그인 API 클라이언트
│   ├── config/                  # OAuth2 설정
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── exception/
│   ├── repository/
│   └── service/
│
├── global/                      # 공통 모듈
│   ├── aop/                     # 로깅 등 횡단 관심사
│   ├── auth/                    # 인증 컨텍스트
│   ├── config/
│   │   ├── redis/               # Redis 설정
│   │   ├── s3/                  # AWS S3 설정
│   │   ├── security/
│   │   │   └── jwt/             # JWT 발급·검증
│   │   └── swagger/             # API 문서 설정
│   ├── entity/                  # BaseEntity 등 공통 엔티티
│   ├── exception/               # 글로벌 예외 처리
│   └── response/                # 공통 응답 포맷
│
├── place/                       # 장소 검색 (카카오 지도 API)
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   │   └── kakao/               # 카카오 API 응답 DTO
│   ├── exception/
│   ├── external/                # 카카오 지도 외부 API 연동
│   ├── repository/
│   └── service/
│
├── plan/                        # 플랜 CRUD
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── exception/
│   ├── repository/
│   └── service/
│
├── recommendation/              # AI 추천 (Spring AI + OpenAI)
│   ├── adapter/                 # 외부 AI 어댑터
│   ├── ai/                      # AI 프롬프트·호출 로직
│   ├── controller/
│   ├── dto/
│   ├── exception/
│   ├── port/                    # 포트·어댑터 인터페이스
│   └── service/
│
├── s3/                          # 파일 업로드 (AWS S3)
│   ├── controller/
│   ├── dto/
│   ├── exception/
│   └── service/
│
├── stats/                       # 트리거 통계·분석
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── event/                   # 전환 이벤트 발행
│   ├── exception/
│   ├── listener/                # 이벤트 리스너 (SwitchLog 집계)
│   ├── repository/
│   └── service/
│
├── trigger/                     # 트리거 대응 플로우
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── exception/
│   ├── repository/
│   └── service/
│
└── user/                        # 회원 관리
    ├── controller/
    ├── domain/
    ├── dto/
    ├── exception/
    ├── repository/
    └── service/
```

## 😎 팀원 구성 (Server)
| <img width="240" height="266" alt="설아 프로필" src="https://avatars.githubusercontent.com/u/192125242?v=4" /> | <img width="240" height="266" alt="승인 프로필" src="https://avatars.githubusercontent.com/u/144124353?s=400&u=9bda70cb07b771d6301ac64df65acb931406b09e&v=4" /> | 
| :---: | :---: | 
| [민설아](https://github.com/mymy1023) | [유승인](https://github.com/seung-in-Yoo) |
| Backend | Backend |
