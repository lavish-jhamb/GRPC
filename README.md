# 📘 Java gRPC - Beginner’s Guide

## 🧠 Table of Contents
1. [What is gRPC?](#what-is-grpc)
2. [Why gRPC over REST?](#why-grpc-over-rest)
3. [Core Concepts](#core-concepts)
4. [Types of Streaming in gRPC](#types-of-streaming-in-grpc)
5. [Setup Instructions](#setup-instructions)
6. [Creating Your First gRPC App (Java)](#creating-your-first-grpc-app-java)
7. [Protobuf Syntax Quick Reference](#protobuf-syntax-quick-reference)
8. [Useful Commands](#useful-commands)
9. [References & Resources](#references--resources)

## ✅ What is gRPC?

- **gRPC** stands for **Google Remote Procedure Call**.
- It’s a high-performance, open-source RPC framework based on **Protocol Buffers (protobuf)**.
- Enables efficient communication between microservices across platforms and languages.

## 🚀 Why gRPC over REST?

| Feature         | gRPC                  | REST               |
|------------------|-----------------------|--------------------|
| Protocol         | HTTP/2               | HTTP/1.1           |
| Data Format      | Protocol Buffers     | JSON               |
| Performance      | Fast (binary)        | Slower (text)      |
| Bi-Directional?  | Yes                  | No                 |
| Streaming?       | Yes                  | Limited (via hacks)|

## 📚 Core Concepts

| Concept | Description |
|--------|-------------|
| **Service** | Interface definition using `.proto` file. |
| **Stub** | Auto-generated client code to call remote services. |
| **Channel** | Represents connection between client and server. |
| **Protobuf** | IDL to define request/response messages. |
| **Server Implementation** | Java class that implements the service defined in the proto file. |

## 🔄 Types of Streaming in gRPC

gRPC supports 4 types of communication:

| Type | Description | Example Use Case |
|------|-------------|------------------|
| Unary | Client sends a single request and gets a single response. | Get user profile |
| Server Streaming | Client sends a request, server sends a stream of responses. | Live stock price feed |
| Client Streaming | Client sends a stream of requests, server returns a single response. | File upload |
| Bi-directional Streaming | Both client and server send streams of messages. | Chat app |

## 🛠️ Setup Instructions

### Prerequisites
- Java 11+
- Maven or Gradle
- Protobuf compiler (`protoc`)
- gRPC Java libraries

### Add gRPC to Maven `pom.xml`
```xml
<dependencies>
  <dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.62.2</version>
  </dependency>
  <dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.62.2</version>
  </dependency>
  <dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.62.2</version>
  </dependency>
</dependencies>
```

## 💻 Creating Your First gRPC App (Java)

### Step 1: Define the Service (proto)
```proto
syntax = "proto3";

option java_package = "com.example.calculator";
option java_multiple_files = true;

service Calculator {
  rpc Add (AddRequest) returns (AddResponse);
}

message AddRequest {
  int32 a = 1;
  int32 b = 2;
}

message AddResponse {
  int32 result = 1;
}
```

### Step 2: Compile `.proto`
```bash
protoc --java_out=./src/main/java --grpc-java_out=./src/main/java --proto_path=src/main/proto calculator.proto
```

### Step 3: Server Implementation
```java
public class CalculatorService extends CalculatorGrpc.CalculatorImplBase {
  @Override
  public void add(AddRequest request, StreamObserver<AddResponse> responseObserver) {
    int result = request.getA() + request.getB();
    AddResponse response = AddResponse.newBuilder().setResult(result).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
```

### Step 4: Start Server
```java
public class GrpcServer {
  public static void main(String[] args) throws IOException, InterruptedException {
    Server server = ServerBuilder.forPort(9090)
      .addService(new CalculatorService())
      .build();

    server.start();
    System.out.println("Server started on port 9090");
    server.awaitTermination();
  }
}
```

### Step 5: Client Call
```java
public class GrpcClient {
  public static void main(String[] args) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
      .usePlaintext()
      .build();

    CalculatorGrpc.CalculatorBlockingStub stub = CalculatorGrpc.newBlockingStub(channel);

    AddRequest request = AddRequest.newBuilder().setA(10).setB(5).build();
    AddResponse response = stub.add(request);

    System.out.println("Result: " + response.getResult());
    channel.shutdown();
  }
}
```

## 🧾 Protobuf Syntax Quick Reference

```proto
// Data Types
int32, int64, float, double, bool, string

// Field Rules
message Person {
  string name = 1;
  int32 id = 2;
}

// Services
service MyService {
  rpc GetSomething(Request) returns (Response);
}
```

## 🔧 Useful Commands

```bash
# Install protoc (if not installed)
brew install protobuf  # macOS
choco install protoc   # Windows

# Compile .proto
protoc --proto_path=src/main/proto   --java_out=src/main/java   --grpc-java_out=src/main/java   src/main/proto/your_file.proto
```

## 📎 References & Resources

- [gRPC Java Documentation](https://grpc.io/docs/languages/java/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)
- [Awesome gRPC GitHub](https://github.com/grpc-ecosystem/awesome-grpc)