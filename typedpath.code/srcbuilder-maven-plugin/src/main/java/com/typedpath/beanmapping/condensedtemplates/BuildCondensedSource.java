package com.typedpath.beanmapping.condensedtemplates;

import com.typedpath.beanmapping.MappedClass;
import com.typedpath.template.Templater;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BuildCondensedSource {

    public static List<MappedClass> mapClassCondensed(Class type, Function<String, String> packageMapper, String overrideTemplate) throws Exception{
        return mapClassCondensed(type, packageMapper, overrideTemplate, null);
    }

   public static List<MappedClass> mapClassCondensed(Class type, Function<String, String> packageMapper, String overrideTemplate,
                     Function<Class, Boolean> isComplexFieldType) throws Exception{
        String newPackageName = packageMapper.apply(type.getPackage().getName());
        String simpleName = type.getSimpleName();
        Templater.TemplateValues template = createCondensedClassTemplate(type, newPackageName, simpleName, packageMapper, isComplexFieldType);
        String code = (new Templater()).applyCondensedTemplateInferTemplateFromValuesType(template, overrideTemplate);
        return Arrays.asList(new MappedClass(newPackageName + "." + simpleName, code));
    }

    public static Templater.TemplateValues createCondensedClassTemplate(Class type, String newPackageName,
                                                                 String simpleName, Function<String, String> packageMapper,  Function<Class, Boolean> isComplexFieldType) throws Exception {
        Templater.TemplateValues templateValues;
        if (type.isEnum()) {
            MappedEnum typedTemplate = new MappedEnum();
            templateValues = typedTemplate;
            typedTemplate.setSimpleName(type.getSimpleName());
            typedTemplate.setPackageName(newPackageName);
            for (Object oEnum : type.getEnumConstants()) {
                Enum unum = (Enum) oEnum;
                typedTemplate.values.add(unum.name());
            }
        } else {
            BeanTemplate typedTemplate = new BeanTemplate();
            templateValues = typedTemplate;
            typedTemplate.setPackageName(newPackageName);
            typedTemplate.setSimpleName(simpleName);
            for (Field field : type.getDeclaredFields()) {
                //check there is a getter !
                //TODO write test for this
                Method getterMethod = null;
                try {
                    getterMethod = type.getMethod("get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1));
                }
                catch (Exception ex) {
                    //NA
                }
                if (getterMethod==null) {
                    continue;
                }

                FieldSpec fieldSpec = new FieldSpec();
                fieldSpec.setName(field.getName());
                fieldSpec.setType(field.getType().getName());
                fieldSpec.setParentType(type.getName());
                fieldSpec.setPackageMapper(packageMapper);
                if (List.class.isAssignableFrom(field.getType())) {
                    fieldSpec.setComplex();
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    fieldSpec.setType(pt.getActualTypeArguments()[0].getTypeName());
                    fieldSpec.setCollectionType(field.getType().getName());
                } else if (isComplexFieldType!=null && isComplexFieldType.apply(field.getType())) {
                        fieldSpec.setComplex();
                }
                typedTemplate.getFields().add(fieldSpec);
            }
        }
        return templateValues;
    }
}
