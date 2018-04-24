package com.typedpath.beanmapping.fromcglib;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class TestUniqueSimpleValueDefaulter {

    /**
     * test that unique objects are created for each type of simple class
     */
    @Test
    public void testUniques() {
        Class []theClasses = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE,
           Short.class, Short.TYPE, Character.class, Character.TYPE,
           Integer.class, Integer.TYPE, Long.class, Long.TYPE,
           Float.class, Float.TYPE, Double.class, Double.TYPE,
                LocalDate.class
        };
        for (Class theClass: theClasses) {
            int instanceTarget=3;
            Map<Object, Object> identityObjectMap = new IdentityHashMap<>();
            Map<Object, Object> objectMap = new HashMap<>();
            for (int instanceCount=0; instanceCount<instanceTarget;
                 instanceCount++) {
                 Object o1 = UniqueSimpleValueDefaulter.getNextValue(theClass);
                 identityObjectMap.put(o1, o1);
                 objectMap.put(o1, o1);
            }

            Assert.assertEquals(instanceTarget, identityObjectMap.size());
            Assert.assertEquals(1, objectMap.size());
            System.out.println("created " + instanceTarget  + " \".equal\" but not \"==\" of " + theClass.getName());
        }


    }


}
