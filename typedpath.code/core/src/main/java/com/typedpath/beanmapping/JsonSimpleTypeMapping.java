package com.typedpath.beanmapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class JsonSimpleTypeMapping {

    //TODO externalise this
    public static String writeJsonValue(Object o) {
        if (o==null) {
            return "null";
        } else if (o instanceof String) {
            return String.format("\"%s\"", o);
        } else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue()?"true":"false";
        } else if (o instanceof UUID) {
            return String.format("\"%s\"", o);
        } else if (o instanceof LocalDate) {
            return String.format("\"%s\"", localDateFormat.format((LocalDate)o));
        }
        else return o.toString();
    }

    static DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    //can be used as a default but should only reference types in java runtime
    public static <T> T objectToNative(Object source, Class<T> destinationType) {
        if (source==null) {
            return null;
        }
        Object result = source;
        if (destinationType.equals(UUID.class))
        {
            result = UUID.fromString(source.toString());
        } else if (destinationType==Byte.TYPE || destinationType==Byte.class) {
            if (!(source instanceof Number)) {
                throw new RuntimeException(String.format("cant convert %s::%s to a ", source.getClass().getName(), source) );
            }
            result = ((Number)source).byteValue();
        } else if (destinationType==Byte.TYPE || destinationType==Byte.class) {
            if (!(source instanceof Number)) {
                throw new RuntimeException(String.format("cant convert %s::%s to a ", source.getClass().getName(), source) );
            }
            result = ((Number)source).byteValue();
        } else if (destinationType==Short.TYPE || destinationType==Short.class) {
            if (!(source instanceof Number)) {
                throw new RuntimeException(String.format("cant convert %s::%s to a ", source.getClass().getName(), source) );
            }
            result = ((Number)source).shortValue();
        } else if (destinationType==Integer.TYPE || destinationType==Integer.class) {
            if (!(source instanceof Number)) {
                throw new RuntimeException(String.format("cant convert %s::%s to a ", source.getClass().getName(), source) );
            }
            result = ((Number)source).intValue();
        }  else if (destinationType==Long.TYPE || destinationType==Long.class) {
            if (!(source instanceof Number)) {
                throw new RuntimeException(String.format("cant convert %s::%s to a ", source.getClass().getName(), source) );
            }
            result = ((Number)source).longValue();
        }
        else if (destinationType.equals(LocalDate.class)) {
            result =  LocalDate.parse( (String)source, localDateFormat);
        }
        return (T) result;
    }



}
