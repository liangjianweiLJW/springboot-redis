package com.ljw.redis.controller;


import com.ljw.redis.entity.User;
import com.ljw.redis.service.UserSpringCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(description = "用户接口（SpringCache）")
@RestController
@RequestMapping("/userSpringCache")
public class UserSpringCacheController {

    @Autowired
    private UserSpringCacheService userSpringCacheService;


    @ApiOperation("单个用户查询，按userid查用户信息")
    @RequestMapping(value = "/findById/{id}", method = RequestMethod.GET)
    public UserVO findById(@PathVariable int id) {
        User user = this.userSpringCacheService.findUserById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @ApiOperation("修改某条数据")
    @PostMapping(value = "/updateUser")
    public void updateUser(@RequestBody UserVO obj) {
        User user = new User();
        BeanUtils.copyProperties(obj, user);
        userSpringCacheService.updateUser(user);
    }

    @ApiOperation("按id删除用户")
    @RequestMapping(value = "/del/{id}", method = RequestMethod.GET)
    public void deleteUser(@PathVariable int id) {
        this.userSpringCacheService.deleteUser(id);
    }


    @ApiOperation("查看缓存时间是否1个小时")
    @RequestMapping(value = "/checkTTL1Hour", method = RequestMethod.GET)
    public String checkTTL1Hour() {
        String ttl = this.userSpringCacheService.checkTTL1Hour();
        return ttl;
    }

    @ApiOperation("查看缓存时间是否1天")
    @RequestMapping(value = "/checkTTL1Day", method = RequestMethod.GET)
    public String checkTTL1Day() {
        String ttl = this.userSpringCacheService.checkTTL1Day();
        return ttl;
    }


    @ApiOperation("默认ttl")
    @RequestMapping(value = "/deafultTTL", method = RequestMethod.GET)
    public String deafultTTL() {
        String ttl = this.userSpringCacheService.deafultTTL();
        return ttl;
    }

    @ApiOperation("caching使用")
    @RequestMapping(value = "/caching", method = RequestMethod.GET)
    public String caching() {
        String ttl = this.userSpringCacheService.caching();
        return ttl;
    }


}
