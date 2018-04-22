package com.wxn.redis.jedis.controller;

import com.wxn.redis.jedis.common.RedisManeger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/redis")
public class RedisController {

    @ResponseBody
    @RequestMapping(value = "/set", method = RequestMethod.GET, produces = {"application/json;charset=utf-8"})
    public String setString(@RequestParam String key, @RequestParam String value) {
        boolean rs = RedisManeger.set(key, value);
        return String.valueOf(rs);
    }
}
