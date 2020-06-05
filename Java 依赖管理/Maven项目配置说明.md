# Maven 项目配置说明

本文档用于说明 Maven 项目中如何做到：

- 配置项目的远程仓库发布地址
- 配置项目的用于获取依赖的仓库
- 将 SDK 发布到 本地 / 远程仓库的命令

## 配置项目的远程仓库发布地址

配置 Maven 工程的远程仓库发布地址，作用是可以在使用 ` mvn clean deploy -DskipTests` 命令后，将项目打包好的 SDK 发布到 Maven 仓库中，方便集成到其它应用。

**配置方法**

例如，在项目的 `pom.xml` 中添加以下配置：

> `distributionManagement` 标签为 maven pom.xml 中 `project` 的子标签

```xml
<!-- jar 包发布地址 -->
<!-- 该配置表示将项目发布到自身的远程仓库 -->
<distributionManagement>
    <repository>
        <id>maven-release</id>
        <!-- 请填写自己的远程仓库地址 -->
        <url>http://192.1.5.98:8191/repository/maven-releases/</url>
    </repository>

    <snapshotRepository>
        <id>maven-snapshot</id>
        <!-- 请填写自己的远程仓库地址 -->
        <url>http://192.1.5.98:8191/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

## 配置项目的用于获取依赖的仓库

配置 Maven 用于获取依赖的仓库，可以让该项目获取到依赖仓库中所拥有的依赖，减少 pom.xml 上一些通用依赖的重复编写，方便依赖版本的管理等。

**配置方法**

例如，在项目的 `pom.xml` 中添加以下配置：

> `repositories` 标签为 maven pom.xml 中 `project` 的子标签

```xml
<!-- 获取依赖的仓库地址 -->
<!-- 该配置表示可以从以下仓库中获取依赖 -->
<repositories>
    <repository>
        <id>maven-public</id>
        <name>maven-public</name>
        <!-- 配置自己用于获取依赖的仓库地址 -->
        <url>http://192.1.5.98:8191/repository/maven-public/</url>
    </repository>
</repositories>
```

## 将 SDK 发布到 本地 / 远程仓库的命令

如果您已经您已经拥有一个 SDK 文件，需要将它发布到 Maven 的本地 / 远程仓库时，可以使用以下命令达到这个目的：

> 以下命令表示将一个名为 `soft-algoithm-java-1.0.0.jar` 的 SDK，发布到  Maven 的本地 / 远程仓库

```sh
# 分发到本地仓库
mvn install:install-file -Dfile=path/soft-algoithm-java-1.0.0.jar -DgroupId=soft.algorithm -DartifactId= soft-algoithm-java -Dversion=1.0.0 -DgeneratePom=true

# 分发到远程仓库
mvn deploy:deploy-file -Dfile=path/soft-algoithm-java-1.0.0.jar -DgroupId=soft.algorithm -DartifactId= soft-algoithm-java -Dversion=1.0.0 -Dpackage=jar  -DrepositoryId=maven-release -Durl= http://repo.example.com:8191/repository/maven-releases
```

