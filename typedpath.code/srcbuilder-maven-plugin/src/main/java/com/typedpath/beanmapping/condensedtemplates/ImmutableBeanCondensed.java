package com.typedpath.beanmapping.condensedtemplates;

import com.typedpath.template.Templater;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImmutableBeanCondensed extends ClassSourceTemplate implements Templater.TemplateValues {

   private List<FieldSpec> fields = new ArrayList<>();
   public List<FieldSpec> getFields() {
       return fields;
   }
    public List<FieldSpec> getComplexFields() {
        return fields.stream().filter((f)->f.complex()).collect(Collectors.toList());
    }
    public List<FieldSpec> getNonComplexFields() {
        return fields.stream().filter((f)->!f.complex()).collect(Collectors.toList());
    }
    public List<FieldSpec> getComplexListFields() {
        return fields.stream().filter((f)->f.complex() && f.getCollectionType()!=null ).collect(Collectors.toList());
    }
    public List<FieldSpec> getComplexNonListFields() {
        return fields.stream().filter((f)->f.complex() && f.getCollectionType()==null ).collect(Collectors.toList());
    }

    //represent to make templateable
    @Override
    public String getPackageName() {
        return super.getPackageName();
    }
    //repreent to make templateable
    @Override
    public String getSimpleName() {
        return super.getSimpleName();
    }

    public String getTableName() {
        return FieldSpec.camelCase2DatabaseUnderscore(getSimpleName());
    }


}
