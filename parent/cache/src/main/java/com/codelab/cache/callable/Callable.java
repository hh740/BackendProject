package com.codelab.cache.callable;

public interface Callable<V,T>  {

    public V call(T t) throws Exception;
}
