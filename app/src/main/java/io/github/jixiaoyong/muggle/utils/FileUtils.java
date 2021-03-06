package io.github.jixiaoyong.muggle.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.github.jixiaoyong.muggle.FileEntity;
import io.github.jixiaoyong.muggle.R;

public class FileUtils {
    private static String className = FileUtils.class.getName();

    /**
     * Listing the files from specified file path.
     *
     * @param filesPath file path used to list files.
     * @return list which contain files.
     */
    public static List<FileEntity> listFiles(String filesPath) {
        File file = new File(filesPath);
        if (!file.exists()) {
            file.mkdirs();
            return new ArrayList<>();
        }
        final ArrayList<FileEntity> entityList = new ArrayList<>();
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean isAccept;
                String fileName = pathname.getName();
                isAccept = fileName.endsWith(".md") || fileName.endsWith(".markdown")
                        || fileName.endsWith(".mdown");
                if (isAccept) {
                    FileEntity entity = new FileEntity();
                    entity.setName(pathname.getName());
                    entity.setLastModified(pathname.lastModified());
                    entity.setAbsolutePath(pathname.getAbsolutePath());
                    entityList.add(entity);
                }
                return isAccept;
            }
        });
        Collections.sort(entityList, new Comparator<FileEntity>() {
            @Override
            public int compare(FileEntity o1, FileEntity o2) {
                return Long.compare(o2.getLastModified(), o1.getLastModified());
            }
        });
        return entityList;
    }

    /**
     * Search files from specified file path
     *
     * @param filesPath filepath be searched
     * @param query     search key word
     * @return search result
     */
    public static List<FileEntity> searchFiles(String filesPath, final String query) {
        File file = new File(filesPath);
        if (!file.exists()) {
            file.mkdirs();
            return new ArrayList<>();
        }
        final ArrayList<FileEntity> entityList = new ArrayList<>();
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean isAccept;
                String fileName = pathname.getName();
                isAccept = fileName.contains(query) && (fileName.endsWith(".md")
                        || fileName.endsWith(".markdown") || fileName.endsWith(".mdown"));
                if (isAccept) {
                    FileEntity entity = new FileEntity();
                    entity.setName(pathname.getName());
                    entity.setLastModified(pathname.lastModified());
                    entity.setAbsolutePath(pathname.getAbsolutePath());
                    entityList.add(entity);
                }
                return isAccept;
            }
        });
        Collections.sort(entityList, new Comparator<FileEntity>() {
            @Override
            public int compare(FileEntity o1, FileEntity o2) {
                return Long.compare(o2.getLastModified(), o1.getLastModified());
            }
        });
        return entityList;
    }

    /**
     * Save content to specified file.
     *
     * @param filePath     file path indicate the file which be written content.
     * @param content
     * @param forceRewrite rewrite old file
     * @return if save success, return true, otherwise return false.
     */
    public static boolean saveFile(String filePath, String content, boolean forceRewrite) {
        boolean success;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            success = saveContent(file, content);
        } else if (file.exists() && file.isFile() && forceRewrite) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            success = saveContent(file, content);
        } else {
            success = false;
        }
        return success;
    }

    /**
     * Rename file
     *
     * @param oldFile the file which be renamed.
     * @param newFile target file.
     */
    public static void renameFile(Context context, File oldFile, File newFile) {
        if (!oldFile.exists()) {
            Log.i(className, "File not found.");
        } else {
            if (newFile.exists()) {
                Toast.makeText(context, R.string.toast_file_name_exists, Toast.LENGTH_SHORT).show();
            } else {
                oldFile.renameTo(newFile);
                Toast.makeText(context, R.string.toast_saved, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Save content to specified file.
     *
     * @param filePath     file path indicate the file which be written content.
     * @param content
     * @param forceRewrite rewrite old file
     * @return if save success, return true, otherwise return false.
     */
    public static boolean saveFile(String filePath, InputStream content, boolean forceRewrite) {
        boolean success;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            success = saveContent(file, content);
        } else if (file.exists() && file.isFile() && forceRewrite) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            success = saveContent(file, content);
        } else {
            success = false;
        }
        return success;
    }


    /**
     * Writing content to file.
     *
     * @param file
     * @param content
     */
    public static boolean saveContent(File file, String content) {
        boolean result;
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(file.getAbsolutePath());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * Writing content to file.
     *
     * @param file
     * @param content
     */
    public static boolean saveContent(File file, InputStream content) {
        boolean result;
        FileOutputStream fileOutputStream = null;
        try {
            byte[] arr = new byte[1024];
            int len;
            fileOutputStream = new FileOutputStream(file);
            while ((len = content.read(arr)) != -1) {
                fileOutputStream.write(arr, 0, len);
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                content.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Get filename which has no extension behind.
     *
     * @param fileName
     * @return
     */
    public static String stripExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        // Get position of last '.'.
        int pos = fileName.lastIndexOf(".");
        // If there wasn't any '.' just return the string as is.
        if (pos == -1) {
            return fileName;
        }

        // Otherwise return the string, up to the dot.
        return fileName.substring(0, pos);
    }

    /**
     * Read content from specified path.
     *
     * @param pathname  pathname of file
     * @param lineBreak indicate whether should include line break in content.
     * @return
     */
    public static String readContentFromPath(String pathname, boolean lineBreak) {
        return readContent(new File(pathname), lineBreak);
    }

    /**
     * Read content from specified file.
     *
     * @param file      file used to read content.
     * @param lineBreak indicate whether should include line break in content.
     * @return
     */
    public static String readContentFromFile(File file, boolean lineBreak) {
        return readContent(file, lineBreak);
    }

    private static String readContent(File file, boolean lineBreak) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
                if (lineBreak) {
                    content.append("\n");
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(className, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(className, e.getMessage());
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * delete file
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return deleteFile(file);
    }

    /**
     * delete file
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
            Log.i(className, "Delete success.");
        } else {
            Log.i(className, "File not found.");
        }
        return result;
    }

    public static Date getCreationDate(String filePath) {
        Path path = Paths.get(filePath);
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Date(attr.creationTime().toMillis());
    }

    public static String getByte64EncodeContent(String path) {
        String fileContent = readContentFromPath(path, true);
        String enCodeStr = Base64.getEncoder().encodeToString(fileContent.getBytes());
        return enCodeStr;
    }
}
