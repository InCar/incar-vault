# incar-vault
![incar-vault](https://travis-ci.org/InCar/incar-vault.svg?branch=master)

项目中开源共享的部分,都先放于此
如果其中一个部分有比较高的价值,再拆分出来,形成单独的项目

[maven](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.incarcloud%22)
```xml
<dependency>
    <groupId>com.incarcloud</groupId>
    <artifactId>incar-vault</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Wiki
本项目包含的内容参考wiki

https://github.com/InCar/incar-vault/wiki

## prerequisite
- [JDK 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Gradle 4.5+](http://gradle.org/gradle-download/)
```shell
# 执行以下命令检查环境
java -version
gradle --version
```

## Configuration
Users from China can copy [gradle-sample.properties](https://github.com/InCar/ac-func-tion/blob/master/gradle-sample.properties) to `gradle.properties` for accelerating downloading speed via ali-yun mirror

## Compile
```SHELL
gradle assemble
```

## License
MIT