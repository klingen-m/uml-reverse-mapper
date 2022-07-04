package com.iluwatar.urm.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileUtility{
    public static List<File> listDirectory(File dir, Set<String> extensions) {
        FilenameFilter ff = (dir1, name) -> {
            File f = new File(dir1,name);
            return f.isDirectory() ||
             extensions.stream().anyMatch( e -> name.endsWith(e));
        };
        return listDirectoryR(dir, ff);
    }

    private static List<File> listDirectoryR(File dir, FilenameFilter extensions) {
        List<File> allFiles = new ArrayList<>();
        if(!dir.isDirectory()){//is File
            return List.of(dir); //return File (ignore extension)
        }
        File[] root = dir.listFiles(extensions);//dir is folder
        if (root != null) {
            for (File f : root) {
                if (f.isDirectory()) {
                    List<File> tmp = listDirectoryR(f, extensions);
                    allFiles.addAll(tmp);
                } else {
                    allFiles.add(f);
                }
            }
        }
        return allFiles;
    }
}
