package com.typedpath.beanmapping.condensedtemplates;

import com.typedpath.template.Templater;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MappedEnum extends ClassSourceTemplate implements Templater.TemplateValues{


    public List<String> values = new ArrayList<>();
    public List<MappedEnumValue> getValues() {
        return values.stream().map(s->new MappedEnumValue(s)).collect(Collectors.toList());
    }

    //represent to make templateable
    public String getPackageName() {
        return super.getPackageName();
    }

    //repreent to make templateable
    public String getSimpleName() {
        return super.getSimpleName();
    }

}
