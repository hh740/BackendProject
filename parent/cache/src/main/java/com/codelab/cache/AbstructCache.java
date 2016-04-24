package com.codelab.cache;

import com.codelab.cache.callable.Callable;
import com.codelab.cache.callable.CallableCache;
import com.codelab.cache.loading.Loader;
import com.codelab.cache.loading.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * basic Cache ,refresh every 5 min
 */
public abstract class AbstructCache<K, V, T> implements LoadingCache<K, V>, CallableCache<K, V, T> {

    private static Logger logger = LoggerFactory.getLogger(AbstructCache.class);

    public static final int DEFAULT_TIMEOUT = 60 * 5 * 1000;

    public static final int DEFAULT_CAPACITY = 10000000;

    public int capacity = DEFAULT_CAPACITY;

    private int timeout = DEFAULT_TIMEOUT;

    private AtomicLong count = new AtomicLong(0);

    private ConcurrentHashMap<K, Entry<K, V>> __CACHE__ = new ConcurrentHashMap<K, Entry<K, V>>();

    private Loader<K, V> loader;

    private String namespace;

    private boolean isCallableCache;

    public AbstructCache(String namespace, Loader<K, V> loader) {
        if (loader == null)
            throw new NullPointerException("loader should not null when rereshCache is false");
        this.namespace = namespace;
        this.loader = loader;
        this.isCallableCache = false;
        try {
            init();
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error("start thread error cause:{}", e.getCause());

        }
    }

    public AbstructCache(String namespace) {
        this.namespace = namespace;
        this.isCallableCache = true;
        try {
            init();
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error("start thread error cause:{}", e.getCause());

        }
    }


    @Override
    public void init() throws Exception {
        Thread t = new Thread(new LoadingCacheLoader());
        t.setDaemon(true);
        t.setName("cache-loader-" + namespace);
        t.start();
    }

    @Override
    public void destroy() throws Exception {
        // todo stop thread & & clean
    }

    @Override
    public Map<K, V> load() throws Exception {
        Map<K, V> r = loader.load();
        for (K k : r.keySet()) {
            set(k, r.get(k));
        }
        if (logger.isInfoEnabled()) logger.info("load {} data item successfully", r.size());
        count.set(r.size());
        return r;
    }


    public V get(K key) {
        if (logger.isDebugEnabled()) logger.debug("Enter cache get entry for:{}", key);
        Entry entry = __CACHE__.get(key);
        if (entry == null) {
            if (logger.isDebugEnabled()) logger.debug("No entry found for: {}, return null", key);
            return null;
        }
        if (logger.isDebugEnabled())
            logger.debug("Local cache hit:[{}={}, ignore expiredAt, return successfully]", key, entry.getValue());
        entry.addExpiredAt(timeout);
        return (V) entry.getValue();
    }

    @Override
    public List<V> gets(Set<K> keys) {
        if (keys == null) throw new NullPointerException("keys");
        if (keys.isEmpty()) return Collections.emptyList();
        List<V> ls = new ArrayList();
        for (K k : keys) {
            V v = get(k);
            if (v != null) ls.add(v);
        }
        return ls;
    }

    public void set(K key, V value) {
        set(key, value, timeout);
    }

    public void set(K key, V value, long timeout) {
        long expiredAt = System.currentTimeMillis() + (timeout);
        Entry entry = new Entry<Object, Object>(key, value, expiredAt);
        __CACHE__.put(key, entry);
        count.incrementAndGet();
        if (logger.isDebugEnabled()) logger.debug("put new entry, key:{}, expiredAt:{}", key, expiredAt);
    }

    public void delete(K key) {
        __CACHE__.remove(key);
        count.decrementAndGet();
        if (logger.isDebugEnabled()) logger.debug("delete key:{}", key);
    }

    @Override
    public V get(K key, Callable<V, T> callable, int timeout) {
        if (isCallableCache == false)
            throw new InvalidParameterException("this cache is loading cache ,can not call the get");
        else {
            V v = get(key);
            if (v != null) return v;
            if (callable == null) return null;
            ReentrantLock lock = LockSupport.INSTANCE.getLock(key);
            try {
                lock.lock();
                long start = System.currentTimeMillis();
                T t = getResource();
                v = callable.call(t);
                returnResource(t);
                long end = System.currentTimeMillis();
                if (logger.isInfoEnabled())
                    logger.info("call data, ns:{} key:{}, time:{} ms", namespace, key, (end - start));
                long expiredAt = end + timeout;
                set(key, v, expiredAt);
                count.incrementAndGet();
                return v;
            } catch (Exception e) {
                logger.error("failed to call data, ns:" + namespace + "key:" + key, e);
                return null;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public V get(K key, Callable<V, T> callable) {
        return get(key, callable, DEFAULT_TIMEOUT);
    }


    class LoadingCacheLoader implements Runnable {

        private Logger logger = LoggerFactory.getLogger(LoadingCacheLoader.class);
        private static final long INTERVAL = 5 * 60 * 1000; // 5 min

        @Override
        public void run() {
            while (true) {

                if (logger.isInfoEnabled()) logger.info("updater start, cache size {}", count.get());
                long start = System.currentTimeMillis();

                if (isCallableCache == false) {
                    try {
                        // dose not calcualte expire time,load every time
                        load();
                        if (logger.isInfoEnabled()) logger.info("load data, ns:{}", namespace);
                    } catch (Exception e) {
                        logger.error("faild to load data, ns:" + namespace, e);
                    }

                } else {
                    try {
                        //refresh the expire time,remove the expire key
                        Iterator<Entry<K, V>> it = __CACHE__.values().iterator();
                        while (it.hasNext()) {
                            Entry<K, V> entry = it.next();
                            if (entry.isDelete() || start > entry.getExpiredAt() || count.get() > capacity) {
                                it.remove();
                            }
                        }
                    } catch (Exception e) {
                        logger.error("updater error", e);
                    }

                }
                long end = System.currentTimeMillis();
                long sleep = INTERVAL - (end - start);
                if (logger.isInfoEnabled())
                    logger.info("updater end, cache size {} time {} ms sleep {} ms", count.get(), end - start, sleep);
                if (sleep > 0)
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        logger.error("cache updater interrupt", e);
                    }

            }
        }
    }

}
