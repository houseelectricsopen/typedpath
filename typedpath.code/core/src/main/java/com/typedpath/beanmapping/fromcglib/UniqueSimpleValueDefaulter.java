package com.typedpath.beanmapping.fromcglib;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

// TODO make this open
public class UniqueSimpleValueDefaulter {

      //TODO open this up
      public static Object getNextValue(Class theClass) {
          if (String.class == theClass) {
              return new String("hi");
          } else if (UUID.class == theClass) {
                  return UUID.randomUUID();
          } else if (Integer.class == theClass || Integer.TYPE==theClass) {
              return new Integer(1);
          } else if (Long.class == theClass || Long.TYPE==theClass) {
              return new Long(1);
          } else if (Boolean.class == theClass || Boolean.TYPE==theClass) {
              return new Boolean(true);
          } else if (Float.class == theClass || Float.TYPE==theClass) {
              return new Float(1);
          } else if (Double.class == theClass || Double.TYPE==theClass) {
              return new Double(1);
          } else if (Short.class == theClass || Short.TYPE==theClass) {
              return new Short((short)1 );
          } else if (Byte.class == theClass || Byte.TYPE==theClass) {
              return new Byte((byte) 1 );
          }
          else if (Character.class == theClass || Character.TYPE==theClass) {
                   return new Character('a');
          }
          else if (LocalDate.class == theClass) {
              return LocalDate.now();
          }

          throw new RuntimeException("cant getNextValue for type  " + theClass.getName() );
      }




}
