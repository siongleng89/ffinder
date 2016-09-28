package com.ffinder.android.utils;

import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by SiongLeng on 27/1/2016.
 */
public class Strings {


    public static String joinArr(ArrayList<String> arr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = arr.size(); i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(arr.get(i));
        }
        return sbStr.toString();
    }

    public static ArrayList<String> split(String input, String sSep) {
        String[] tmp = input.split(sSep);
        ArrayList<String> result = new ArrayList<String>();
        for(String s : tmp){
            result.add(s);
        }
        return result;
    }

    public static ArrayList<String> split(String input, int limitPerString){
        ArrayList<String> result = new ArrayList<String>();
        if(input.length() < limitPerString){
            result.add(input);
        }
        else{
            int index = 0;
            while (index < input.length()) {
                result.add(input.substring(index, Math.min(index + limitPerString, input.length())));
                index += limitPerString;
            }
        }

        return result;
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static String formatToTwoDec(float f){
        return String.format("%.2f", f);
    }

    public static String byteToMb(long b){
        return formatToTwoDec((float) b / 1024f / 1024f);
    }

    public static String cutOff(String input, int limit){
        if(limit == 0) limit = 9999;
        if(input == null) return null;
        if(input.length() > limit) {
            input = input.substring(0, limit - 2);
            input+="..";
        }
        return input;
    }

    public static boolean isLargerLexically(String target, String against){
        if(target.length() != against.length()){
            return target.length() > against.length();
        }
        else{
            return (target.compareTo(against) >= 0);
        }
    }

    public static boolean isEmpty(String input){
        if(input == null) return true;
        if(input.trim().equals("")) return true;
        return false;
    }

    public static String generateUniqueRandomKey(int length){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder( length );
        for( int i = 0; i < length; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static String generateUniqueNumber(int length){
        String AB = "0123456789";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder( length );
        for( int i = 0; i < length; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static String generateUserKey(){
        return generateUniqueNumber(4) + "-" + generateUniqueNumber(4) + "-" + generateUniqueNumber(4);
    }

    public static String pickNonEmpty(String... inputs){
        for(String input : inputs){
            if(!Strings.isEmpty(input)){
                return input;
            }
        }
        return "";
    }

}
