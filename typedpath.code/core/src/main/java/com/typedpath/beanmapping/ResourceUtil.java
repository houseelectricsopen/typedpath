package com.typedpath.beanmapping;

import java.io.InputStream;

public class ResourceUtil {
    public static String readResource(InputStream is) throws Exception {
        byte data[] = new byte[is.available()];
        is.read(data);
        return new String(data);
    }

    public static String readResourceByName(Class theClass, String resourceName) throws Exception {
        InputStream is =  theClass.getResourceAsStream(resourceName);
        return readResource(is);
    }




}
