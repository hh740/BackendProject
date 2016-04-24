package com.codelab.cache;


public class Entry<K,V> {
    protected K key;
    protected V value;
    protected long expiredAt;
    protected boolean delete;

    public Entry(K key, V value, long expiredAt) {
        if (key == null) throw new NullPointerException("key");
        this.key = key;
        this.value = value;
        if (expiredAt != 0) this.expiredAt = expiredAt;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
    public long getExpiredAt(){
        return expiredAt;
    }
    public boolean isDelete(){
        return delete;
    }
    public void addExpiredAt(long time){
        expiredAt = expiredAt + time;
    }
}
