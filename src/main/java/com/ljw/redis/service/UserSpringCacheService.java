package com.ljw.redis.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.redis.entity.User;

public interface UserSpringCacheService extends IService<User> {
    User findUserById(Integer id);

    User updateUser(User obj);

    void deleteUser(Integer id);

    String checkTTL1Hour();

    String checkTTL1Day();

    String deafultTTL();

    String caching();
}
