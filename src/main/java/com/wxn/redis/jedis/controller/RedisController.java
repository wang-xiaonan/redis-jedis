package com.wxn.redis.jedis.controller;

import com.wxn.redis.jedis.common.RedisManeger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping(value = "/redis")
public class RedisController {

    @ResponseBody
    @RequestMapping(value = "/set", method = RequestMethod.GET, produces = {"application/json;charset=utf-8"})
    public String setString(@RequestParam String key, @RequestParam String value) {
//        boolean rs = RedisManeger.set(key, value);
        Date d = new Date(2018, 3, 4, 5, 20, 22);
        System.out.print(new Long(d.getTime()).intValue());
        long rs = RedisManeger.expiryAt("12345", new Long(d.getTime()).intValue());
        return String.valueOf(rs);
    }
}
