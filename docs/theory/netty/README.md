# Netty README

## 1. Netty 개요

Netty는 비동기 이벤트 기반 네트워크 처리 프레임워크이다. 적은 수의 스레드로 많은 연결을 관리할 수 있도록 설계되어 있으며, 내부적으로 OS의 epoll/kqueue 기반 이벤트 모델을 활용한다.

## 2. 핵심 요소

* EventLoopGroup: 이벤트 루프 스레드를 관리하는 그룹
* Boss Group: TCP 연결 요청을 수락
* Worker Group: 수락된 연결의 I/O 이벤트 처리
* Channel: TCP 연결(소켓)을 추상화한 객체
* ChannelPipeline: 요청/응답 처리를 위한 핸들러 체인
* ByteBuf: Netty의 고성능 버퍼 구조

## 3. 이벤트 처리 흐름

```
TCP 연결 → Boss EventLoop가 accept 처리
↓
Channel 생성
↓
Worker EventLoop 중 하나에 Channel 배정
↓
이후 해당 Channel에 대한 read/write 이벤트는 동일 EventLoop에서 처리
```

Channel은 특정 EventLoop와 1:1로 고정 연결되므로 락 없이 안전하게 처리할 수 있다.

## 4. Pipeline과 Handler

ChannelPipeline은 인바운드/아웃바운드 이벤트 흐름을 단계적으로 처리한다.

```java
ch.pipeline()
  .addLast(new HttpServerCodec())
  .addLast(new HttpObjectAggregator(1_048_576))
  .addLast(new BusinessHandler());
```

## 5. 비동기 결과 처리

Netty의 write 연산은 즉시 반환되고, 실제 완료 여부는 ChannelFuture로 확인한다.

```java
channel.writeAndFlush(response).addListener(f -> {
  if (f.isSuccess()) {...}
  else {...}
});
```

## 6. 성능 상의 주의 사항

* EventLoop 스레드에서 블로킹 작업 금지(DB, 파일 I/O 등)
* 블로킹이 필요하다면 별도 스레드 풀로 위임
* ByteBuf는 참조 카운트 기반이므로 필요 시 명시적 해제 필요

## 요약

* Netty는 이벤트 루프 기반으로 효율적인 I/O 처리를 제공한다.
* Channel은 EventLoop와 고정 연결되어 일관성 있고 락 없는 처리 가능.
* Handler 기반 파이프라인으로 유연한 프로토콜 처리 구성 가능.
