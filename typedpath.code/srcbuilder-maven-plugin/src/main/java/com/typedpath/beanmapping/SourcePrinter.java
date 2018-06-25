package com.typedpath.beanmapping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class SourcePrinter {
    public void print(File packageRoot, String className, String code, Function<String, String> classNameToSourceFileName) {
        String strFilePath =  className.replace(".", "/");
        strFilePath = classNameToSourceFileName.apply(strFilePath);
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
