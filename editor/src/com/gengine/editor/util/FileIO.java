package com.gengine.editor.util;

import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.nio.file.Files;

public class FileIO {

    public static boolean renameFile(FileHandle fileHandle, String newName) {
        File file = fileHandle.file();
        File renamed = new File(file.getParent() + File.separator + newName);
        if(renamed.exists()) {
           return false;
        }
        return file.renameTo(renamed);
    }


}
