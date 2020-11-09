# postgres

> 1. 不需要进入postgres就可以直接执行
> 2. dum.sql 是导出的 sql 文件

**导出数据库**

pg_dump -h localhost -U postgres(用户名) 数据库名(缺省时同用户名) > dum.sql



**导入数据库**

psql -h localhost -U postgres(用户名)  数据库名(缺省时同用户名) < dum.sql



**进入 postgers**

psql -h [地址] -U [用户名] -W，回车，再输入密码



**进入 postgres 后，列出所有数据库**

\l



**进入postgres后，进入具体数据库**

\c [dbname]



**列出所有表**

\d



> 1. 有人连接是无法删除
> 2. 需要进入postgres

**删除数据库**

drop database [dbname]



**删除表**

drop table [tablename]



**查看当期连接数**

select count(1) from pg_stat_activity;



**最大连接数**

show max_connections;



**配置最大连接数**

postgresql.conf：

max_connections = 500

**删除指定数据库的连接**
select pg_terminate_backend(pid) from pg_stat_activity where datname='caastest3';

# 列出所有 schema
\dn

# 创建 schema。
# schema 是用在多人共享数据库，而又希望数据库相互独立的需求中的。默认是 public。不同的人可以创建不同的 schema，从而拥有相同的数据库名和地址等基本参数，但内容却是相互独立的。
# 一般在进入具体数据库后，再创建需要的 schema
create schema [schema 名字]