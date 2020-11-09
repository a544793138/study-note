# 简介

Docker 是一个开源的应用容器引擎，基于 [Go 语言](http://www.runoob.com/go/go-tutorial.html) 并遵从Apache2.0协议开源。

Docker 可以让开发者打包他们的应用以及依赖包到一个轻量级、可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。

容器是完全使用沙箱机制，相互之间不会有任何接口（类似 iPhone 的 app）,更重要的是容器性能开销极低。

## 三大优点

1. **简化程序**。Docker改变了虚拟化的方式，使开发者可以直接将自己的成果放入Docker中进行管理。方便快捷已经是 Docker的最大优势，过去需要用数天乃至数周的	任务，在Docker容器的处理下，只需要数秒就能完成。
2. **避免选择恐惧症**。比如 Docker 镜像；Docker 镜像中包含了运行环境和配置，所以 Docker 可以简化部署多种应用实例工作。比如 Web 应用、后台应用、数据库应用、大数据应用比如 Hadoop 集群、消息队列等等都可以打包成一个镜像部署。
3. **节省开支**。Docker 改变了高性能必然高价格的思维定势。Docker 与云的结合，让云空间得到更充分的利用。不仅解决了硬件管理的问题，也改变了虚拟化的方式。

# Docker 架构

Docker 使用客户端 - 服务器（C/S）架构模式，使用远程 API 来管理和创建 Docker 容器。

Docker 容器通过 Dokcer 镜像来创建，容器和镜像的关系类似 Java 的对象和类。

![](./img/576507-docker1.png)

| Docker 镜像(Images)    | Docker 镜像是用于创建 Docker 容器的模板。                    |
| ---------------------- | ------------------------------------------------------------ |
| Docker 容器(Container) | 容器是独立运行的一个或一组应用。                             |
| Docker 客户端(Client)  | Docker 客户端通过命令行或者其他工具使用 Docker API (<https://docs.docker.com/reference/api/docker_remote_api>) 与 Docker 的守护进程通信。 |
| Docker 主机(Host)      | 一个物理或者虚拟的机器用于执行 Docker 守护进程和容器。       |
| Docker 仓库(Registry)  | Docker 仓库用来保存镜像，可以理解为代码控制中的代码仓库。Docker Hub([https://hub.docker.com](https://hub.docker.com/)) 提供了庞大的镜像集合供使用。 |
| Docker Machine         | Docker Machine是一个简化Docker安装的命令行工具，通过一个简单的命令行即可在相应的平台上安装Docker，比如VirtualBox、 Digital Ocean、Microsoft Azure。 |

# Docker 安装

Docker 在多系统上都有相应的版本，参考链接安装：

- Ubuntu：http://www.runoob.com/docker/ubuntu-docker-install.html
- CentOS：http://www.runoob.com/docker/centos-docker-install.html
- Win：http://www.runoob.com/docker/windows-docker-install.html
- Mac：http://www.runoob.com/docker/macos-docker-install.html

# Docker 使用

Docker 允许你在容器内允许应用程序，使用 `docker run` 命令来在容器内运行一个应用程序。

```
runoob@runoob:~$ docker run ubuntu:15.10 /bin/echo "Hello world"
Hello world
```

解析：

- docker - Docker 的二进制执行文件
- run - 与前面的 docker 组合来运行一个容器
- ubuntu:15.10 - 指定要运行的镜像，Dokcer 首先从本地主机上查找镜像是否存在，如果不存在，Docker会从镜像仓库 Docker Hub 下载公共镜像
- /bin/echo "Hello world" - 在启动的容器里执行的命令

完整意思：

​	Docker 以 ubuntu:15.10 镜像创建一个新容器，然后再容器里执行 bin/echo "Hello world"，然后输出结果

## 运行交互式的容器

```
runoob@runoob:~$ docker run -i -t ubuntu:15.10 /bin/bash
root@dc0050c79503:/#
```

各个参数解析：

- **-t:**在新容器内指定一个伪终端或终端。
- **-i:**允许你对容器内的标准输入 (STDIN) 进行交互。

此时我们已进入一个 ubuntu15.10系统的容器，我们可以通过运行exit命令或者使用CTRL+D来退出容器。

## 启动容器（后台模式）

```
runoob@runoob:~$ docker run -d ubuntu:15.10 /bin/sh -c "while true; do echo hello world; sleep 1; done"
2b1b7a428627c51ab8810d541d759f072b4fc75487eed05812646b8534a2fe63
```

在输出中，我们没有看到期望的"hello world"，而是一串长字符

**2b1b7a428627c51ab8810d541d759f072b4fc75487eed05812646b8534a2fe63**

这个长字符串叫做容器ID，对每个容器来说都是唯一的，我们可以通过容器ID来查看对应的容器发生了什么。

首先，我们需要确认容器有在运行，可以通过 **docker ps** 来查看

```
runoob@runoob:~$ docker ps
```

**CONTAINER ID:**容器ID

**NAMES:**自动分配的容器名称

在容器内使用 `docker logs + 容器ID/容器名称` 命令，查看容器内的标准输出

```
runoob@runoob:~$ docker logs 2b1b7a428627
或
runoob@runoob:~$ docker logs amazing_cori
```

这样我们就看到了期望的 hello world

## 停止容器

我们使用 `docker stop + 容器ID/容器名称` 命令来停止容器

```
runoob@runoob:~$ docker stop 2b1b7a428627
runoob@runoob:~$ docker stop amazing_cori
```

# Docker 容器使用

```
# 直接输入 docker 命令来查看到 Docker 客户端的所有命令选项
runoob@runoob:~# docker

#通过命令 docker command --help 更深入的了解指定的 Docker 命令使用方法
runoob@runoob:~# docker stats --help
```

## 运行一个web应用

在docker容器中运行一个 Python Flask 应用来运行一个web应用

```
runoob@runoob:~# docker pull training/webapp  # 载入镜像
runoob@runoob:~# docker run -d -P training/webapp python app.py
```

参数说明:

- **-d:**让容器在后台运行。
- **-P:**将容器内部使用的网络端口映射到我们使用的主机上。

## 查看web应用容器

使用 docker ps 来查看我们正在运行的容器：

```
runoob@runoob:~#  docker ps
```

| CONTAINER ID | IMAGE           | COMMAND         | ...  | PORTS                   |
| ------------ | --------------- | --------------- | ---- | ----------------------- |
| d3d5e39ed9d3 | training/webapp | "python app.py" | ...  | 0.0.0.0:32769->5000/tcp |

其中 PORTS 为端口信息。

Docker 开放了 5000 端口（默认 Python Flask 端口）映射到主机端口 32769 上。

这时候我们通过浏览器访问 web 应用：xxx.xxx.xxx.xxx:32769，就可以看到应用的输出。



我们也可以通过 -p 参数来设置不一样的端口：

```
# 容器内部的 5000 端口映射到我们本地主机的 5000 端口上。
runoob@runoob:~$ docker run -d -p 5000:5000 training/webapp python app.py
```

| CONTAINER ID | IMAGE           | ...  | PORTS                   | NAMES                  |
| ------------ | --------------- | ---- | ----------------------- | ---------------------- |
| bf08b7f2cd89 | training/webapp | ...  | 0.0.0.0:5000->5000/tcp  | wizardly_chandrasekhar |
| d3d5e39ed9d3 | training/webapp | ...  | 0.0.0.0:32769->5000/tcp | xenodochial_hoov       |

## 网络端口的快捷方式

通过 **docker ps** 命令可以查看到容器的端口映射，**docker** 还提供了另一个快捷方式 **docker port**，使用 **docker port** 可以查看指定 （ID 或者名字）容器的某个确定端口映射到宿主机的端口号。

上面我们创建的 web 应用容器 ID 为 **bf08b7f2cd89** 名字为 **wizardly_chandrasekhar**。

我可以使用 **docker port bf08b7f2cd89** 或 **docker port wizardly_chandrasekhar** 来查看容器端口的映射情况。

```
runoob@runoob:~$ docker port bf08b7f2cd89
5000/tcp -> 0.0.0.0:5000
```

## 查看 web 应用程序日志

docker logs [ID或者名字] 可以查看容器内部的标准输出。

```
runoob@runoob:~$ docker logs -f bf08b7f2cd89
 * Running on http://0.0.0.0:5000/ (Press CTRL+C to quit)
192.168.239.1 - - [09/May/2016 16:30:37] "GET / HTTP/1.1" 200 -
192.168.239.1 - - [09/May/2016 16:30:37] "GET /favicon.ico HTTP/1.1" 404 -
```

**-f:** 让 **docker logs** 像使用 **tail -f** 一样来输出容器内部的标准输出。

从上面，我们可以看到应用程序使用的是 5000 端口并且能够查看到应用程序的访问日志。

## 查看 web 应用程序容器的进程

可以使用 docker top 来查看容器内部运行的进程

```
runoob@runoob:~$ docker top wizardly_chandrasekhar
UID     PID         PPID          ...       TIME                CMD
root    23245       23228         ...       00:00:00            python app.py
```

## 检测 web 应用程序

使用 **docker inspect** 来查看 Docker 的底层信息。它会返回一个 JSON 文件记录着 Docker 容器的配置和状态信息。

```json
runoob@runoob:~$ docker inspect wizardly_chandrasekhar
[
    {
        "Id": "bf08b7f2cd897b5964943134aa6d373e355c286db9b9885b1f60b6e8f82b2b85",
        "Created": "2018-09-17T01:41:26.174228707Z",
        "Path": "python",
        "Args": [
            "app.py"
        ],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 23245,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2018-09-17T01:41:26.494185806Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
......
```

## 停止 web 应用容器

```
runoob@runoob:~$ docker stop wizardly_chandrasekhar   
wizardly_chandrasekhar
```

## 重启 web 应用容器

已经停止的容器，我们可以使用命令 docker start 来启动。

```
runoob@runoob:~$ docker start wizardly_chandrasekhar
wizardly_chandrasekhar
```

docker ps -l 查询最后一次创建的容器：

```
#  docker ps -l 
CONTAINER ID        IMAGE                             PORTS                     NAMES
bf08b7f2cd89        training/webapp     ...        0.0.0.0:5000->5000/tcp    wizardly_chandrasekhar
```

正在运行的容器，我们可以使用 docker restart 命令来重启

## 移除WEB应用容器

我们可以使用 docker rm 命令来删除不需要的容器

```
runoob@runoob:~$ docker rm wizardly_chandrasekhar  
wizardly_chandrasekhar
```

**删除容器时，容器必须是停止状态，否则会报如下错误**

```
runoob@runoob:~$ docker rm wizardly_chandrasekhar
Error response from daemon: You cannot remove a running container bf08b7f2cd897b5964943134aa6d373e355c286db9b9885b1f60b6e8f82b2b85. Stop the container before attempting removal or force remove
```

# Docker 镜像使用

当运行容器时，使用的镜像如果在本地中不存在，docker 就会自动从 docker 镜像仓库中下载，默认是从 Docker Hub 公共镜像源下载。

下面我们来学习：

- 1、管理和使用本地 Docker 主机镜像
- 2、创建镜像

## 列出镜像列表

我们可以使用 **docker images** 来列出本地主机上的镜像。

```
runoob@runoob:~$ docker images           
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
ubuntu              14.04               90d5884b1ee0        5 days ago          188 MB
php                 5.6                 f40e9e0f10c8        9 days ago          444.8 MB
nginx               latest              6f8d099c3adc        12 days ago         182.7 MB
mysql               5.6                 f2e8d6c772c0        3 weeks ago         324.6 MB
httpd               latest              02ef73cf1bc0        3 weeks ago         194.4 MB
ubuntu              15.10               4e3b13c8a266        4 weeks ago         136.3 MB
hello-world         latest              690ed74de00f        6 months ago        960 B
training/webapp     latest              6fae60ef3446        11 months ago       348.8 MB
```


启动 caas 数据库
systemctl start docker
sudo docker start postgres