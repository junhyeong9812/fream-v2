# WebFlux README

## 1. 개요

Spring WebFlux는 논블로킹 I/O 기반 웹 프레임워크로, Reactor와 Netty를 핵심 실행 엔진으로 사용한다. 기존 Spring MVC(Tomcat, 요청당 스레드)를 대체하는 **이벤트 루프 기반 고성능 모델**을 제공한다.

WebFlux의 핵심 목표는 다음과 같다:

* 높은 동시성 처리
* 적은 스레드 사용
* 논블로킹 I/O 기반 데이터 처리
* Reactor(Mono/Flux) 기반 비동기 연산 구성

## 2. 요청 → 응답 처리 흐름

```
HTTP 요청 수신 (Netty)
↓
ServerHttpRequest / ServerHttpResponse 생성
↓
HttpHandler → WebHandler
↓
DispatcherHandler
  - HandlerMapping (라우팅)
  - HandlerAdapter (실행)
↓
컨트롤러 / 라우터 호출 → Mono 또는 Flux 반환
↓
Publisher 구독 시 실행 시작
↓
응답 데이터 직렬화 → Netty를 통해 반환
```

## 3. 컨트롤러 방식

### 애노테이션 스타일

```java
@RestController
@RequestMapping("/v1/users")
class UserController {
  private final UserService service;

  @GetMapping("/{id}")
  Mono<UserDto> getUser(@PathVariable String id) {
    return service.findById(id); // Mono 반환
  }
}
```

### 함수형 라우터 스타일

```java
@Bean
RouterFunction<ServerResponse> routes(UserHandler handler) {
  return RouterFunctions.route()
    .GET("/v1/users/{id}", handler::getUser)
    .build();
}
```

## 4. ServerRequest와 요청 바디 처리

HTTP Body는 **스트림(Flux<DataBuffer>)** 형태로 전달된다.

```java
Mono<UserRequest> body = request.bodyToMono(UserRequest.class);
Flux<UserRequest> stream = request.bodyToFlux(UserRequest.class);
```

## 5. 응답 생성

`ServerResponse`는 함수형 스타일에서 주로 사용된다.

```java
ServerResponse.ok().body(service.findAll(), UserDto.class);
```

## 6. 필터와 공통 처리

### WebFilter

```java
@Component
class AuthFilter implements WebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return validate(exchange) ? chain.filter(exchange) : unauthorized(exchange);
  }
}
```

### 전역 예외 처리

```java
@Component
class GlobalErrorHandler implements WebExceptionHandler {
  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
      .bodyValue(ex.getMessage())
      .flatMap(resp -> resp.writeTo(exchange, new ContextView() {}));
  }
}
```

## 7. 스레드 & 성능 모델

| 항목     | WebMVC   | WebFlux         |
| ------ | -------- | --------------- |
| I/O 모델 | Blocking | Non-Blocking    |
| 스레드 모델 | 요청당 스레드  | 적은 수의 EventLoop |
| 확장성    | 낮음       | 높음              |

### 주의

* **EventLoop 스레드에서 블로킹 호출(JDBC, RestTemplate 등)을 실행하면 전체 서버 성능이 급격히 저하된다.**
* 필요 시 `boundedElastic` 스케줄러로 격리한다.

```java
Mono.fromCallable(this::blockingWork)
    .subscribeOn(Schedulers.boundedElastic());
```

## 8. 요약

* WebFlux는 Reactor + Netty 기반 논블로킹 서버 모델이다.
* 컨트롤러는 Mono/Flux를 반환하며, 실행은 지연되고 구독 시점에 시작된다.
* 스레드 효율이 매우 높아 고동시성 환경에 유리하다.
* 블로킹 I/O는 반드시 별도 스케줄러로 분리해야 한다.
