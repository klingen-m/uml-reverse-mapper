package com.iluwatar.urm.helper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 *
 */
//https://www.codejava.net/java-se/file-io/programmatically-extract-a-zip-file-using-java
//https://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
public class ZipUtility {
    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath  zipFilePath
     * @param destDir destDir
     * @throws IOException IOException
     */
    public static void unzip(String zipFilePath, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDir.getAbsolutePath() + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn zipIn
     * @param filePath filePath
     * @throws IOException IOException
     */
    private static  void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static File zip(File zipFile, File srcFolder) {
        List<File> fileList = generateFileList(srcFolder);
        byte[] buffer = new byte[BUFFER_SIZE];

        try( FileOutputStream fos= new FileOutputStream(zipFile);
             ZipOutputStream zos  = new ZipOutputStream(fos)) {

            System.out.println("Output to Zip : " + zipFile);
            for (File file: fileList) {
                System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(file.getAbsoluteFile().getAbsolutePath().replace(srcFolder.getAbsolutePath()+"\\", ""));
                zos.putNextEntry(ze);

                try(FileInputStream in= new FileInputStream(file)){
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }

            zos.closeEntry();
            System.out.println("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return zipFile;
    }

    private static List<File> generateFileList(File src) {
        // add file only
        List <File> fileList = new ArrayList<>();
        if (src.isFile()) {
            fileList.add(src);
        }

        if (src.isDirectory()) {
            String[] subNote = src.list();
            if(subNote!=null) {
                for (String filename : subNote) {
                    List<File> tmp = generateFileList(new File(src, filename));
                    fileList.addAll(tmp);
                }
            }
        }
        return fileList;
    }
}