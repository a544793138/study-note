# Maven

## 常用打包命令

- 将项目的依赖（pom.xml 中声明的 jar）加载到本地仓库：

  > 通常需要将项目 install 后才好 package

  clean install -DskipTests

- 用于将项目打包：clean package -DskipTests

  - clean：清空 target 文件夹内容
  - package：打包
  - -DskipTests：跳过项目中的测试

- 导出项目的所有依赖包：

  > 完成后会在 target 中看到 dependencies 文件夹

  clean dependency:copy-dependencies

- 用于将项目部署到远程仓库里

  clean deploy -DskipTests

## 高级使用

- 项目 A 依赖项目 B，现在需要打一个项目 A 的包：

  > 先将 A 的依赖打好包后再打 A 的包

  mvn clean package -pl [A] -am  -DskipTests

- 扫描项目的依赖包，显示依赖的情况：是否有冲突、是否重复等等，同时，也可以使用 IDEA 里的 Maven 工具，可以显示出依赖树，显示红色的表示存在冲突

  mvn -U dependency:tree -Dverbose

- 将本地 jar 包安装到本地仓库中

  mvn install:install-file -Dfile=C:\Users\20190418\Desktop\soft-crypto-0.0.1.jar -DgroupId=com.keyou -DartifactId=soft-crypto -Dversion=0.0.1 -Dpackaging=jar