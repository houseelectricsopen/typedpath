package com.typedpath.beanmapping.fromjsonschema;

import com.typedpath.beanmapping.JsonUtil;
import com.typedpath.beanmapping.SourcePrinter;
import com.typedpath.beanmapping.condensedtemplates.BeanTemplate;
import com.typedpath.beanmapping.condensedtemplates.ClassSourceTemplate;
import com.typedpath.beanmapping.condensedtemplates.FieldSpec;
import com.typedpath.beanmapping.condensedtemplates.MappedEnum;
import com.typedpath.template.Templater;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class JsonSourceBuilder {
//  TODO detect duplicate classnames
//

    public void mapJsonToJavaBeanSource(String json, File packageRootDirectory, String destinationPackage,
                                        String rootClassShortName, String overrideTemplate) throws Exception {
        mapJsonToBeanSource(json, packageRootDirectory, destinationPackage,
                rootClassShortName, overrideTemplate, defaultJavaClassNameMapper(), classNameIn->classNameIn+".java");
    }

    public void mapJsonToTypeScriptBeanSource(String json, File packageRootDirectory, String destinationPackage,
                                        String rootClassShortName, String overrideTemplate) throws Exception {
        mapJsonToBeanSource(json, packageRootDirectory, destinationPackage,
                rootClassShortName, overrideTemplate, defaultJavaClassNameMapper(), classNameIn->classNameIn+".ts");
    }


    public void mapJsonToBeanSource(String json, File packageRootDirectory, String destinationPackage,
                                        String rootClassShortName, String overrideTemplate,  Function<String, String> classNameMapper, Function<String, String> classNameToSOurceFileName) throws Exception {
        Map<String, ClassSourceTemplate> contextToClassTemplateValues =  mapJsonSchemaObject(json, destinationPackage, rootClassShortName, classNameMapper );

        System.out.println("************* Templates");

        SourcePrinter sourcePrinter = new SourcePrinter();
        System.out.println("writing to " + packageRootDirectory.getAbsolutePath());
        System.out.println(" from  " + contextToClassTemplateValues);


        contextToClassTemplateValues.forEach((s,t)-> {
            String className = t.getPackageName() + "." + t.getSimpleName();
            String code = null;
            try {
                if (t instanceof MappedEnum) {
                    code = (new Templater()).applyCondensedTemplateInferTemplateFromValuesType(t, null);
                } else {
                    code = (new Templater()).applyCondensedTemplateInferTemplateFromValuesType(t, overrideTemplate);
                    //System.out.println("about printed  " + t.getSimpleName());
                }

            } catch (Exception ex) {
                throw new RuntimeException("failed to create code " + s, ex);
            }
            sourcePrinter.print(packageRootDirectory, className, code, classNameToSOurceFileName);
        });

        System.out.println("wrote to " + packageRootDirectory.getAbsolutePath());
    }

    private Function<String, String> defaultJavaClassNameMapper() {
        Function<String, String> classNameMapper =
                c-> {
                    if (c == null) {
                        return "null";
                    } else if (c.equals("#/definitions/datePattern")) {
                        return LocalDate.class.getName();
                    } else if (c.equals("#/definitions/isoDate")) {
                        return Date.class.getName();
                    } else if (c.startsWith("#/definitions/uuid")) {
                        return UUID.class.getName();
                    } else if (c.startsWith("#/definitions/")) {
                        String str =  c.substring("#/definitions/".length());
                        return str.substring(0, 1).toUpperCase() + str.substring(1);
                    } else if ("string".equals(c)) {
                        return String.class.getName();
                    }
                    else if (!c.contains("."))
                    {
                        return c.substring(0, 1).toUpperCase() + c.substring(1);
                    } else {
                        return c;
                    }

                };
        return classNameMapper;
    }


    private Map<String, ClassSourceTemplate> mapJsonSchemaObject(String json, String destinationPackage, String rootName, Function<String, String> classNameMapper) throws Exception {
        Map<String, ClassSourceTemplate> contextToClassTemplateValues = new HashMap<>();
        Map<String, FieldSpec>  definitionNameToFieldSpec = new HashMap<>();
        mapJsonSchemaObject(JsonUtil.stringToJson(json), rootName, destinationPackage, classNameMapper, contextToClassTemplateValues, definitionNameToFieldSpec);
        contextToClassTemplateValues.forEach((k,v)->{
            if (v instanceof BeanTemplate) {
                BeanTemplate ib = (BeanTemplate) v;
                System.out.println(k + " -> " + ib.getSimpleName());
//                System.out.println("checking fields: " + );
                ib.getFields().forEach(
                        f-> {
                            System.out.println("   " + f.getType() + " : " + f.getName() + " complex:" + f.complex());
                            //rewrite reference types e.g. #/definitions/hearing -> Hearing
                            if (definitionNameToFieldSpec.containsKey(f.getType())) {
                               FieldSpec def = definitionNameToFieldSpec.get(f.getType());
                                System.out.println("*********************** found defined field spec " + f.getType() +
                                        " mapping totype " + def.getType()  );
                               f.setType(def.getType());
                               f.setCollectionType(def.getCollectionType());
                               if (def.complex()) f.setComplex();
                            }
                            if (contextToClassTemplateValues.containsKey(f.getType())) {
                                ClassSourceTemplate template = contextToClassTemplateValues.get(f.getType());
                                 f.setType(template.getSimpleName());
                                 if (template instanceof BeanTemplate) {
                                     f.setComplex();
                                 }
                            } else {
                                f.setType(classNameMapper.apply(f.getType()));
                            }
                        });
            }
        });

        definitionNameToFieldSpec.forEach((name, fieldSpec)->
        {
            System.out.println("******************** defName:" + name + " => " + fieldSpec.getType());
        });


        return contextToClassTemplateValues;
    }

    public void processDefinitions (String path, ScriptObjectMirror definitions, String destinationPackage, Function<String, String> classNameMapper, Map<String, ClassSourceTemplate> contextToClassTemplateValues,
                                    Map<String, FieldSpec>  definitionNameToFieldSpec)
      throws Exception {
        for (Map.Entry<String, Object> entry : definitions.entrySet()) {
            String defName = path + entry.getKey();
            ScriptObjectMirror objectDef = (ScriptObjectMirror) entry.getValue();
            String type = (String) objectDef.get("type");
            if ("object".equals(type)) {
                BeanTemplate subClass = mapJsonSchemaObject(objectDef, defName, destinationPackage, classNameMapper, contextToClassTemplateValues, definitionNameToFieldSpec);
            }  else if ("array".equals(type)) {
                System.out.println("*******processDefinitions array field def: " + defName);
                FieldSpec fieldSpec = readArrayField(defName, objectDef, destinationPackage, classNameMapper,
                         contextToClassTemplateValues,
                        definitionNameToFieldSpec);
                definitionNameToFieldSpec.put(defName, fieldSpec);
            }
        }
    }

    private FieldSpec readArrayField(String pName, Map propertyDef, String destinationPackage, Function<String, String> classNameMapper, Map<String, ClassSourceTemplate> contextToClassTemplateValues,
                                     Map<String, FieldSpec>  definitionNameToFieldSpec) throws Exception{
        String strType;
        FieldSpec fieldSpec = new FieldSpec();
        //process items
        //TODO make this a nethod that outputs a fieldSpec
        ScriptObjectMirror itemDef = (ScriptObjectMirror) propertyDef.get("items");
        if (itemDef==null) {
            throw new Exception("array \"" + pName + "\" requires items definition");
        }
        else if (itemDef.containsKey("$ref")) {
            //  types are rewritten  after definitions have beenn digested
            strType = itemDef.get("$ref").toString();// classNameMapper.apply(itemDef.get("$ref").toString());
            fieldSpec.setCollectionType(List.class.getName());
            //TODO remove assumption that all collection types are complex
            fieldSpec.setComplex();
        } else
        if (!itemDef.containsKey("type") || !"object".equals(itemDef.get("type"))) {
            //its a raw array
            if (!itemDef.containsKey("type")) {
                throw new Exception("array \" + pName + \" with no properties is expected to have exactly 1 item(s) defining a raw type ");
            }
            strType = classNameMapper.apply(""+itemDef.get("type"));
            fieldSpec.setCollectionType(List.class.getName());

        }
        else {
            BeanTemplate subClass =  mapJsonSchemaObject(itemDef, pName, destinationPackage, classNameMapper, contextToClassTemplateValues, definitionNameToFieldSpec);
            strType = subClass.getSimpleName();
            fieldSpec.setCollectionType(List.class.getName());
            //TODO remove assumption that all collection types are complex
            fieldSpec.setComplex();
        }
        fieldSpec.setType(strType);
        return fieldSpec;
    }

    //TODO inject these mappings
    private String simpleTypeMappingFromPropertyDef(ScriptObjectMirror jsonDef) {
        final Object type = jsonDef.get("type");
        final Object format = jsonDef.get("format");
        if ("string".equals(type) && "date-time".equals(format)) {
            return ZonedDateTime.class.getName();
        } else
        {
            return null;
        }
    }

    //TODO add field def to fieldSpec mapping
    public BeanTemplate mapJsonSchemaObject(ScriptObjectMirror json, String context, String destinationPackage, Function<String, String> classNameMapper, Map<String, ClassSourceTemplate> contextToClassTemplateValues,
                                            Map<String, FieldSpec>  definitionNameToFieldSpec)
       throws Exception {
        String className = classNameMapper.apply(context);
        BeanTemplate beanTemplate = new BeanTemplate();
        beanTemplate.setSimpleName(className);
        beanTemplate.setPackageName(destinationPackage);
        contextToClassTemplateValues.put(context, beanTemplate);

        if (json.containsKey("definitions")) {
            Object oDefs = json.get("definitions");
            if (!(oDefs instanceof ScriptObjectMirror )) {
                throw new Exception("definitions property is not a javascript obect");
            }
            processDefinitions("#/definitions/", (ScriptObjectMirror)oDefs, destinationPackage, classNameMapper, contextToClassTemplateValues, definitionNameToFieldSpec);
        }


        Object oProperties = json.get("properties");
        if (oProperties==null  )
            {
                throw new Exception("expected properties for " + context);
            }
        if (!(oProperties instanceof ScriptObjectMirror))  {
            throw new Exception("expected properties for " + context + " to be of type " + oProperties.getClass().getName());
        }
        ScriptObjectMirror properties = (ScriptObjectMirror) oProperties;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String pName =  entry.getKey();
            ScriptObjectMirror propertyDef = (ScriptObjectMirror)  entry.getValue();
            String type = (String) propertyDef.get("type");
            if (type==null) {
                type = (String) propertyDef.get("$ref");
            }
            //String strType;
            FieldSpec fieldSpec = new FieldSpec();
            boolean isEnum = propertyDef.containsKey("enum");
            if (isEnum) {
                MappedEnum mappedEnum = new MappedEnum();
                mappedEnum.setPackageName(destinationPackage);
                String strType = classNameMapper.apply(pName);
                String strContextRef = pName;
                if (propertyDef.containsKey("$className")) {
                    strType = propertyDef.get("$className").toString();
                    strContextRef = strType;
                }
                mappedEnum.setSimpleName(strType);
                ScriptObjectMirror jsEnum = (ScriptObjectMirror) propertyDef.get("enum");
                jsEnum.values().forEach(
                        v->mappedEnum.values.add(v.toString())
                );
                contextToClassTemplateValues.put(strContextRef, mappedEnum);
                System.out.println(" " + jsEnum);
                fieldSpec.setType(strType);
            }
            else if ("object".equals(type)) {
                                BeanTemplate subClass = mapJsonSchemaObject(propertyDef, pName, destinationPackage, classNameMapper, contextToClassTemplateValues, definitionNameToFieldSpec);
                 String strType = subClass.getSimpleName();
                 fieldSpec.setComplex();
                 fieldSpec.setType(strType);
            } else if ("array".equals(type)) {
                fieldSpec = readArrayField(pName, propertyDef, destinationPackage, classNameMapper, contextToClassTemplateValues, definitionNameToFieldSpec);
            } else
                {
                final String simpleTypeMapping = simpleTypeMappingFromPropertyDef(propertyDef);
                if (simpleTypeMapping!=null) {
                    type = simpleTypeMapping;
                }
                //TODO remove assumption that class name can be inferred from ref name
                fieldSpec.setType(type);
            }
            fieldSpec.setName(pName);
            fieldSpec.setParentType(beanTemplate.getSimpleName());
            beanTemplate.getFields().add(fieldSpec);
        }
        return beanTemplate;
    }
}
