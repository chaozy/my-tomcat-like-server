package uk.ac.ucl.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * FileUtil aims to deal with file input and output.
 */
public class FileUtil {
    /**
     * Loop through all files under the given directory
     * and return those with specified extension.
     * @param file
     * @param extension
     * @return
     * @throws FileNotFoundException
     */
    public static List<File> getFiles(File file, String extension) throws FileNotFoundException {
        List<File> fileList = new ArrayList<>();

        if (file == null || !file.exists()){
            return fileList;
        }

        if (file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            if (files.length > 0){
                for (File tmp : files){
                    fileList.addAll(getFiles(tmp, extension));
                }
            }
        }
        else{
            if (extension != null || file.getName().endsWith(extension)){
                fileList.add(file);
            }
        }
        return fileList;
    }

    public static List<File> getFiles(File file) throws FileNotFoundException {
        return getFiles(file, null);
    }

}
