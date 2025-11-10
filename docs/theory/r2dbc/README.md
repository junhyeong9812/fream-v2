# R2DBC README

## 1. 개요

R2DBC(Reactive Relational Database Connectivity)는 JDBC의 **블로킹 I/O** 문제를 해결하기 위해 설계된 **논블로킹, 비동기 SQL 데이터 접근 규약**이다. Reactor 기반으로 동작하며, 데이터베이스와의 통신을 **Flux/Mono 스트림** 형태로 처리한다.

WebFlux 환경과 자연스럽게 연결되며, 아래 특징을 가진다:

* 커넥션 획득 비동기
* 쿼리 실행 비동기
* 결과 행(Row) 스트리밍 처리
* Backpressure 지원

JDBC는 호출 시 스레드를 블로킹시키지만, R2DBC는 **이벤트 기반으로 응답이 도착하면 연산을 재개한다.**

---

## 2. ConnectionFactory 설정

Spring Boot에서는 `application.yml` 또는 Bean 정의로 ConnectionFactory를 설정한다.

```java
@Configuration
public class R2dbcConfig {

  @Bean
  public ConnectionFactory connectionFactory() {
    return ConnectionFactories.get("r2dbc:pool:mysql://user:pw@localhost:3306/app");
  }
}
```

`r2dbc:pool:` 을 사용하면 커넥션 풀 적용이 가능하다.

---

## 3. DatabaseClient 사용

DatabaseClient는 SQL을 Reactor 스트림 형태로 다루는 API이다.

```java
@Service
@RequiredArgsConstructor
public class UserRepository {

  private final DatabaseClient client;

  public Mono<User> findById(String id) {
    return client.sql("SELECT * FROM users WHERE id = :id")
      .bind("id", id)
      .map((row, meta) -> mapUser(row))
      .one(); // 결과 단일 → Mono
  }

  public Flux<User> findAll() {
    return client.sql("SELECT * FROM users")
      .map((row, meta) -> mapUser(row))
      .all(); // 결과 다건 → Flux
  }
}
```

### 결과 해석 방식

| 연산         | 반환        | 의미          |
| ---------- | --------- | ----------- |
| `.one()`   | `Mono<T>` | 0 또는 1개의 결과 |
| `.first()` | `Mono<T>` | 첫 번째 행만 가져옴 |
| `.all()`   | `Flux<T>` | 다건 결과 스트리밍  |

---

## 4. Transaction (반응형 트랜잭션)

스프링에서 R2DBC 트랜잭션은 **ReactiveTransactionManager** 로 처리된다.

```java
@Service
@RequiredArgsConstructor
public class TransferService {

  private final R2dbcEntityTemplate template;

  @Transactional
  public Mono<Void> transfer(String from, String to, long amount) {
    return template.selectOne(query(where("id").is(from)), Account.class)
      .zipWith(template.selectOne(query(where("id").is(to)), Account.class))
      .flatMap(tuple -> applyTransfer(tuple.getT1(), tuple.getT2(), amount))
      .then(); // 결과 없음 → Mono<Void>
  }
}
```

R2DBC 트랜잭션은 **Mono/Flux 체인 안에서만 유지**되며, `.block()` 또는 `.subscribe()` 로 분기하면 트랜잭션 범위가 끊어진다.

---

## 5. Row Streaming과 Backpressure

R2DBC는 결과를 한 번에 모두 메모리로 가져오지 않고, DB 응답을 **행(Row) 단위 스트림**으로 전달한다.

Reactor 구독자가 `request(n)` 을 통해 수요(demand)를 통지하면, DB 드라이버는 그만큼만 데이터를 push 한다.

이 방식은 **대량 결과 조회 시 메모리 폭주(OOM)를 방지**한다.

---

## 6. 주의 사항

| 항목                            | 이유                          | 해결 방법                   |
| ----------------------------- | --------------------------- | ----------------------- |
| JDBC 사용 금지                    | JDBC는 블로킹 → EventLoop 정지 위험 | R2DBC 드라이버 사용           |
| `.block()` 금지                 | 체인 중단 + 트랜잭션 범위 깨짐          | 끝까지 Mono/Flux 유지        |
| 대량 조회 시 `collectList()` 남발 금지 | 메모리 급증 가능                   | 가능한 경우 **Flux 스트리밍** 유지 |

블로킹이 불가피할 경우:

```java
Mono.fromCallable(this::jdbcCall)
    .subscribeOn(Schedulers.boundedElastic());
```

---

## 7. 요약

* R2DBC는 논블로킹 SQL 데이터 접근을 제공한다.
* 결과는 Mono/Flux 스트림 형태로 전달된다.
* 트랜잭션은 Reactor 체인 내에서만 유지된다.
* 대용량 처리 시 Backpressure 기반 Row streaming으로 메모리 안전성이 높다.
* WebFlux 환경과 자연스럽게 통합된다.
