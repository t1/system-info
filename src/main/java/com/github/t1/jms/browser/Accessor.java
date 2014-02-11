package com.github.t1.jms.browser;

import java.net.URI;

public interface Accessor<T> {
    public String title(T element);

    public URI link(T element);
}
