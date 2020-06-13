# Linux 常用命令

## 基础

- 关闭防火墙 重启失效：service firewalld stop
- 查看 Java 进程：ps -ef | grep java
- 切换至 root 用户：sudo -i
- 当前目录下的文件占用磁盘情况：du -sh *
- 添加用户：useradd [用户名]，必须添加密码：passwd [用户名]，然后回车，输入密码

## 压缩 / 解压

- 解压 tar 包：tar -xzvf [tar包]

- 将文件夹压缩成 tar 包：tar -zcvf [生成的tar包名字.tar.gz] [需要压缩的文件夹]

## 内容传输

- 传输：sftp 命令
  - 连接：sftp [用户名]@[需要连接的 IP 地址]
  - 从连接处获取内容：连接后，get xxx
  - 推送内容到连接处：连接后，put xxx

- 查看指令的映射地址：ls -al [指令]
  - 例如，java 指令，ls -al java，即可显示出 java 的来源

## 查看 CPU 占用较高的线程

1. 查看进程中CPU占用较高的线程，记录对应的TID（即shift+h后显示的PID）

   top -p [PID]，然后 shift + h

2. 获取线程信息，并找到占用CPU高的线程

   ps -mp [pid] -o THREAD,tid,time | sort -rn 

3. 将线程对应PID转为 16进制数（TID16）

   printf "%x\n" TID

4. 查看该线程的堆栈信息；

   jstack [PID] | grep -A 30 "nid=0x[TID16]"

## 查看端口 / 连接

- 查看某个端口的连接数：

  netstat -nat | grep -iw "9090" | wc -l

- 查看连接状况：

  netstat -nat | grep -iw "9090"