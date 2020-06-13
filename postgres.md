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

