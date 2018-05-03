package com.typedpath.beanmapping.mojo;

import com.typedpath.beanmapping.ResourceUtil;
import com.typedpath.beanmapping.fromjsonschema.JsonSourceBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;

@Mojo( name = "pathsforjsonschema", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class PathsForJsonSchemaMojo extends AbstractMojo{

    @Parameter( property = "destinationSourceRoot", required = true )
    private String destinationSourceRoot;

    @Parameter(  property = "destinationPackage", required = true )
    private String destinationPackage;

    @Parameter(  property = "jsonSchemaFile", required = true )
    private String jsonSchemaFile;

    @Parameter( property = "rootClassShortName", required = true )
    private  String rootClassShortName;

    @Parameter( property = "templateName", required = false )
    private String templateName="ImmutableBeanAndPaths.template";

    private String getTemplateName() {
        return templateName;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        String strContext = getClass().getSimpleName() + "::execute";
        System.out.println(strContext + " destinationSourceRoot:" + destinationSourceRoot);
        if (destinationSourceRoot == null || destinationSourceRoot.trim().length() == 0) {
            throw new MojoFailureException("destinationSourceRoot is unspecified");
        }
        String json;
        File packageRootDirectory = new File(destinationSourceRoot);
        File fJsonSchemaFile = new File(jsonSchemaFile);
        if (!fJsonSchemaFile.exists()) {
            throw new MojoFailureException("jsonSchemaFile does not exist " + jsonSchemaFile);
        }

        try {
            json = ResourceUtil.readResource(new FileInputStream(fJsonSchemaFile));
        } catch (Exception ex) {
            throw new MojoFailureException("failed to read jsonSchemaFile ", ex);
        }

        try {
            String templateName = getTemplateName();
            System.out.println("looking up " + templateName);
            String template = ResourceUtil.readResourceByName(PathsForJsonSchemaMojo.class,  templateName);
            (new  JsonSourceBuilder()).mapJsonToImmutableBeanSource(json, packageRootDirectory, destinationPackage, rootClassShortName, template);
        } catch (Exception ex) {
            throw new MojoFailureException("failed to process  jsonSchemaFile " + fJsonSchemaFile.getAbsolutePath(), ex);

        }

    }

    public String getDestinationSourceRoot() {
        return destinationSourceRoot;
    }

    public void setDestinationSourceRoot(String destinationSourceRoot) {
        this.destinationSourceRoot = destinationSourceRoot;
    }

    public String getDestinationPackage() {
        return destinationPackage;
    }

    public void setDestinationPackage(String destinationPackage) {
        this.destinationPackage = destinationPackage;
    }

    public String getJsonSchemaFile() {
        return jsonSchemaFile;
    }

    public void setJsonSchemaFile(String jsonSchemaFile) {
        this.jsonSchemaFile = jsonSchemaFile;
    }

    public String getRootClassShortName() {
        return rootClassShortName;
    }

    public void setRootClassShortName(String rootClassShortName) {
        this.rootClassShortName = rootClassShortName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public static void main(String []args) throws Exception {
        PathsForJsonSchemaMojo pathsForJsonSchemaMojo = new PathsForJsonSchemaMojo();
        pathsForJsonSchemaMojo.setDestinationPackage("com.temp");
        pathsForJsonSchemaMojo.setDestinationSourceRoot("/temp");
        pathsForJsonSchemaMojo.setTemplateName("FluidSettablePojo.template");
        //pathsForJsonSchemaMojo.setJsonSchemaFile("./typedpath.code/testplugin/src/main/resources/fromjsonschema/hearing.command.generate-nows-schema.json");
        pathsForJsonSchemaMojo.setJsonSchemaFile("./typedpath.code/testplugin/src/main/resources/fromjsonschema/schema/referencedata.result.nows.json");        pathsForJsonSchemaMojo.setRootClassShortName("GenerateNow");
        pathsForJsonSchemaMojo.execute();





    }

}