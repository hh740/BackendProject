package com.codelab.cache.loading;

import com.codelab.cache.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基本的loading Cache
 * 5分钟刷新一次缓存,重新loader内容
 */
public class LocalLoadingCache<K, V>  implements LoadingCache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(LocalLoadingCache.class);

    public static final int DEFAULT_TIMEOUT = 60 * 5; // 5 minutes

    protected int timeout = DEFAULT_TIMEOUT;

    protected ConcurrentHashMap<K, Entry<K, V>> __CACHE__ = new ConcurrentHashMap<K, Entry<K, V>>();

    private Loader<K, V> loader;

    private String namespace;

    public LocalLoadingCache(String namespace, Loader<K, V> loader) {
        if(loader == null) throw new NullPointerException("loader");
        this.namespace = namespace;
        this.loader = loader;
    }


    @Override
    public void init() throws Exception {
        load();
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
    public void load() throws Exception {
        Map<K, V> r = loader.load();
        for(K k : r.keySet()){
            set(k, r.get(k));
        }
        if(logger.isInfoEnabled()) logger.info("load {} data item successfully", r.size());
    }

    /**
     * Ignore the expire settings
     * @param key
     * @return
     */
    @Override
    public V get(K key) {
        if (logger.isDebugEnabled()) logger.debug("Enter cache get entry for:{}", key);
        Entry entry = __CACHE__.get(key);
        if (entry == null) {
            if (logger.isDebugEnabled()) logger.debug("No entry found for: {}, return null", key);
            return null;
        }
        if (logger.isDebugEnabled()) logger.debug("Local cache hit:[{}={}, ignore expiredAt, return successfully]", key, entry.getValue());
        return (V)entry.getValue();
    }

    @Override
    public void set(K key, V value) {
        set(key, value, timeout);
    }

    @Override
    public void set(K key, V value, int timeout) {
        long expiredAt = System.currentTimeMillis() + (timeout * 1000);
        Entry entry = new Entry<Object, Object>(key, value, expiredAt);
        __CACHE__.put(key, entry);
        if (logger.isDebugEnabled()) logger.debug("put new entry, key:{}, expiredAt:{}", key, expiredAt);
    }

    @Override
    public void delete(K key) {
        __CACHE__.remove(key);
        if (logger.isDebugEnabled()) logger.debug("delete key:{}", key);
    }


    class LoadingCacheLoader implements Runnable{

        private Logger logger = LoggerFactory.getLogger(LoadingCacheLoader.class);
        private static final long INTERVAL = 1000 * 60 * 5; // 5 min

        @Override
        public void run() {
            while(true){
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    logger.error("cache loader interrupt", e);
                }
                try {
                    load();
                    if(logger.isInfoEnabled()) logger.info("load data, ns:{}", namespace);
                }catch (Exception e){
                    logger.error("faild to load data, ns:" + namespace, e);
                }
            }
        }
    }


    class Entry<K, V> {
        protected K key;
        protected V value;
        protected long expiredAt;
        protected boolean delete;

        Entry(K key, V value, long expiredAt) {
            if(key == null) throw new NullPointerException("key");
            this.key = key;
            this.value = value;
            if(expiredAt != 0) this.expiredAt = expiredAt;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
