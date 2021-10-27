---
theme: channing-cyan
highlight: atom-one-dark
---
# SpringBoot集成Mybatis-Plus,Redis和Swagger

## 项目结构
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/299e4912d4b64d97860229e8581d0c78~tplv-k3u1fbpfcp-watermark.image?)
## pom依赖
```java
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ljw</groupId>
    <artifactId>redis</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>redis</name>
    <description>redis</description>
    <properties>
        <java.version>1.8</java.version>
        <!--swagger-bootstrap-ui-->
        <springfox.swagger.version>2.8.0</springfox.swagger.version>
        <swagger-bootstrap-ui.version>1.9.6</swagger-bootstrap-ui.version>
    </properties>
    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--        spring session-->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-core</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!--        测试-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!--        redis-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>2.5.5</version>
        </dependency>

        <!--mysql驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>

        <!-- API管理与测试的依赖 -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>${springfox.swagger.version}</version>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>${springfox.swagger.version}</version>
        </dependency>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>swagger-bootstrap-ui</artifactId>
            <version>${swagger-bootstrap-ui.version}</version>
        </dependency>

        <!--mybatis-plus 持久层-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.3.4</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```


## 配置application.properties
```java
spring.application.name=spring-boot-mybatis-redis
server.port=8080
###############################数据库配置
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/ljw?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
spring.datasource.username=root
spring.datasource.password=root
###############################日志级别
logging.level.com.ljw=debug
###############################是否开启swagger2
spring.swagger2.enabled=true
###############################Redis 配置
## Redis数据库索引（默认为0）
spring.redis.database=0
## Redis服务器地址
spring.redis.host=127.0.0.1
## Redis服务器连接端口
spring.redis.port=6379
## Redis服务器连接密码（默认为空）
spring.redis.password=
###############################mybatis-plus配置
mybatis-plus.mapper-locations:classpath*:com/ljw/redis/mapper/xml/*.xml
mybatis-plus.typeAliasesPackage:com.ljw.**.entity.**
# 是否开启自动驼峰命名规则（camel case）映射
mybatis-plus.configuration.mapUnderscoreToCamelCase:true
# 控制台 打印sql
mybatis-plus.configuration.log-impl:org.apache.ibatis.logging.stdout.StdOutImpl
# 数据库类型
mybatis-plus.global-config.db-config.db-type:mysql
# 主键id  AUTO(0):自增ID，NONE(1)：未设置主键类型，INPUT(2)：用户输入ID，ID_WORKER(3)：全局唯一ID (64位idWorker)，UUID(4)：全局唯一ID (32位UUID)，ID_WORKER_STR(5)：字符串全局唯一ID (64位idWorker 的字符串表示)
mybatis-plus.global-config.db-config.id-type:auto
#驼峰下划线转换
mybatis-plus.global-config.db-config.db-column-underline:true
mybatis-plus.global-config.db-config.logic-delete-value:1
mybatis-plus.global-config.db-config.logic-not-delete-value:0
```






## 配置redis序列化器


为什么要重写Redis序列化方式，改为Json呢？  
因为RedisTemplate默认使用的是JdkSerializationRedisSerializer，会出现2个问题：  
1. 被序列化的对象必须实现Serializable接口
```java 
@TableName("users")
public class User implements  Serializable {
```  
2. 被序列化会出现乱码,导致value值可读性差. 

优化重写Redis的序列化，改为Json方式
```java
@Configuration
public class RedisConfiguration {
    /**
     * 重写Redis序列化方式，使用Json方式:
     * 当我们的数据存储到Redis的时候，我们的键（key）和值（value）都是通过Spring提供的Serializer序列化到Redis的。
     * RedisTemplate默认使用的是JdkSerializationRedisSerializer，
     * StringRedisTemplate默认使用的是StringRedisSerializer。
     * <p>
     * Spring Data JPA为我们提供了下面的Serializer：
     * GenericToStringSerializer、Jackson2JsonRedisSerializer、
     * JacksonJsonRedisSerializer、JdkSerializationRedisSerializer、
     * OxmSerializer、StringRedisSerializer。
     * 在此我们将自己配置RedisTemplate并定义Serializer。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //创建一个json的序列化对象
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        //设置value的序列化方式json
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        //设置key序列化方式string
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //设置hash key序列化方式string
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //设置hash value的序列化方式json
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
```
## 配置swagger
```java

/**
 * 访问：http://localhost:8080/doc.html
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value(value = "${spring.swagger2.enabled}")
    private Boolean swaggerEnabled;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ljw.redis"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("接口文档")
                .description("Spring Boot redis")
                .termsOfServiceUrl("www.baidu.com")
                .version("1.0")
                .build();
    }
}
```

## 控制器controller

