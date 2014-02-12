package com.github.t1.jms.browser;

public class MapMetaData {
    private final String pageTitle;
    private final String keyTitle;
    private final String valueTitle;

    public MapMetaData(String pageTitle, String keyTitle, String valueTitle) {
        this.pageTitle = pageTitle;
        this.keyTitle = keyTitle;
        this.valueTitle = valueTitle;
    }

    public String pageTitle() {
        return pageTitle;
    }

    public String keyTitle() {
        return keyTitle;
    }

    public String valueTitle() {
        return valueTitle;
    }
}
