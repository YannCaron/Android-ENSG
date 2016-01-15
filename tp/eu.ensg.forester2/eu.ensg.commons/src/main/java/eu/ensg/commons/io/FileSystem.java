package eu.ensg.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by cyann on 21/12/15.
 */
public class FileSystem {

    private FileSystem() {
        throw new RuntimeException("Static class cannot be instantiated!");
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static void copyFile(File in, File out) throws IOException {
        copyFile(new FileInputStream(in), new FileOutputStream(out));
    }

}
