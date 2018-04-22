package com.wxn.redis.jedis.common;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;

/**
 * Redis管理类
 */
public class RedisManeger {

    /**
     * 创建单例对象
     */
    private static class StaticHolder {
        static final GenericObjectPoolConfig poolConfig;
        static final JedisPool jedisPool;

        static {
            poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(2);
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxWaitMillis(5000);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestWhileIdle(false);
//            JedisSentinelPool使用的是Sentinel架构形式下的Redis缓存服务
//            Set<String> sentinels = new HashSet<String>();
//            sentinels.add("127.0.0.1:6379");
//            jedisPool = new JedisSentinelPool("mymaster", sentinels, poolConfig);

//            SharedJedisPool采用一致性哈希实现分布式存储

//            jedisPool是普通的实现形式
            jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379);
        }

    }

    public static JedisPool getJedisPool() {
        return StaticHolder.jedisPool;
    }

    /**************  redis 操作  ****************/

    /**
     * setString
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean set(String key, String defaultValue) {
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedisPool().getResource();
            jedis.set(key, defaultValue);
            result = true;
        } finally {
            if (jedis != null) jedis.close();
        }
        return result;
    }


    /**
     * Jedis出异常的时候，回收jedis对象资源
     * 此方法已被丢弃，改用close替换，
     *
     * @param jedis
     */
    private static void returnBrokenResource(Jedis jedis) {
        if (null != jedis) {
            getJedisPool().returnBrokenResource(jedis);
        }
    }
}
