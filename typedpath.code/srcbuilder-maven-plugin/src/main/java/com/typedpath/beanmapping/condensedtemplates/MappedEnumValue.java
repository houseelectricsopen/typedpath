package com.typedpath.beanmapping.condensedtemplates;

import com.typedpath.template.Templater;

public class MappedEnumValue implements Templater.TemplateValues {

    private String value;

    public String getValue() {
        return value;
    }

    public MappedEnumValue(String value) {
        this.value=value;
    }


}
