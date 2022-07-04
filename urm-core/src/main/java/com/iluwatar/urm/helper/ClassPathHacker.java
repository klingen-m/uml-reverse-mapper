package com.iluwatar.urm.helper;

import java.io.IOException;
import java.io.File;
import java.net.URL;

public class ClassPathHacker {

    private static ClassPathHacker INSTANCE;
    private final MyClassloader classLoader;

    private ClassPathHacker() {
        classLoader = new MyClassloader(new URL[0], this.getClass().getClassLoader());
    }

    public MyClassloader getClassLoader(){
        return classLoader;
    }

    public static ClassPathHacker getInstance(){
        if(INSTANCE==null){
            INSTANCE = new ClassPathHacker();
        }
        return INSTANCE;
    }

    public void addFile(File file) throws IOException {
        if(file.exists()) {
            URL url = file.toURI().toURL();
            classLoader.addURL(url);
        }

    }//end method

}//end class
