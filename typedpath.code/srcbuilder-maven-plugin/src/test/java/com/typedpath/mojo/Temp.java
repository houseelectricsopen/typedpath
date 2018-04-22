package com.typedpath.mojo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Temp {
    public static void main(String []args) {
        ZonedDateTime now = ZonedDateTime.now();
        String strDatetime = now.format(DateTimeFormatter.ofPattern("1"));
        System.out.println(strDatetime);
    }

}
