package com.typedpath.beanmapping.fromjsonschema;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class JsonUtil {

    static DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    //can be used as a default but should only reference types in java runtime
    public static <T> T ScriptObjectMirrorToNative(Object source, Class<T> destinationType) {
        Object result = source;
        if (source!=null && destinationType.equals(UUID.class))
        {
            result = UUID.fromString(source.toString());
        } else if (source != null && destinationType.equals(LocalDate.class)) {
            result =  LocalDate.parse( (String)source, localDateFormat);
        }
        return (T) result;
    }

    public static ScriptObjectMirror stringToJson(String strJson) throws Exception {
        String json = "var result = " + strJson+ "; result;";
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        return (ScriptObjectMirror) engine.eval(json );
    }

    public static void printJsonObject(String jsonObject) throws Exception {
        ScriptObjectMirror result = stringToJson(jsonObject);
        System.out.println(result.getClass().getName() + "::" + result);
        print("", result);
    }

    public static void print(String indent, ScriptObjectMirror to) {
        for (Map.Entry<String, Object> entry : to.entrySet()) {
            if (entry.getValue()!=null && entry.getValue() instanceof ScriptObjectMirror) {
                System.out.println(indent + (((ScriptObjectMirror) entry.getValue()).getClassName()) + "::" +  entry.getKey() + "==");
                print(indent + "  ", (ScriptObjectMirror) entry.getValue() );
            } else {
                System.out.println(indent + entry.getKey() + "==" + entry.getValue());
            }
        }
    }

}
