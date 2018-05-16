package com.typedpath.beanmapping.fromjsonschema;

import com.typedpath.beanmapping.JsonUtil;
import com.typedpath.beanmapping.SourcePrinter;
import com.typedpath.beanmapping.condensedtemplates.ClassSourceTemplate;
import com.typedpath.beanmapping.condensedtemplates.FieldSpec;
import com.typedpath.beanmapping.condensedtemplates.ImmutableBeanCondensed;
import com.typedpath.beanmapping.condensedtemplates.MappedEnum;
import com.typedpath.template.Templater;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class JsonSourceBuilder {
//  TODO detect duplicate classnames
//

    public void mapJsonToImmutableBeanSource(String json, File packageRootDirectory, String destinationPackage, String rootClassShortName, String overrideTemplate) throws Exception {
        Map<String, ClassSourceTemplate> contextToClassTemplateValues =  mapJsonSchemaObject(json, destinationPackage, rootClassShortName);
        System.out.println("************* Templates");

        SourcePrinter sourcePrinter = new SourcePrinter();
        System.out.println("writing to " + packageRootDirectory.getAbsolutePath());
        System.out.println(" from  " + contextToClassTemplateValues);


        contextToClassTemplateValues.forEach((s,t)-> {
            String className;
            className = t.getPackageName() + "." + t.getSimpleName();
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
            sourcePrinter.print(packageRootDirectory, className, code);
        });

        System.out.println("wrote to " + packageRootDirectory.getAbsolutePath());
    }

    private Map<String, ClassSourceTemplate> mapJsonSchemaObject(String json, String destinationPackage, String rootName) throws Exception {
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
                   } else
                           {
                               return c.substring(0, 1).toUpperCase() + c.substring(1);
                           }
               };
        Map<String, ClassSourceTemplate> contextToClassTemplateValues = new HashMap<>();
        mapJsonSchemaObject(JsonUtil.stringToJson(json), rootName, destinationPackage, classNameMapper, contextToClassTemplateValues);

        contextToClassTemplateValues.forEach((k,v)->{
            if (v instanceof ImmutableBeanCondensed) {
                ImmutableBeanCondensed ib = (ImmutableBeanCondensed) v;
                System.out.println(k + " -> " + ib.getSimpleName());
//                System.out.println("checking fields: " + );
                ib.getFields().forEach(
                        f-> {
                            System.out.println("   " + f.getType() + ":" + f.getName() + " complex:" + f.complex());
                            //rewrite reference types e.g. #/definitions/hearing -> Hearing
                            if (contextToClassTemplateValues.containsKey(f.getType())) {
                                ClassSourceTemplate template = contextToClassTemplateValues.get(f.getType());
                                 f.setType(template.getSimpleName());
                                 if (template instanceof ImmutableBeanCondensed) {
                                     f.setComplex();
                                 }
                            } else {
                                f.setType(classNameMapper.apply(f.getType()));
                            }
                        });
            }
        });

        return contextToClassTemplateValues;
    }

    public void processDefinitions (String path, ScriptObjectMirror definitions, String destinationPackage, Function<String, String> classNameMapper, Map<String, ClassSourceTemplate> contextToClassTemplateValues)
      throws Exception {
        for (Map.Entry<String, Object> entry : definitions.entrySet()) {
            String defName = path + entry.getKey();
            ScriptObjectMirror objectDef = (ScriptObjectMirror) entry.getValue();
            String type = (String) objectDef.get("type");
            if ("object".equals(type)) {
                ImmutableBeanCondensed subClass = mapJsonSchemaObject(objectDef, defName, destinationPackage, classNameMapper, contextToClassTemplateValues);
            }
        }
    }


    public ImmutableBeanCondensed mapJsonSchemaObject(ScriptObjectMirror json, String context, String destinationPackage, Function<String, String> classNameMapper, Map<String, ClassSourceTemplate> contextToClassTemplateValues)
       throws Exception {
        String className = classNameMapper.apply(context);
        ImmutableBeanCondensed immutableBeanCondensed = new ImmutableBeanCondensed();
        immutableBeanCondensed.setSimpleName(className);
        immutableBeanCondensed.setPackageName(destinationPackage);
        contextToClassTemplateValues.put(context, immutableBeanCondensed);

        if (json.containsKey("definitions")) {
            Object oDefs = json.get("definitions");
            if (!(oDefs instanceof ScriptObjectMirror )) {
                throw new Exception("definitions property is not a javascript obect");
            }
            processDefinitions("#/definitions/", (ScriptObjectMirror)oDefs, destinationPackage, classNameMapper, contextToClassTemplateValues);
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
            String strType;
            FieldSpec fieldSpec = new FieldSpec();
            boolean isEnum = propertyDef.containsKey("enum");
            if (isEnum) {
                MappedEnum mappedEnum = new MappedEnum();
                mappedEnum.setPackageName(destinationPackage);
                strType = classNameMapper.apply(pName);
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
            }
            else if ("object".equals(type)) {
                ImmutableBeanCondensed subClass = mapJsonSchemaObject(propertyDef, pName, destinationPackage, classNameMapper, contextToClassTemplateValues);
                 strType = subClass.getSimpleName();
                 fieldSpec.setComplex();
            } else if ("array".equals(type)) {
                ScriptObjectMirror itemDef = (ScriptObjectMirror) propertyDef.get("items");
                if (itemDef!=null && itemDef.containsKey("$ref")) {
                    strType = itemDef.get("$ref").toString() +"[]";
                }
                else if (itemDef==null || !itemDef.containsKey("type") || !"object".equals(itemDef.get("type"))) {
                    //its a raw array
                    if (!itemDef.containsKey("type")) {
                        throw new Exception("array with no properties is expected to have exactly 1 item defining a raw type " + pName);
                    }
                    strType = classNameMapper.apply(""+itemDef.get("type")) +  "[]";

                }
                else if (itemDef.containsKey("$ref")) {
                    //  types are rewritten  after definitions have beenn digested
                     strType = itemDef.get("$ref").toString();// classNameMapper.apply(itemDef.get("$ref").toString());
                    fieldSpec.setCollectionType(List.class.getName());
                    //TODO remove assumption that all collection types are complex
                    fieldSpec.setComplex();
                } else {
                   ImmutableBeanCondensed subClass =  mapJsonSchemaObject(itemDef, pName, destinationPackage, classNameMapper, contextToClassTemplateValues);
                    strType = subClass.getSimpleName();
                    fieldSpec.setCollectionType(List.class.getName());
                    //TODO remove assumption that all collection types are complex
                    fieldSpec.setComplex();
                }
            } else
                {
                    //TODO remove assumption that class name can be inferred from ref name
                strType = type;//classNameMapper.apply(type);
            }
            fieldSpec.setName(pName);
            fieldSpec.setType(strType);
            fieldSpec.setParentType(immutableBeanCondensed.getSimpleName());
            immutableBeanCondensed.getFields().add(fieldSpec);

        }
        return immutableBeanCondensed;
    }
}
