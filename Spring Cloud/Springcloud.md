# Spring Cloud

Cloud 是微服务架构。

需要熟悉 SpringMVC + Spring /SpringBoot + Mybatis + git + maven ...

Cloud 不是一种技术，而是一堆技术的集合，大概 21 种技术

这里挑选常用的说，俗称 Cloud 技术**五大神兽: Eureka, Ribbon/Feign, Hystrix, zuul, Config**。

## 1 面试题

- 什么是微服务

- 微服务之间是如何独立通讯的

- Spring Cloud 和 Dubbo 有哪些区别

- Spring Boot 和 Spring Cloud, 请谈谈你对他们的理解

- 什么是服务熔断？什么是服务降级？

- 微服务的优缺点分别是什么？项目开发中碰到的坑

- 你所知道的微服务技术栈有哪些，列举一二

- Eureka 和 zookeeper 都可以提供服务注册与发现功能，请说说两个的区别？

  ...

## 2 微服务概述

### 2.1 微服务是什么

微服务由**马丁福勒**提出

现在还没有一个统一的标准的定义，但有大概说法

**提倡将单一应用程序，按业务拆分为多个小服务，每个服务运行在独立的进程中**。

> 为什么说没有统一的定义，一个原因就是这个拆分角度。

**服务之间采用轻量级的通信机制互相沟通**

> **Spring Cloud 与 Dubbo 有什么区别，其中一个区别就在这里 !!!!**：
>
> - Dubbo 是采用基于 RPC 远程过程调用的机制通信
> - Spring Cloud 是采用 HTTP 的 RESTful API 接口进行通信

分布式 - 专业的事情交给专人做，尽量降低耦合度

每个服务独立的开发，独立部署，都可以提供单个业务功能，拥有自己独立的数据库。

可以使用不同的语言开发。

> 原文翻译：http://blog.cuicc.com/blog/2015/07/22/microservices/
>
> 简单来说，微服务架构风格是**一种将一个单一应用程序开发为一组小型服务**的方法，每个服务运行在自己的进程中，服务间通信采用轻量级通信机制(通常用HTTP资源API)。这些服务围绕业务能力构建并且可通过全自动部署机制独立部署。这些服务共用一个最小型的集中式的管理，服务可用不同的语言开发，使用不同的数据存储技术。 

![](F:\woods\study-note\Spring Cloud\images\单体与微服务应用.png)









## 3 Spring Cloud 入门

## 4 Rest 微服务构建案例工程模块

## 5 Eureka 服务注册与发现

## 6 Ribbon 负载均衡

## 7 Feign 负载均衡

## 8 Hystrix 断路器

## 9 zuul 路由网关

## 10 Spring Cloud Config 分布式配置中心

## 11 总结