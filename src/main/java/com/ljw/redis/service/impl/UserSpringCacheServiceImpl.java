package com.ljw.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljw.redis.entity.User;
import com.ljw.redis.mapper.UserMapper;
import com.ljw.redis.service.UserSpringCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Description: todo
 * @Author: jianweil
 * @date: 2021/9/24 10:37
 */
@Service
@CacheConfig(cacheNames = {"user"})
@Slf4j
public class UserSpringCacheServiceImpl extends ServiceImpl<UserMapper, User> implements UserSpringCacheService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 默认10分钟，key为配置文件配置的前缀+id
     * 或  @CacheConfig(cacheNames = {"user"})配置的 user::id
     *
     * @param id
     * @return
     */
    @Override
    @Cacheable(key = "#id")
    public User findUserById(Integer id) {
        return this.userMapper.selectById(id);
    }

    /**
     * 更新会修改ttl为10分钟
     *
     * @return
     */
    @Override
    @CachePut(key = "#obj.id")
    public User updateUser(User obj) {
        this.userMapper.updateById(obj);
        return this.userMapper.selectById(obj.getId());
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteUser(Integer id) {
        User user = new User();
        user.setId(id);
        user.setDeleted((byte) 1);
        this.userMapper.updateById(user);
    }

    /**
     * 过期时间使用redisExpire1h缓存区的过期时间
     * key->   redisExpire1h::checkTTL1Hour
     *
     * @return
     */
    @Override
    @Cacheable(cacheNames = "redisExpire1h", key = "'checkTTL1Hour'")
    public String checkTTL1Hour() {
        Long expire = redisTemplate.getExpire("checkTTL1Hour");
        log.info("checkTTL1Hour:" + expire);
        return String.valueOf(expire);
    }

    /**
     * 过期时间使用redisExpire1d缓存区的过期时间
     * key->   redisExpire1d::checkTTL1Day
     *
     * @return
     */
    @Override
    @Cacheable(cacheNames = "redisExpire1d", key = "'checkTTL1Day'")
    public String checkTTL1Day() {
        Long expire = redisTemplate.getExpire("checkTTL1Day");
        log.info("checkTTL1Hour:" + expire);
        return String.valueOf(expire);
    }

    /**
     * 这个缓存名没有配置noExistName ，默认ttl
     * key->   noExistName::deafultTTL 以具体的noExistName为key
     *
     * @return
     */
    @Override
    @Cacheable(cacheNames = "noExistName", key = "'deafultTTL'")
    public String deafultTTL() {
        Long expire = redisTemplate.getExpire("deafultTTL");
        log.info("checkTTL1Hour:" + expire);
        return String.valueOf(expire);
    }


    /**
     * @Caching注解可以让我们在一个方法或者类上同时指定多个Spring Cache相关的注解。
     * 其拥有三个属性：cacheable、put和evict，分别用于指定@Cacheable、@CachePut和@CacheEvict。
     */
    @Override
    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "redisExpire1h", key = "'1002'"),
                    @Cacheable(cacheNames = "redisExpire1d", key = "'1002'")
            },
            evict = {
                    @CacheEvict(value = "CacheEvict1", key = "'1002'"),
                    @CacheEvict(value = "CacheEvict2", key = "'1002'")
            })
    public String caching() {
        redisTemplate.opsForValue().set("CacheEvict1::1002", "Caching");
        redisTemplate.opsForValue().set("CacheEvict2::1002", "Caching");
        return "Caching";
    }

}
