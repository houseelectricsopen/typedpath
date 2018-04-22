package com.typedpath.beanmapping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourcePrinter {
    public void print(File packageRoot, String className, String code) {
        String strFilePath = className.replace(".", "/") + ".java";
        Path sourceFilePath = packageRoot.toPath().resolve(strFilePath);
        File parent = sourceFilePath.toFile().getParentFile();
        parent.mkdirs();
        try {
            Files.write(sourceFilePath, code.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
}
