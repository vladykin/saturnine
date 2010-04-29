package org.saturnine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Alexey Vladykin
 */
public class FileUtil {

    private FileUtil() {
    }

    public static void empty(File file) throws IOException {
        new FileOutputStream(file).close();
    }

    public static void delete(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }

    public static void rename(File src, File dst) throws IOException {
        if (!src.renameTo(dst)) {
            throw new IOException("Failed to rename " + src + " to " + dst);
        }
    }

    public static String relativePath(File base, File file) {
        String basePath = base.getPath();
        String filePath = file.getPath();
        if (filePath.startsWith(basePath) && basePath.length() < filePath.length()) {
            return filePath.substring(basePath.length() + 1);
        } else {
            return null;
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
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
            int bytesRead;
            try {
                //open the files for input and output
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest);
                //while bytesRead indicates a successful read, lets write...
                while ((bytesRead = fin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) { //Error copying file...
                IOException wrapper = new IOException("copyFiles: Unable to copy file: "
                        + src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
                wrapper.initCause(e);
                wrapper.setStackTrace(e.getStackTrace());
                throw wrapper;
            } finally { //Ensure that the files are closed (if they were open).
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fout.close();
                }
            }
        }
    }
}