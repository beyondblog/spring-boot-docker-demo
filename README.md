## spring-boot 和 docker 集成

### 描述
java 的 Spring是一个很火的框架,Spring boot 这个也不用说了，Docker 近年也很火热, 本文就介绍下我在 Spring boot + Docker的集成一些经验 ：） 其实官网已经有一个手册介绍了 [这里](https://spring.io/guides/gs/spring-boot-docker)


### 可能会用到的东西
* JDK 1.8 或者更高
* Maven 3.0+ 或者是 Gradle 2.3+
* 一个IDE或者一个记事本
* 一个本地或者远程的Docker服务
* 充满好奇心的你

对没错,你至少需要一个Docker,可以安装在本地也可以安装在服务器上,具体安装方式请移步 [这里](https://docs.docker.com/installation/#installation f)

### 整个Spring boot的项目
我这边用的是IDEA 创建的
![create](https://raw.githubusercontent.com/beyondblog/spring-boot-docker-demo/master/img/create.png)

```
➜  sping-boot-docker  tree
.
├── mvnw
├── mvnw.cmd
├── pom.xml
├── sping-boot-docker.iml
└── src
    ├── main
    │   ├── java
    │   │   └── org
    │   │       └── beyondblog
    │   │           ├── DemoApplication.java
    │   │           ├── controller
    │   │           │   └── HelloController.java
    │   │           └── model
    │   │               └── Hello.java
    │   └── resources
    │       ├── application-dev.yml
    │       └── application.yml
    └── test
        └── java
            └── org
                └── beyondblog
                    └── DemoApplicationTests.java

12 directories, 10 files

```


#### pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>org.beyondblog</groupId>
	<artifactId>demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>demo</name>
	<description>Demo project for Spring Boot</description>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.3.2.BUILD-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

	<repositories>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
	</repositories>
	<pluginRepositories>
		<pluginRepository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</pluginRepository>
		<pluginRepository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</pluginRepository>
	</pluginRepositories>

</project>

```

程序很简单创建了一个Controller,然后映射了一个/hello的路由 运行之后如下

```
➜  target  java -jar demo-0.0.1-SNAPSHOT.jar
➜  sping-boot-docker  http "http://127.0.0.1:8080/hello"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Sun, 20 Dec 2015 23:41:14 GMT
Server: Apache-Coyote/1.1
Transfer-Encoding: chunked

{
    "code": 200,
    "messge": "Hello spring! profile=production"
}

```
可以正常允许，然后配置下pom集成docker (也可以用Dockerfile)

```
<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>com.spotify</groupId>
				<artifactId>docker-maven-plugin</artifactId>
				<version>0.3.8</version>
				<executions>
					<execution>
						<phase>deploy</phase>
						<goals>
							<goal>build</goal>
							<goal>tag</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<dockerHost>http://172.16.0.17:2375</dockerHost>
					<imageName>registry.****.com/demo:${project.artifactId}</imageName>
					<baseImage>docker.io/java:latest</baseImage>
					<registryUrl>https://registry.****.com</registryUrl>
					<env>
						<TZ>'Asia/Shanghai'</TZ>
						<LC_ALL>en_US.UTF-8</LC_ALL>
					</env>
					<resources>
						<resource>
							<targetPath>/</targetPath>
							<directory>${project.build.directory}</directory>
							<include>${project.build.finalName}.jar</include>
						</resource>
						<!--<resource>-->
							<!--<targetPath>/web</targetPath>-->
							<!--<directory>${project.build.directory}/web</directory>-->
						<!--</resource>-->
					</resources>
					<exposes>
						<expose>8080</expose>
					</exposes>
					<cmd>["java", "-jar", "/${project.build.finalName}.jar"]</cmd>
					<image>registry.****.com/demo:${project.artifactId}</image>
					<newName>registry.****.com/demo</newName>
					<forceTags>true</forceTags>
					<pushImage>false</pushImage>
				</configuration>
			</plugin>
		</plugins>
	</build>
```
描述下这段的意思

这儿用到的是spotify公司（就是那个做音乐的）的 docker-maven-plugin 插件用的是目前最新的0.3.8 之前的几个版本有一些目录拷贝的bug。

configuration 节点

有个dockerHost 就是docker服务器的地址 (必选)

imageName 就是生成的镜像名称的东西

baseImage 就是容器基于那个镜像跑 这儿用的是官方的 docker.io/java:latest（可以直接 docker pull 下来，也可以用自己做的。官方的是OpenJDK Java 7 JRE 可以去看它的项目主页 [这里](https://github.com/dockerfile/java) ） 就是一个JRE的环境

registryUrl 这个就是docker自建的私服地址 (可空) 这个填了之后可以触发push 到仓库的指令

env 不解释 默认的时区啊 字符集啊别忘记了 (自制镜像的可以忽略它)

resources 里面包含需要将那些东西包装到镜像里面去，注意我注释的这段

```
<resource>
	<targetPath>/web</targetPath>
	<directory>${project.build.directory}/web</directory>
</resource>
```
这个是为了举例如何包含整个目录（通过看[源代码](https://github.com/spotify/docker-maven-plugin)发现 不写include就是copy整个目录 目前插件的0.38版本是,之前的版本好像拷贝目录下的所有文件囧。。。）

exposes 是暴露出容器的那个端口出来,这儿填的是8080 Spring boot 默认就这端口

cmd 是镜像在run的时候默认执行的命令,这儿默认直接run （ps:这块可以指定profile 做生产环境和开发环境的切换）

image 同imageName

newName 是Tag的时候生成的名字 （可选） 强烈推荐加这个 这样默认会把新的容器的tag 设置成latest

forceTags 是否强制tag (强行哟)

pushImage 是否自动push到仓库

基本上需要用的就这么多,当然还有一些其他的参数可以去它的项目主页看

配置完了之后可以用 mvn的一些指令了 如下

```
docker:build 应该是编译镜像（强烈建议跟 mvn install 不然会有一个坑）

-DpushImage 加这个参数可以强制push镜像到仓库

DOCKER_HOST 加这个环境变量可以设置默认的docker主机地址 不过上面dockerHost已经填了

docker:removeImage 删除镜像吧

-DimageName=xxx 设置镜像名称

docker:tag 打tag

```
还有一些设置认证的介绍可以去看它的项目主页或者读源代码

下面就执行下

```
    mvn install docker:build docker:tag
```

可以看到类似下面的输出

```
[INFO] Copying /Users/xxx/Documents/code/github/sping-boot-docker/target/demo-0.0.1-SNAPSHOT.jar -> /Users/xxx/Documents/code/github/sping-boot-docker/target/docker/demo-0.0.1-SNAPSHOT.jar
[INFO] Building image registry.****.com/demo:demo
Step 0 : FROM docker.io/java:latest
 ---> e9de8c6faf15
Step 1 : ENV LC_ALL en_US.UTF-8
 ---> Using cache
 ---> 8600ba9f5363
Step 2 : ENV TZ 'Asia/Shanghai'
 ---> Using cache
 ---> 9416efd6b55a
Step 3 : ADD /demo-0.0.1-SNAPSHOT.jar //
 ---> 8cf7cbbd260e
Removing intermediate container 9a53dfb28f52
Step 4 : EXPOSE 8080
 ---> Running in 4f640acaf303
 ---> 3a4400f72be5
Removing intermediate container 4f640acaf303
Step 5 : CMD java -jar /demo-0.0.1-SNAPSHOT.jar
 ---> Running in e8ff98f7e9ce
 ---> 6834dbb25c33
Removing intermediate container e8ff98f7e9ce
Successfully built 6834dbb25c33
[INFO] Built registry.****.com/demo:demo
```

通过这个输出可以知道这个插件的原理大致是 install成功jar之后根据pom文件的配置去生成一个dockerfile的文件然后把jar包和dockerfile打包调用docker的api去build去push

可以看到它生成的dockerfile来验证

```
➜  sping-boot-docker  cd target/docker
➜  docker  ls
Dockerfile              demo-0.0.1-SNAPSHOT.jar
➜  docker  cat Dockerfile
FROM docker.io/java:latest
ENV LC_ALL en_US.UTF-8
ENV TZ 'Asia/Shanghai'
ADD /demo-0.0.1-SNAPSHOT.jar //
EXPOSE 8080
CMD ["java", "-jar", "/demo-0.0.1-SNAPSHOT.jar"]
➜  docker
```
这个时候去docker服务器上看看镜像的情况

```
[root@172-16-0-17 cache]# docker images | grep "demo"
registry.****.com/demo                              demo                          6834dbb25c33        5 minutes ago       655.3 MB
registry.****.com/demo                              latest                        6834dbb25c33        5 minutes ago       655.3 MB
```
可以看到有两个但是通过看它的 digest 其实是同一个镜像不同的名字而已。 因为我们有docker:tag 这样就会在生成一个latest

可以看到镜像还挺大的这主要是那个java的基础镜像大（可以自制一个小的）不过docker每次都是有cache和增量更新所以速度还是很快的

然后我们验证下能不能正常工作

```
docker run -p 6666:8080 registry.****.com/demo

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v1.3.2.BUILD-SNAPSHOT)

2015-12-21 08:30:05.303  INFO 1 --- [           main] org.beyondblog.DemoApplication           : Starting DemoApplication v0.0.1-SNAPSHOT on a734ef04ef23 with PID 1 (/demo-0.0.1-SNAPSHOT.jar started by root in /)
2015-12-21 08:30:05.307  INFO 1 --- [           main] org.beyondblog.DemoApplication           : No active profile set, falling back to default profiles: default
2015-12-21 08:30:05.463  INFO 1 --- [           main] ationConfigEmbeddedWebApplicationContext : Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@429e6a3d: startup date [Mon Dec 21 08:30:05 CST 2015]; root of context hierarchy
2015-12-21 08:30:06.471  INFO 1 --- [           main] o.s.b.f.s.DefaultListableBeanFactory     : Overriding bean definition for bean 'beanNameViewResolver' with a different definition: replacing [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration$WhitelabelErrorViewConfiguration; factoryMethodName=beanNameViewResolver; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [org/springframework/boot/autoconfigure/web/ErrorMvcAutoConfiguration$WhitelabelErrorViewConfiguration.class]] with [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter; factoryMethodName=beanNameViewResolver; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [org/springframework/boot/autoconfigure/web/WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter.class]]
2015-12-21 08:30:07.725  INFO 1 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat initialized with port(s): 8080 (http)
2015-12-21 08:30:07.745  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service Tomcat
2015-12-21 08:30:07.746  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet Engine: Apache Tomcat/8.0.30
2015-12-21 08:30:07.834  INFO 1 --- [ost-startStop-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2015-12-21 08:30:07.835  INFO 1 --- [ost-startStop-1] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 2386 ms
2015-12-21 08:30:08.106  INFO 1 --- [ost-startStop-1] o.s.b.c.e.ServletRegistrationBean        : Mapping servlet: 'dispatcherServlet' to [/]
2015-12-21 08:30:08.110  INFO 1 --- [ost-startStop-1] o.s.b.c.embedded.FilterRegistrationBean  : Mapping filter: 'characterEncodingFilter' to: [/*]
2015-12-21 08:30:08.110  INFO 1 --- [ost-startStop-1] o.s.b.c.embedded.FilterRegistrationBean  : Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
2015-12-21 08:30:08.110  INFO 1 --- [ost-startStop-1] o.s.b.c.embedded.FilterRegistrationBean  : Mapping filter: 'httpPutFormContentFilter' to: [/*]
2015-12-21 08:30:08.111  INFO 1 --- [ost-startStop-1] o.s.b.c.embedded.FilterRegistrationBean  : Mapping filter: 'requestContextFilter' to: [/*]
2015-12-21 08:30:08.523  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerAdapter : Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@429e6a3d: startup date [Mon Dec 21 08:30:05 CST 2015]; root of context hierarchy
2015-12-21 08:30:08.566  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/hello],methods=[GET]}" onto public java.lang.Object org.beyondblog.controller.HelloController.hello()
2015-12-21 08:30:08.568  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
2015-12-21 08:30:08.568  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
2015-12-21 08:30:08.587  INFO 1 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2015-12-21 08:30:08.587  INFO 1 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2015-12-21 08:30:08.613  INFO 1 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2015-12-21 08:30:08.714  INFO 1 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2015-12-21 08:30:08.782  INFO 1 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2015-12-21 08:30:08.794  INFO 1 --- [           main] org.beyondblog.DemoApplication           : Started DemoApplication in 5.045 seconds (JVM running for 7.068)
```
这个命令 有个 -p 是容器的端口和主机的端口映射 也就是通过6666访问容器的8080端口
可以在加个-d 使它能在后台跑(不过哪有就看不到输出)

先看看数据能不能出来

```
➜  docker  http "http://172.16.0.17:6666/hello"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Mon, 21 Dec 2015 00:32:20 GMT
Server: Apache-Coyote/1.1
Transfer-Encoding: chunked

{
    "code": 200,
    "messge": "Hello spring! profile=production"
}

```

好了大致是ok。先这样工头喊我去搬砖了下次在介绍如果处理日志和k8s集群管理



----
参考文章:

[0] https://spring.io/guides/gs/spring-boot-docker/

[1] https://github.com/spotify/docker-maven-plugin
