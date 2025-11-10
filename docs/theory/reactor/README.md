# Reactor README

## 1. Reactor 개요

Reactor는 JVM 환경에서 비동기 논블로킹 처리를 위해 만들어진 반응형 스트림 구현체이다. 데이터는 즉시 존재하지 않아도, 이후에 생성되었을 때 이어서 처리될 작업을 정의하고 재개(Resume)할 수 있도록 한다.

핵심 개념은 다음과 같다:

* 비동기 데이터 흐름 구성
* 논블로킹 실행
* Backpressure(요청자 수요 조절) 지원

## 2. Mono / Flux

| 타입      | 설명                    | 예시                       |
| ------- | --------------------- | ------------------------ |
| Mono<T> | 0 또는 1개의 값을 비동기적으로 처리 | 단일 조회, 로그인 응답 등          |
| Flux<T> | 0개 이상 연속된 값 스트림       | 채팅, SSE 스트림, 쿼리 결과 다건 조회 |

이들은 값을 저장하는 객체가 아니라, **값이 준비되었을 때 이어서 실행할 처리 파이프라인**을 기록하는 객체이다. 실행은 구독 시점에 시작된다.

## 3. 주요 연산자

* map: 값 변환
* flatMap: 비동기 값 변환(Mono → Mono, Flux → Flux)
* switchMap / concatMap: 순서/취소 제어
* zip / merge / combineLatest: 여러 스트림 결합
* onErrorResume / onErrorReturn: 에러 처리

```java
Mono.just("hello")
    .map(String::toUpperCase)
    .flatMap(v -> Mono.just(v + " world"));
```

## 4. Scheduler와 스레드 모델

Reactor는 연산을 특정 스레드에서 실행하도록 제어할 수 있다.

| Scheduler      | 용도           |
| -------------- | ------------ |
| parallel       | CPU 바운드 작업   |
| boundedElastic | 블로킹 가능 작업 위임 |
| single         | 단일 스레드 실행    |

`subscribeOn`은 최초 실행 스레드를, `publishOn`은 이후 연산 스레드를 변경한다.

```java
Mono.fromCallable(this::blockingOperation)
    .subscribeOn(Schedulers.boundedElastic());
```

## 5. Context 전파

Reactor Context는 비동기 흐름에서 논리적 컨텍스트를 전달하는 구조이다.

```java
Mono.deferContextual(ctx -> Mono.just(ctx.get("traceId")))
    .contextWrite(Context.of("traceId", "abc-123"));
```

## 6. 테스트 (StepVerifier)

```java
StepVerifier.create(Flux.just(1, 2, 3).map(i -> i * 2))
    .expectNext(2, 4, 6)
    .verifyComplete();
```

## 요약

* Reactor는 비동기 계산을 표현하는 도구이다.
* Mono/Flux는 실제 실행 흐름을 기록하는 정의체이며 지연 실행된다.
* Scheduler로 실행 스레드 제어가 가능하며, Backpressure로 수요 조절을 처리한다.
