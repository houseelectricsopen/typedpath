package com.typedpath.beanmapping.mojo;

import com.typedpath.beanmapping.MappedClass;
import com.typedpath.beanmapping.ResourceUtil;
import com.typedpath.beanmapping.SourcePrinter;
import com.typedpath.beanmapping.condensedtemplates.BuildCondensedSource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mojo( name = "pathsforimmutablebeans", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class PathsForImmutableBeansMojo extends AbstractMojo{

    @Parameter( defaultValue = "", property = "destinationSourceRoot", required = true )
    private String destinationSourceRoot;

    @Parameter( defaultValue = "", property = "types", required = true )
    private List<String> types = new ArrayList<>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        String strContext = getClass().getSimpleName() + "::execute";
        System.out.println(strContext + " destinationSourceRoot:" + destinationSourceRoot);
        if (destinationSourceRoot==null || destinationSourceRoot.trim().length()==0) {
            throw new MojoFailureException("destinationSourceRoot is unspecified");
        }
        List<Class> lTypes = new ArrayList<>();
        types.forEach(
                t-> {
                    System.out.println(strContext + "type:" +t);
                    try {
                    Class theClass = Class.forName(t);
                    System.out.println("loaded class " + theClass.getName());
                    lTypes.add(theClass);
                    }
                    catch (Exception ex) {
                        throw new RuntimeException("failed to load class " + t +
                              " referenced classes must be in plugins/plugin/dependencies"     , ex);
                    }
                }
        );

        File packageRoot = new File(destinationSourceRoot);
        String template = null;
        String templateName = "PathsForImmutableBean.template";
        try {
            template = ResourceUtil.readResourceByName(PathsForImmutableBeansMojo.class, templateName);
        } catch (Exception ex) {
            throw new MojoFailureException("failed to load template " + templateName, ex);
        }
        if (template==null) {
            throw new MojoFailureException("failed to load template " + templateName);
        }

        Function<String, String> packageMapper = s->s;

        System.out.println("package root: " + packageRoot.getAbsolutePath());
        List<MappedClass> mappedClasses;

        Function<Class, Boolean> isComplexFieldType = cl->lTypes.contains(cl);

        //TODO should add source path here to reduce config ?

        try {
            for (Class type : lTypes) {
                mappedClasses = BuildCondensedSource.mapClassCondensed(type, packageMapper, template, isComplexFieldType);
                mappedClasses.forEach(mc ->  {
                    //TODO clean up this hack
                    mc.className = mc.className +"Path";
                    (new SourcePrinter()).print(packageRoot, mc.className, mc.code, classNameIn->classNameIn+".java");
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("failed on " + ex);
        }
    }

}