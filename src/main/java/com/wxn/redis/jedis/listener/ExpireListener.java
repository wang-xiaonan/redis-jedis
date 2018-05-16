package com.wxn.redis.jedis.listener;

import redis.clients.jedis.JedisPubSub;

/**
 * @Author: wangxiaonan
 * @Date: 2018/5/16
 **/
public class ExpireListener extends JedisPubSub {
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        super.onPSubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        super.onPMessage(pattern, channel, message);
    }
}
