[TOC]

# gRPC 学习

## 学习文档

[gRPC 基础知识教程官方文档](<https://grpc.io/docs/tutorials/basic/java/>)

[快速开始官方文档](<https://grpc.io/docs/quickstart/java/>)

## 基础

**什么是 gRPC ?**

可以查找 gRPC 时发现更多出现的是 RPC，那么可以猜测 gPRC 这是一个组合而成的词：g - 应该是代表 google，即框架的开发商。

那么 RPC 是什么意思？从[资料](<https://www.jianshu.com/p/2accc2840a1b>)中了解到，是 RPC 即是**远程过程调用** ，与传统的 HTTP 请求接口的**本地过程调用**

区别开来。例如通俗的说，**现在需要完成洗碗这一件事情，本地过程调用就是你亲自去洗碗，而远程过程调用就是你不用亲自去洗碗，你只需要叫另一个人去洗碗就可以了**。

gRPC 就是类似的一个框架，提供远程过程调用功能，让你的远程调用和本地调用性能相差不远，甚至更好。同时，因为它是远程调用的，这说明构建分布式系统时，只需要部署多个客户端（调用方）就可以了。

总之，使用 gRPC 可以解决分布式问题，可以提高客户端与服务之间的性能。

## 术语

**protocol buffers**

- **协议缓冲区**。这是带有 `.proto` 扩展名的普通文本文件。

- gRPC 使用协议缓冲区，就是我们说的 proto 文件。gRPC 可以根据 proto 文件并使用议缓冲区编译器 protoc ，可以直接生成对应的 java 类（服务端和客户端代码）。

- 协议缓冲区中的结构化数据被叫为消息，使用 `message` 定义，用于表示 proto 中定义的方法的入参或响应值，如：

  ```protobuf
  message Person {
    string name = 1;
    int32 id = 2;
    bool has_ponycopter = 3;
  }
  
  或者
  
  // The greeter service definition.
  service Greeter {
    // Sends a greeting
    rpc SayHello (HelloRequest) returns (HelloReply) {}
  }
  
  // The request message containing the user's name.
  message HelloRequest {
    string name = 1;
  }
  
  // The response message containing the greetings
  message HelloReply {
    string message = 1;
  }
  ```

- 最新的协议缓冲区是 proto3 [20191016]。

## 服务定义

从上面可以知道，gRPC 默认使用协议缓存区来作为接口定义语言（IDL）来描述服务接口和有效负载消息的结构。如果需要，可以使用其他替代方法。

```protobuf
service HelloService {
  rpc SayHello (HelloRequest) returns (HelloResponse);
}

message HelloRequest {
  string greeting = 1;
}

message HelloResponse {
  string reply = 1;
}
```

### 4 种服务方法

gPRC 允许定义 4 种类似的服务方法。

- 一元RPC。客户端向服务器发送单个请求并获得单个响应，就像普通函数调用一样。

  ```protobuf
  rpc SayHello (HelloRequest) returns (HelloResponse) {}
  ```

- 服务器流式RPC。客户端在其中向服务器发送请求，并获取流以读取回一系列消息。客户端从返回的流中读取，直到没有更多消息为止。gRPC保证单个RPC调用中的消息顺序。

  ```protobuf
  rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse){}
  ```

- 客户端流式RPC，客户端在其中编写消息序列，然后再次使用提供的流将它们发送到服务器。客户端写完消息后，它将等待服务器读取消息并返回响应。gRPC再次保证了在单个RPC调用中的消息顺序。

  ```protobuf
  rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse) {}
  ```

- 双向流式RPC，双方都使用读写流发送一系列消息。这两个流是独立运行的，因此客户端和服务器可以按照自己喜欢的顺序进行读写：例如，服务器可以在写响应之前等待接收所有客户端消息，或者可以先读取消息再写入消息，或其他一些读写组合。每个流中的消息顺序都会保留。

  ```protobuf
  rpc BidiHello(stream HelloRequest) returns (stream HelloResponse){}
  ```

### API

在编写好 proto 文件后，使用协议缓冲区编译器插件，可以生成客户端和服务器端代码。**gRPC用户通常在客户端调用这些API，并在服务器端实现相应的API**。

这就说明客户端和服务器都有 proto 文件中定义的服务方法，只不过一个是调用方，做传入参数，读取响应的工作，另一个是被调用方，做读取参数，处理参数，返回响应的工作。

在客户端中，客户端具有一个称为 *stub* 的本地对象（对于某些语言，首选术语是 *client*），该对象实现与服务相同的方法。也就是上面所说的。

### 同步 / 异步

对于 Java 而言，无论请求 / 响应，只要带有 steam 都是异步。只有一元是同步的。

## 性能

gRPC 在各种语言版本上，包括 Java ，都加入了自己的性能测试程序。通过下面地址可以看到：

<https://grpc.io/docs/guides/benchmarking/>

其中的测试结果需要翻墙才可以看到。

## 接口例子

从 <https://grpc.io/docs/tutorials/basic/java/> 教程中 “实施RouteGuide” 部分可以看到下面要说的内容。

教程中主要使用代码中以下文件：

- /home/user/projects/grpc-java/examples/src/main/proto/route_guide.proto
- /home/user/projects/grpc-java/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideServer.java
- /home/user/projects/grpc-java/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideClient.java

这部分主要是介绍服务端是如何工作的，同时如何跟客户端通讯。

### 一元 RPC 服务方法

```java
// getFeature 有两个参数：
// 1. 从客户端接收到的请求 Point
// 2. StreamObserver<Feature>：响应观察者，这是服务器用来调用其响应的特殊接口。
@Override
public void getFeature(Point request, StreamObserver<Feature> responseObserver) {
  // checkFeature 方法通过 Point 返回 Feature
  // onNext 方法将 Feature 从服务端返回给客户端
  responseObserver.onNext(checkFeature(request));
  // 使用 onCompleted 方法告诉客户端，服务端已经处理完 RPC
  responseObserver.onCompleted();
    
  // 可以看到 onNext、onCompleted 方法都属于 responseObserver 对象的。该对象就是用来沟通客户端和服务端的。
}

private Feature checkFeature(Point location) {
  for (Feature feature : features) {
    if (feature.getLocation().getLatitude() == location.getLatitude()
        && feature.getLocation().getLongitude() == location.getLongitude()) {
      return feature;
    }
  }

  // No feature was found, return an unnamed feature.
  return Feature.newBuilder().setName("").setLocation(location).build();
}
```

### 服务端流式 RPC

在服务端流式 RPC 中，我们需要将多个 Features 返回给客户端。

```java
// 同样带有 StreamObserver<Feature> 参数
@Override
public void listFeatures(Rectangle request, StreamObserver<Feature> responseObserver) {
  int left = min(request.getLo().getLongitude(), request.getHi().getLongitude());
  int right = max(request.getLo().getLongitude(), request.getHi().getLongitude());
  int top = max(request.getLo().getLatitude(), request.getHi().getLatitude());
  int bottom = min(request.getLo().getLatitude(), request.getHi().getLatitude());

  for (Feature feature : features) {
    if (!RouteGuideUtil.exists(feature)) {
      continue;
    }

    int lat = feature.getLocation().getLatitude();
    int lon = feature.getLocation().getLongitude();
    if (lon >= left && lon <= right && lat >= bottom && lat <= top) {
      // 只要符合条件，一直使用 onNext 将 Features 返回给客户端
      responseObserver.onNext(feature);
    }
  }
  // 最后同样使用 onCompleted 表示处理完成
  responseObserver.onCompleted();
}
```

### 客户端流式 RPC

```java
// 像上面的方法一样，一样有 StreamObserver 对象
// 但是，这次不一样，它返回一个 StreamObserver<Point> 对象给客户端来编写需要传入的 Point
// 然后入参却是本来需要返回的 RouteSummary
@Override
public StreamObserver<Point> recordRoute(final StreamObserver<RouteSummary> responseObserver) {
  // 这个新建出来的 StreamObserver<Point> 是返回给客户端的
  return new StreamObserver<Point>() {
    int pointCount;
    int featureCount;
    int distance;
    Point previous;
    long startTime = System.nanoTime();

    // 从上面可以看出来这个 StreamObserver<Point> 是返回给客户端的使用的，所以 StreamObserver<Point> 里重写的方法，也是提供给客户端使用的。客户端每次使用 onNext 传入 Point 的时候，都是使用这个 onNext 方法。也就是说，客户端调用的 onNext 方法就是这个返回的流里面的 onNext 方法。
	// 同理，下面的 onError、onCompleted 也是提供给客户端使用的方法。
    @Override
    public void onNext(Point point) {
      pointCount++;
      if (RouteGuideUtil.exists(checkFeature(point))) {
        featureCount++;
      }
      // For each point after the first, add the incremental distance from the previous point
      // to the total distance value.
      if (previous != null) {
        distance += calcDistance(previous, point);
      }
      previous = point;
    }

    @Override
    public void onError(Throwable t) {
      logger.log(Level.WARNING, "Encountered error in recordRoute", t);
    }

	// 从上面知道这个方法会在客户端调用 onCompleted 表示它完成传输。同时，客户端调用 onCompleted 的方法，就是调用这里的 onCompleted 方法。
	// 从这个方法中可以看到，是使用 responseObserver 的 onNext 方法返回 RouteSummary，并在后面使用 onCompleted 表示服务端处理完成 PRC。
    @Override
    public void onCompleted() {
      long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
      responseObserver.onNext(RouteSummary.newBuilder().setPointCount(pointCount)
          .setFeatureCount(featureCount).setDistance(distance)
          .setElapsedTime((int) seconds).build());
      responseObserver.onCompleted();
    }
  };
}
```

### 双向流式 RPC

双向流式的服务方法，是指请求和返回都是流式的。从教程中可知，双向流式的 RPC 是允许客户端与服务端像打乒乓球，客户端和服务器可以按照自己喜欢的顺序进行读写：例如，服务器可以在写响应之前等待接收所有客户端消息，或者可以先读取消息再写入消息，或其他一些读写组合。每个流中的消息顺序都会保留。

```protobuf
// proto 中的定义
rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
```

与从客户端流式中的道理，是一样的。只不过在双向流式 RPC 中客户端也是需要返回一个流，并在流中重写 onNext、onError、onCompleted 方法，这些方法是提供给服务端使用的。

```java
@Override
public StreamObserver<RouteNote> routeChat(final StreamObserver<RouteNote> responseObserver) {
  return new StreamObserver<RouteNote>() {
    @Override
    public void onNext(RouteNote note) {
      List<RouteNote> notes = getOrCreateNotes(note.getLocation());

      // Respond with all previous notes at this location.
      for (RouteNote prevNote : notes.toArray(new RouteNote[0])) {
        responseObserver.onNext(prevNote);
      }

      // Now add the new note to the list
      notes.add(note);
    }

    @Override
    public void onError(Throwable t) {
      logger.log(Level.WARNING, "Encountered error in routeChat", t);
    }

    @Override
    public void onCompleted() {
      responseObserver.onCompleted();
    }
  };
}
```

## 启动服务器

























