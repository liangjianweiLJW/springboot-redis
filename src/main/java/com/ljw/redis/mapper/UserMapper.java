package com.ljw.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ljw.redis.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
}