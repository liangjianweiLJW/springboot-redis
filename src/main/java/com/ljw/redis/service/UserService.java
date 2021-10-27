package com.ljw.redis.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.redis.entity.User;

public interface UserService extends IService<User> {
    void createUser(User obj);

    void updateUser(User obj);

    User findUserById(Integer userid);
}
