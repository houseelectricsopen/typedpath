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
          } else if (LocalDate.class == theClass) {
              return LocalDate.of((int) index.get().getAndIncrement(), 1,1);
          }

          throw new RuntimeException("cant getNextValue for type  " + theClass.getName());
      }




}
