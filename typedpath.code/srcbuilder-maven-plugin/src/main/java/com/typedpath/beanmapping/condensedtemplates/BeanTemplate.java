package com.typedpath.beanmapping.condensedtemplates;

import com.typedpath.template.Templater;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanTemplate extends ClassSourceTemplate implements Templater.TemplateValues {

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
    public List<FieldSpec> getNonComplexListFields() {
        return fields.stream().filter((f)->!f.complex() && f.getCollectionType()!=null ).collect(Collectors.toList());
    }
    public List<FieldSpec> getNonComplexNonListFields() {
        return fields.stream().filter((f)->!f.complex() && f.getCollectionType()==null ).collect(Collectors.toList());
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

    public String getSimpleNameCamelCase() {
        String name = getSimpleName();
        return name.substring(0,1).toLowerCase()+name.substring(1);
    }

    public String getTableName() {
        return FieldSpec.camelCase2DatabaseUnderscore(getSimpleName());
    }

    public List<Import> getExtractFieldImports() {
       Set<String> result = new HashSet<>();
       //TODO check for import with the same simple name
       fields.forEach(
               f->{
                   int lastDotIndex = f.getType().lastIndexOf(".");
                   if (-1!=lastDotIndex) {
                        result.add(f.getType());
                        f.setType(f.getType().substring(lastDotIndex+1));
                   }
                   if (f.getCollectionType()!=null) {
                       lastDotIndex = f.getCollectionType().lastIndexOf(".");
                       if (-1!=lastDotIndex) {
                           result.add(f.getCollectionType());
                           f.setCollectionType(f.getCollectionType().substring(lastDotIndex+1));
                       }
                   }

               }
       );

       return result.stream().map(s->new Import(s)).collect(Collectors.toList());
    }

    public static class Import implements Templater.TemplateValues {
       final private String typeName;
       Import(String typeName) {
           this.typeName=typeName;
       }
       public String getTypename() {
           return typeName;
       }
    }

}
