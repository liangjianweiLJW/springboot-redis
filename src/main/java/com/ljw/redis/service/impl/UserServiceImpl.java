package com.ljw.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljw.redis.entity.User;
import com.ljw.redis.mapper.UserMapper;
import com.ljw.redis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

/**
 * @Description: todo
 * @Author: jianweil
 * @date: 2021/9/24 10:37
 */
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
