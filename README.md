# Fream-v2 프로젝트 개요

Fream-v2는 기존 Fream(v1) 프로젝트를 객체지향적인 도메인 중심 설계와 WebFlux + R2DBC 기반의 비동기 논블로킹 구조로 재구현하는 프로젝트입니다. 기존 MVC + JPA 기반 구조는 도메인 간 강한 결합과 트래픽 확장성의 한계가 존재했습니다. 본 프로젝트는 해당 구조적 한계를 개선하고, 향후 MSA로 확장 가능한 아키텍처 기반을 마련하는 것을 목표로 합니다.

---

## 기술 스택

* Spring WebFlux (논블로킹 HTTP 요청 처리)
* Spring Data R2DBC (Reactive 기반 데이터 접근)
* H2 Database (개발 환경 데이터베이스)
* Lombok, Validation 등 개발 편의성 도구

---

## 프로젝트 설계 목표

1. 글로벌 레벨 예외 처리 설계 및 통합 예외 응답 방식 정의
2. 요청 및 응답 객체 포맷 통일 (API Response Specification 확립)
3. 이벤트 기반 처리 구조 설계 (도메인 이벤트 흐름 정립)
4. 인증(Auth) 도메인 설계 및 구현
5. 사용자(User) 도메인 설계 및 구현
6. 상품(Product) 도메인 설계 및 구현
7. 판매(Sell) 도메인 설계 및 구현
8. 구매(Buy) 도메인 설계 및 구현

추후 확장 예정: 공지, 알림, 로그, 관리 도메인 등

---

## 패키지 구조

```
xyz.fream.api
 ├─ global                  # 공통 (예외, 응답, 보안 등)
 └─ domain
     ├─ user
     │   ├─ model           # Aggregate Root, Entity, VO, Domain Event
     │   ├─ service
     │   │   ├─ command     # 쓰기/상태 변경 Use Case
     │   │   └─ query       # 조회 전용 Use Case
     │   ├─ port
     │   │   ├─ inbound     # 도메인으로 들어오는 인터페이스 (Use Case 포트)
     │   │   └─ outbound    # 도메인이 외부로 호출하는 인터페이스 (Repository 등)
     │   └─ adapters
     │       ├─ web         # Inbound Adapter (Controller)
     │       └─ infrastructure
     │           ├─ persistence  # Outbound Adapter (R2DBC Repository 구현체)
     │           └─ external     # 외부 API / Event Broker Adapter
     │
     ├─ product
     │   └─ (동일 구조 반복)
     ├─ sell
     │   └─ (동일 구조 반복)
     └─ buy
         └─ (동일 구조 반복)
```

패키지의 핵심 포인트:

* `domain` 은 시스템의 중심이며 외부 기술에 의존하지 않음
* `port` 는 도메인의 **의도가 드러나는 인터페이스**
* `adapters` 는 해당 포트를 구현하는 **구현체 계층**으로 Web / DB / 외부 API 등이 위치
* `global` 은 공통 정책과 규칙을 관리하며 domain 에 영향 없이 교체 가능
