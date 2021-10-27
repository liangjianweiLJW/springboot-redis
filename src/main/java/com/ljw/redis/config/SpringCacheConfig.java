package com.ljw.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * spring cache 配置
 */
@EnableConfigurationProperties(CacheProperties.class)
@Configuration
@EnableCaching
public class SpringCacheConfig {

    @Autowired
    private CacheProperties cacheProperties;

    /**
     * 自定义注解的缓存配置
     * <p>
     * 配置使用注解的时候缓存配置，默认是序列化反序列化的形式，加上此配置则为 json 形式
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                // 默认策略，只要缓存注解的value不是下面map自定义的就使用这个默认过期时间 10分钟
                this.getRedisCacheConfigurationWithTtl(cacheProperties.getRedis().getTimeToLive()),
                //	@Cacheable(value = "redisExpire1h",key = "'a'+#itemCode") 就是使用缓存名称为redisExpire1h的配置，过期时间1小时。。可自定义多个
                this.getRedisCacheConfigurationMap()
        );
    }

    /**
     * 注解上的value对应的是cacheManager中的redisCacheConfigurationMap中的配置（map可以放多个配置），
     * 这里指定的是redisExpire1h；
     * 不写或者匹配不上，使用的是cacheManager中默认的defaultCacheConfig
     *
     * @return
     */
    private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap() {
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
        //期时间1小时
        redisCacheConfigurationMap.put("redisExpire1h", this.getRedisCacheConfigurationWithTtl(Duration.ofMillis(3600000L)));
        //过期时间一天
        redisCacheConfigurationMap.put("redisExpire1d", this.getRedisCacheConfigurationWithTtl(Duration.ofMillis(86400000L)));
        return redisCacheConfigurationMap;
    }

    /**
     * 配置不同缓存区的配置信息。可根据业务区分
     *
     * @param duration
     * @return
     */
    private RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Duration duration) {
        //获取默认缓存配置的
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                //设置每个缓存区的过期时间
                .entryTtl(duration)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        //将配置文件中所有的配置对应在CacheProperties类，想要都生效，这里要重新赋值config
       /* if (redisProperties.getKeyPrefix() != null) {
            //默认指定为cacheNames为key的前缀 ：@CacheConfig(cacheNames = {"user"})和@Cacheable(cacheNames = "redisExpire1h", key = "'checkTTL1Hour'")的前缀为user:: 和redisExpire1h：：
            //添加自定义前缀，如果这里或配置文件application.properties指定前缀则覆盖注解的前缀
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }*/
        if (!redisProperties.isCacheNullValues()) {
            //表示不允许缓存空值
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            //  不使用默认前缀
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
