package com.anthonyrhowell.newsgetter;

public class Ordinal {

    public static int getInt(String text){
        if(text.length() == 3){
            return Integer.valueOf(text.substring(0,1));
        } else {
            return Integer.valueOf(text.substring(0, 2));
        }
    }
}
