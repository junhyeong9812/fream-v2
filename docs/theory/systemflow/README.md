# Netty / Reactor / TCP / HTTP / Mono 내부 동작 흐름 정리

1. TCP 연결과 바이트 스트림

* TCP는 3-way handshake로 연결을 생성한다.
* 연결은 (Source IP, Source Port, Destination IP, Destination Port) 4개의 정보로 식별된다.
* TCP는 데이터를 메시지 단위가 아닌 연속된 바이트 스트림으로 전달한다.
* ACK는 단순히 "몇 번째 바이트까지 정상 수신했는지"를 나타낼 뿐, 요청을 식별하지 않는다.

2. OS 소켓 버퍼

* 각 TCP 연결은 커널 수준에서 send buffer와 receive buffer를 가진다.
* 수신된 데이터는 receive buffer에 저장된다.
* 애플리케이션에서 데이터를 전송하면 send buffer를 통해 네트워크로 나간다.
* OS는 바이트의 순서 및 무결성만 보장하며, HTTP 요청의 의미는 모른다.

3. HTTP 레벨 해석

* Netty는 epoll 등을 통해 소켓에 읽을 데이터가 생겼다는 이벤트를 감지한다.
* Netty는 receive buffer에서 바이트를 읽고 HTTP 디코더를 통해
  "메서드, 경로, 헤더, 바디" 형태의 HTTP 요청 객체로 변환한다.
* HTTP는 바이트 스트림을 요청 단위로 의미 있게 나누는 역할을 한다.

4. Netty Channel 및 EventLoop

* 하나의 TCP 연결은 하나의 Channel 객체에 대응한다.
* Channel은 Worker EventLoop 중 하나에 할당된다.
* EventLoop는 단일 스레드로 여러 Channel을 처리한다.
* EventLoop는 I/O 이벤트(read/write 가능 이벤트)를 감지하고 처리 흐름을 실행한다.

5. WebFlux와 Mono/Flux 파이프라인

* 요청 처리 중 DB, Redis, 외부 API 등 I/O 작업이 발생하면 Mono/Flux 파이프라인이 구성된다.
* 이 시점에서 실제 작업은 수행되지 않고, 이후에 이어서 실행할 연산(continuation)이 저장된다.
* 스레드는 I/O를 기다리지 않고 즉시 반환된다.

6. 데이터베이스 응답 처리

* DB 클라이언트는 요청을 전송할 때 요청을 식별할 수 있는 Request ID 등을 포함한다.
* 응답이 도착하면 OS → Netty EventLoop를 통해 데이터가 읽힌 후
  DB 프로토콜 디코더가 Request ID로 어떤 연산과 이어질지 판단한다.
* Reactor는 저장된 continuation을 재개하여 남은 Mono 파이프라인을 실행한다.

7. 응답 반환

* 최종 처리 결과가 HTTP 응답 객체로 만들어진다.
* Netty는 이를 다시 바이트 스트림으로 직렬화한다.
* send buffer에 쓰고, TCP를 통해 클라이언트에게 전달된다.

8. 전체 요약

* TCP는 신뢰성 있는 바이트 흐름을 제공한다.
* OS는 각 소켓별로 버퍼를 관리한다.
* Netty는 비동기 이벤트 기반으로 I/O를 처리한다.
* HTTP는 바이트 스트림을 의미 있는 요청/응답 형태로 해석한다.
* Reactor는 비동기 I/O 이후 이어서 실행할 처리 흐름을 관리한다.
* 이를 통해 적은 수의 스레드로도 매우 많은 동시 요청을 처리할 수 있다.
