package com.codelab.cache.redis;


import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JVM Memory cache, consider:
 * 1. Global management
 * 2. No Persistence
 * 3. Least Recently Used
 * 4. Capacity
 * 5. Namespace support
 * 6. Based ConcurrentLinkedHashMap
 *
 *
 * @author edwin
 * @since 16 Jan 2015
 */
public class LocalCachePool {

    private static Logger logger = LoggerFactory.getLogger(LocalCachePool.class);

    public static final int DEFAULT_CAPACITY = 20000000;
    public static final int DEFAULT_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    public static final NameSpace DEFAULT_NAMESPACE = NameSpace.DEFAULT;
    public static final char NAMESPACE_SEPARATOR = '.';

    private Map<NameSpace, LocalCache> _NS;
    private Map<String, Entry> _CACHE;

    private int capacity;
    private int timeout;

    private Locks locks = new Locks();

    private Status status = new Status();

    private static LocalCachePool instance = new LocalCachePool();

    private LocalCachePool() {
        this.capacity = DEFAULT_CAPACITY;
        this.timeout = DEFAULT_TIMEOUT;
        _NS = new ConcurrentHashMap<NameSpace, LocalCache>();
        _CACHE = new ConcurrentLinkedHashMap.Builder<String, Entry>().maximumWeightedCapacity(capacity).build();
        status.start();
    }

    public static LocalCachePool getInstance(){
        return instance;
    }


    public LocalCache getCache(){
        return getCache(DEFAULT_NAMESPACE);
    }


    public LocalCache getCache(NameSpace namespace){
        if(namespace == null) throw new NullPointerException("namespace");
        LocalCache cache = _NS.get(namespace);
        if(cache == null){
            synchronized (_NS){
                cache = _NS.get(namespace);
                if(cache == null){
                    cache = new LocalCache(namespace, this);
                    _NS.put(namespace, cache);
                }
            }
        }
        return cache;
    }

    Object get(String key){
        return get(key, true);
    }

    Object get(String key, boolean stat){
        Entry entry = _CACHE.get(key);
        if(entry == null){
            if(stat) status.miss();
            return null;
        }
        if(System.currentTimeMillis() > entry.expiredAt){
            if(logger.isDebugEnabled()) logger.debug("Found expired key:{}, delete", key);
            _CACHE.remove(key);
            if(stat) status.expire();
            return null;
        }
        status.hit();
        return entry.value;
    }

    void put(String key, Object value){
        Entry entry = new Entry(value, System.currentTimeMillis() + timeout);
        _CACHE.put(key, entry);
    }

    <V> V get(String key, Callable<V> callable) throws Exception {
        if(StringUtils.isBlank(key)) throw new NullPointerException("key can't be null");
        if(callable == null) throw new NullPointerException("callable can't be null");
        Object v = get(key);
        if(v == null){
            ReentrantLock lock = locks.getLock(key);
            try{
                lock.lock();
                v = get(key, false);
                if(v == null){
                    v = callable.call();
                    put(key, v);
                }
            }finally {
                lock.unlock();
            }
        }
        return (V) v;
    }

    String getFinalKey(NameSpace namespace, String key){
        if(namespace == null || StringUtils.isBlank(key)) throw new NullPointerException("namespace & key");
        StringBuilder sb = new StringBuilder();
        sb.append(namespace.name()).append(NAMESPACE_SEPARATOR).append(key);
        return sb.toString();
    }


    Status getStatus(){
        status.setActiveCount(_CACHE.size());
        return status;
    }

    private class Locks {
        public static final int DEFAULT_LOCK_COUNT = 1024;

        private ReentrantLock[] locks = new ReentrantLock[DEFAULT_LOCK_COUNT];

        private Locks() {
            for (int i = 0; i < DEFAULT_LOCK_COUNT; i++) {
                locks[i] = new ReentrantLock();
            }
        }

        public ReentrantLock getLock(Object key) {
            if (key == null) throw new NullPointerException();
            int index = Math.abs(key.hashCode()) % DEFAULT_LOCK_COUNT;
            if (logger.isDebugEnabled())
                logger.debug("Get lock index:{}", index);
            return locks[index];
        }
    }

    private class Entry{

        Object value;
        long expiredAt;

        Entry(Object value, long expiredAt) {
            this.value = value;
            this.expiredAt = expiredAt;
        }
    }

    class Status extends Thread{
        private long hitCount;
        private long missCount;
        private long expireCount;
        private int activeCount;

        public void hit(){
            hitCount++;
        }

        public void miss(){
            missCount++;
        }

        public void expire(){
            expireCount++;
        }

        public long getHitCount() {
            return hitCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public long getExpireCount() {
            return expireCount;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }

        @Override
        public void run() {

            while(true){
                try {
                    //30s' output load condition
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted", e);
                }

                if(logger.isWarnEnabled()) logger.warn(getStatus().toString());
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("LocalCache Status[");
            sb.append("hitCount:").append(hitCount);
            sb.append(", missCount:").append(missCount);
            sb.append(", expireCount:").append(expireCount);
            sb.append(", activeCount:").append(activeCount);
            long totalCount = hitCount + missCount + expireCount;
            if(totalCount > 0)
                sb.append(", hitRate:").append(((float)hitCount/totalCount)*100).append("%");
            sb.append("]");
            return sb.toString();
        }
    }
}
