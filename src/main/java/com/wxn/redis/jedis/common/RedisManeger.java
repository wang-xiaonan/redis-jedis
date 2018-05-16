package com.wxn.redis.jedis.common;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

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

    /**************  redis operations start  ****************/

    /**
     * setString
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean set(String key, String defaultValue) {
        boolean result;
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(key, defaultValue);
            result = true;
        }
        return result;
    }

    public static String get(String key) {
        String result = null;
        if (key == null || "".equals(key)) {
            return result;
        }
        try (Jedis jedis = getJedisPool().getResource()) {
            result = jedis.get(key);
        }
        return result;
    }

    /**
     * 1: the timeout was set. 0: the timeout was not set
     *
     * @param key
     * @param second
     * @return
     */
    public static long expiry(String key, int second) {
        long result = 0;
        if (null == key || "".equals(key)) {
            return result;
        }
        try (Jedis jedis = getJedisPool().getResource()) {
            result = jedis.expire(key, second);
        }
        return result;
    }

    /**
     * 1: the timeout was set. 0: the timeout was not set
     *
     * @param key
     * @param unixTime Date.getTime()
     * @return
     */
    public static long expiryAt(String key, long unixTime) {
        long result = 0;
        if (null == key || "".equals(key)) {
            return 0;
        }
        try (Jedis jedis = getJedisPool().getResource()) {
            result = jedis.expireAt(key, unixTime);
        }
        return result;
    }

    public static void psubscribe(JedisPubSub listener, String... patterns) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.psubscribe(listener, patterns);
        }
    }

    private static final String KEY_PREFIX = "lock@";

    /**
     * 分布式锁
     *
     * @param lockName
     * @param waitTimeoutInMS
     * @param lockTimeout
     * @return
     */
    public static String lock(String lockName, long waitTimeoutInMS, int lockTimeout) {
        if (lockName == null || "".equals(lockName)) {
            return null;
        }
        String lockKey = KEY_PREFIX + lockName;
        String token = null;
        String identifier = UUID.randomUUID().toString();
        // 获取锁等待截止时间
        long endTime = System.currentTimeMillis() + waitTimeoutInMS;
        try (Jedis jedis = getJedisPool().getResource()) {
            while (System.currentTimeMillis() < endTime) {
                if (jedis.setnx(lockKey, identifier) == 1) {
                    jedis.expire(lockKey, lockTimeout);
                    token = identifier;
                }

                // 防止锁不失效
                if (jedis.ttl(lockKey) == -1) {
                    jedis.expire(lockKey, lockTimeout);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("获取锁失败：" + e.getMessage());
        }
        return token;
    }

    /**
     * 释放锁
     *
     * @param lockName
     * @param token
     * @return
     */
    public static boolean releaseLock(String lockName, String token) {
        boolean isRelease = false;
        if (lockName == null || "".equals(lockName) || token == null || "".equals(token)) {
            return isRelease;
        }
        String lockKey = KEY_PREFIX + lockName;
        try (Jedis jedis = getJedisPool().getResource()) {
            while (true) {
                jedis.watch(lockKey);
                if (token.equals(jedis.get(lockKey))) {
                    Transaction tx = jedis.multi();
                    tx.del(lockKey);
                    List<Object> ret = tx.exec();
                    if (ret == null) {
                        continue;
                    }
                    isRelease = true;
                }
                jedis.unwatch();
                break;
            }
        } catch (Exception e) {
            System.out.println("释放锁失败");
        }

        return isRelease;
    }
    /**************  redis operations end  ****************/

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
