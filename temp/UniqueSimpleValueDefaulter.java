package com.typedpath.beanmapping.fromcglib;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

// TODO make this open
public class UniqueSimpleValueDefaulter {
      private static ThreadLocal<AtomicLong> index = new ThreadLocal<>();
      public static void reset() {
          index.set(new AtomicLong());
      }

      public static Object getNextValue(Class theClass) {
          if (String.class == theClass) {
              return ""+index.get().getAndIncrement();
          } else if (UUID.class == theClass) {
                  return UUID.randomUUID();
          } else if (Integer.class == theClass || Integer.TYPE==theClass) {
              return (int) index.get().getAndIncrement();
          } else if (Long.class == theClass || Long.TYPE==theClass) {
              return index.get().getAndIncrement();
          } else if (Boolean.class == theClass || Boolean.TYPE==theClass) {
              return new Boolean(true);
          } else if (Float.class == theClass || Float.TYPE==theClass) {
              return new Float(index.get().getAndIncrement());
          } else if (Double.class == theClass || Double.TYPE==theClass) {
              return new Double(index.get().getAndIncrement());
          } else if (Short.class == theClass || Short.TYPE==theClass) {
              return new Short((short) index.get().getAndIncrement() );
          } else if (Byte.class == theClass || Byte.TYPE==theClass) {
              return new Byte((byte) index.get().getAndIncrement() );
          }
          else if (Character.class == theClass || Character.TYPE==theClass) {
              int currentIndex = (int) index.get().getAndIncrement();
              char[] chars = Character.toChars(currentIndex);
              //TODO fix this
              if (chars.length>1) {
                  throw new RuntimeException("character cant cope with field count above " + currentIndex);
              }
              return new Character(chars[0]);
          }
          else if (LocalDate.class == theClass) {
              return LocalDate.of((int) index.get().getAndIncrement(), 1,1);
          }

          throw new RuntimeException("cant getNextValue for type  " + theClass.getName() );
      }




}
