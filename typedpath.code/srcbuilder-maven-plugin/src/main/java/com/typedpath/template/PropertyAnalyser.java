package com.typedpath.template;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PropertyAnalyser {
    public Map<String, Function<Object, Object> > extractReadableProperties(Object parent) {
        Map<String, Function<Object, Object>> result = new HashMap<>();
        Object emptyParams[] = {};
        for (Method m : parent.getClass().getDeclaredMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length==0) {
                String propertyName = m.getName().substring(3);
                Function<Object, Object> getter = p->
                {
                    try {
                        return m.invoke(parent, emptyParams);
                    } catch (Exception ex) {
                        throw new RuntimeException("cant invoke " + m.getName() + " on " + parent, ex );
                    }
                };
                result.put(propertyName, getter);
            }
        }
    return result;
    }
}
