package org.saturnine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Alexey Vladykin
 */
public class FileUtil {

    private FileUtil() {
    }

    public static String joinPath(String basedir, String path) {
        if (basedir.isEmpty()) {
            return path;
        } else if (path.isEmpty()) {
            return basedir;
        } else {
            return basedir + '/' + path;
        }
    }

    public static String normalizePath(String path) {
        return path.replaceAll("/\\./", "/")
                .replaceAll("^\\.?/+", "")
                .replaceAll("/+\\.?$", "")
                .replaceAll("//+", "/");
    }

    public static void empty(File file) throws IOException {
        new FileOutputStream(file).close();
    }

    public static void delete(File file) throws IOException {
        if (!file.delete() && file.exists()) {
            throw new IOException("Failed to delete " + file);
        }
    }

    public static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        delete(file);
    }

    public static void rename(File src, File dst) throws IOException {
        if (!src.renameTo(dst)) {
            throw new IOException("Failed to rename " + src + " to " + dst);
        }
    }

    public static String relativePath(File base, File file) {
        String basePath = base.getPath();
        String filePath = file.getPath();
        if (basePath.equals(filePath)) {
            return "";
        } else if (filePath.startsWith(basePath + '/')) {
            return filePath.substring(basePath.length() + 1);
        } else {
            return filePath;
        }
    }

    public static void copyFiles(File src, File dest) throws IOException {
        //Check to ensure that the source is valid...
        if (!src.exists()) {
            throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
        } else if (!src.canRead()) { //check to ensure we have rights to the source...
            throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
        }
        //is this a directory copy?
        if (src.isDirectory()) {
            if (!dest.exists()) { //does the destination already exist?
                //if not we need to make it exist if possible (note this is mkdirs not mkdir)
                if (!dest.mkdirs()) {
                    throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
                }
            }
            //get a listing of files...
            String list[] = src.list();
            //copy all the files in the list.
            for (int i = 0; i < list.length; i++) {
                File dest1 = new File(dest, list[i]);
                File src1 = new File(src, list[i]);
                copyFiles(src1, dest1);
            }
        } else {
            //This was not a directory, so lets just copy the file
            try {
                copy(new FileInputStream(src), new FileOutputStream(dest));
            } catch (IOException e) { //Error copying file...
                IOException wrapper = new IOException("copyFiles: Unable to copy file: "
                        + src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
                wrapper.initCause(e);
                wrapper.setStackTrace(e.getStackTrace());
                throw wrapper;
            }
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
        int bytesRead;
        try {
            //while bytesRead indicates a successful read, lets write...
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally { //Ensure that the files are closed (if they were open).
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
