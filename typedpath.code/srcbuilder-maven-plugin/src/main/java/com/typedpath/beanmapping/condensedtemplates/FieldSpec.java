package com.typedpath.beanmapping.condensedtemplates;

import com.typedpath.template.Templater;

import java.util.function.Function;

public class FieldSpec implements Templater.TemplateValues {

    private String name;
    private String type;
    private String parentType;
    private boolean isComplex=false;

    public void setComplex() {
        this.isComplex=true;
    }

    public boolean complex() {
        return this.isComplex;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    private String collectionType;

    private Function<String, String> packageMapper;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public Function<String, String> getPackageMapper() {
        return packageMapper;
    }

    //projections
    public String getPropertyName() {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public String getParentTypeAsString() {
        return packageMapper==null?parentType:packageMapper.apply(parentType);
    }

    public String getParentSimpleName() {
        return parentType.substring(parentType.lastIndexOf('.')+1);
    }

    public String getTypeAsString() {
        String shortenedName = getType().replaceAll("java.lang.", "");
        if (packageMapper!=null) {
            shortenedName = packageMapper.apply(shortenedName);
        }
        if (this.collectionType!=null) {
            shortenedName = this.collectionType +"<" + shortenedName + ">";
        }
        return shortenedName;
    }

    public String getContainedTypeAsString() {
        String shortenedName = getType().replaceAll("java.lang.", "");
        if (packageMapper!=null) {
            shortenedName = packageMapper.apply(shortenedName);
        }
        return shortenedName;
    }


    public void setPackageMapper(Function<String, String> packageMapper) {
        this.packageMapper = packageMapper;
    }

    public static String camelCase2DatabaseUnderscore(String str) {
        StringBuilder sbColumn = new StringBuilder();
        for (int done=0; done<str.length(); done++) {
            char c = str.charAt(done);
            if (done!=0 && Character.isUpperCase(c)) {
                sbColumn.append('_');
            }
            sbColumn.append(Character.toLowerCase(c));
        }
        return sbColumn.toString();
    }

    public String getColumnName() {
        if (name==null) {
            return null;
        } else {
            return camelCase2DatabaseUnderscore(name);
        }
    }



}