```java
@Api(description = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("数据库初始化100条数据")
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public void init() {
        for (int i = 0; i < 100; i++) {
            Random rand = new Random();
            User user = new User();
            String temp = "un" + i;
            user.setUsername(temp);
            user.setPassword(temp);
            int n = rand.nextInt(2);
            user.setSex((byte) n);
            userService.createUser(user);
        }
    }

    @ApiOperation("单个用户查询，按userid查用户信息")
    @RequestMapping(value = "/findById/{id}", method = RequestMethod.GET)
    public UserVO findById(@PathVariable int id) {
        User user = this.userService.findUserById(id);
        UserVO userVO = new UserVO();
        if (user!=null){
            BeanUtils.copyProperties(user, userVO);
        }
        return userVO;
    }

    @ApiOperation("修改某条数据")
    @PostMapping(value = "/updateUser")
    public void updateUser(@RequestBody UserVO obj) {
        User user = new User();
        BeanUtils.copyProperties(obj, user);
        userService.updateUser(user);
    }

}
```

## 业务接口service
```java
package com.ljw.redis.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.redis.entity.User;

public interface UserService extends IService<User> {
    void createUser(User obj);

    void updateUser(User obj);

    User findUserById(Integer userid);
}
```

## 业务接口实现ServiceImpl
```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    public static final String CACHE_KEY_USER = "user:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void createUser(User obj) {
        this.userMapper.insert(obj);

        //缓存key
        String key = CACHE_KEY_USER + obj.getId();
        //到数据库里面，重新捞出新数据出来，做缓存
        obj = this.userMapper.selectById(obj.getId());

        //opsForValue代表了Redis的String数据结构
        //set代表了redis的SET命令
        redisTemplate.opsForValue().set(key, obj);
    }

    @Override
    public void updateUser(User obj) {
        //1.先直接修改数据库
        this.userMapper.updateById(obj);
        //2.再修改缓存
        //缓存key
        String key = CACHE_KEY_USER + obj.getId();
        obj = this.userMapper.selectById(obj.getId());
        //修改也是用SET命令，重新设置，Redis 没有update操作，都是重新设置新值
        redisTemplate.opsForValue().set(key, obj);
    }

    @Override
    public User findUserById(Integer userid) {
        ValueOperations<String, User> operations = redisTemplate.opsForValue();
        //缓存key
        String key = CACHE_KEY_USER + userid;
        //1.先去redis查 ，如果查到直接返回，没有的话直接去数据库捞
        //Redis 用了GET命令
        User user = operations.get(key);

        //2.redis没有的话，直接去数据库捞
        if (user == null) {
            user = this.userMapper.selectById(userid);
            //由于redis没有才到数据库捞，所以必须把捞到的数据写入redis，方便下次查询能redis命中。
            operations.set(key, user);
        }
        return user;
    }

}
```

## 持久层mapper
```java
@Repository
public interface UserMapper extends BaseMapper<User> {
}
```

## 持久层XML
```JAVA
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ljw.redis.mapper.UserMapper">
</mapper>
```

## 实体entity
```java
@TableName("users")
@Data
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别 0=女 1=男
     */
    private Byte sex;

    /**
     * 删除标志，默认0不删除，1删除
     */
    private Byte deleted;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
```

## 展示VO
```Java
@ApiModel(value = "用户信息")
@Data
public class UserVO {
    @ApiModelProperty(value = "用户ID")
    private Integer id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "性别 0=女 1=男 ")
    private Byte sex;

    @ApiModelProperty(value = "删除标志，默认0不删除，1删除")
    private Byte deleted;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
```
## sql脚本


```sql
-- ljw.users definition

CREATE TABLE `users` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(50) NOT NULL DEFAULT '' COMMENT '密码',
  `sex` tinyint NOT NULL DEFAULT '0' COMMENT '性别 0=女 1=男 ',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志，默认0不删除，1删除',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1303 DEFAULT CHARSET=utf8 COMMENT='用户表';
```

## 启动类

```java
//指定要扫描的Mapper类的包的路径
@MapperScan("com.ljw.redis.mapper")
@EnableTransactionManagement
@SpringBootApplication
public class RedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }

}
```

## 表结构
```Java
CREATE TABLE `users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(50) NOT NULL DEFAULT '' COMMENT '密码',
  `sex` tinyint(4) NOT NULL DEFAULT '0' COMMENT '性别 0=女 1=男 ',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '删除标志，默认0不删除，1删除',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8 COMMENT='用户表';
```


## 体验
访问：http://localhost:8080/doc.html ，先初始化数据，再请求其他接口

问题1：默认jdk序列化器进redis的数据的对象必须实现序列化Serializable接口

```java  
解决：配置自定义的序列化器
```
问题2：如果连接不了redis，修改redis配置文件的bind参数
```java  
vi redis.conf
bind 0.0.0.0
```


