package com.iluwatar.urm.helper;

import java.net.URL;
import java.net.URLClassLoader;

public final class MyClassloader extends URLClassLoader {
    public MyClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }
}