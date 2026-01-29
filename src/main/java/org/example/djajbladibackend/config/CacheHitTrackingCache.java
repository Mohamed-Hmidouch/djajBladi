package org.example.djajbladibackend.config;

import org.springframework.cache.Cache;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;

/**
 * Cache decorator that records HIT/MISS for the current request (ThreadLocal).
 * Used to add X-Cache-Status response header so curl tests can verify Redis is used.
 */
public class CacheHitTrackingCache implements Cache {

    private final Cache delegate;

    private static final ThreadLocal<String> CACHE_STATUS = new ThreadLocal<>();

    public static void setStatus(String status) {
        CACHE_STATUS.set(status);
    }

    public static String getStatus() {
        return CACHE_STATUS.get();
    }

    public static void clearStatus() {
        CACHE_STATUS.remove();
    }

    public CacheHitTrackingCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    @NonNull
    public String getName() {
        return delegate.getName();
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper v = delegate.get(key);
        if (v != null) {
            setStatus("HIT");
            return v;
        }
        setStatus("MISS");
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T v = delegate.get(key, type);
        if (v != null) {
            setStatus("HIT");
            return v;
        }
        setStatus("MISS");
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper v = delegate.get(key);
        if (v != null) {
            setStatus("HIT");
            @SuppressWarnings("unchecked")
            T value = (T) v.get();
            return value;
        }
        setStatus("MISS");
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        delegate.put(key, value);
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
