package com.typedpath.scratchpad;

import com.typedpath.beanmapping.ResourceUtil;
import com.typedpath.beanmapping.fromjsonschema.JsonSourceBuilder;
import com.typedpath.beanmapping.mojo.PathsForJsonSchemaMojo;

import java.io.File;
import java.io.FileInputStream;

public class JsonToTypescriptExperiment {
    public static void main(String []args) throws Exception {
        final String strContext = JsonToTypescriptExperiment.class + "::execute";
        final String destinationSourceRoot = "srcbuilder-maven-plugin/target/generatedtssrc";
        final String destinationPackage = "com.typedpath.gentypescript";
        final String rootClassShortName = "VariantDirectory";
        final String templateName = "TypescriptBean.template";
        //String jsonSchemaFile = "testplugin" + "/src/main/resources/fromjsonschema/schema/referencedata.get-all-result-definitions.json";
        String jsonSchemaFile = "testplugin" + "/src/main/resources/fromjsonschema/schema/hearing.save-nows-variants.json";
        System.out.println(strContext + " destinationSourceRoot:" + destinationSourceRoot);
        if (destinationSourceRoot == null || destinationSourceRoot.trim().length() == 0) {
            throw new Exception("destinationSourceRoot is unspecified");
        }
        String json;
        final File packageRootDirectory = new File(destinationSourceRoot);
        final File fJsonSchemaFile = new File(jsonSchemaFile);
        if (!fJsonSchemaFile.exists()) {
            throw new Exception("jsonSchemaFile does not exist " + fJsonSchemaFile.getAbsolutePath());
        }

        try {
            json = ResourceUtil.readResource(new FileInputStream(fJsonSchemaFile));
        } catch (Exception ex) {
            throw new Exception("failed to read jsonSchemaFile " + fJsonSchemaFile.getAbsolutePath(), ex);
        }

        String template=null;

        try {
            System.out.println("looking up " + templateName);
            template = ResourceUtil.readResourceByName(JsonToTypescriptExperiment.class,  templateName);
        }
        catch (Exception ex) {
            throw new Exception("failed to find " + templateName + " realtive to " + PathsForJsonSchemaMojo.class.getName());
        }

        try {
            (new  JsonSourceBuilder()).mapJsonToTypeScriptBeanSource(json, packageRootDirectory, destinationPackage, rootClassShortName, template);
        } catch (Exception ex) {
            throw new Exception("failed to process  jsonSchemaFile " + fJsonSchemaFile.getAbsolutePath(), ex);
        }

    }
}
