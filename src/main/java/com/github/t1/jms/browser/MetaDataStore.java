package com.github.t1.jms.browser;

import java.util.*;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class MetaDataStore {
    private final Map<Object, Object> map = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T, M extends MetaData<T>> M get(T key) {
        return (M) map.get(key);
    }

    public <T> void put(T key, MetaData<T> value) {
        map.put(key, value);
    }
}
