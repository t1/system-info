package com.github.t1.jms.browser;

import java.util.*;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class MetaDataStore {
    private final Map<Object, Object> map = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) map.get(key);
    }

    public void put(Object key, Object value) {
        map.put(key, value);
    }
}
