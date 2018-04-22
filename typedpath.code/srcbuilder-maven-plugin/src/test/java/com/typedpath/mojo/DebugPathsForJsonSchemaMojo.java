package com.typedpath.mojo;

import com.typedpath.beanmapping.mojo.PathsForJsonSchemaMojo;

public class DebugPathsForJsonSchemaMojo {
    public static void main(String[] args) throws Exception {



        PathsForJsonSchemaMojo mojo = new PathsForJsonSchemaMojo();
        mojo.setDestinationPackage("com.typedpath.test.genfromjson.sendingsheetcompleted");
        mojo.setDestinationSourceRoot("target/generated-sources/java");
        mojo.setRootClassShortName("SendingSheetCompleted");
        mojo.setJsonSchemaFile("./testplugin/src/main/resources/fromjsonschema/sending-sheet-completed.json");
        mojo.execute();
    }
}