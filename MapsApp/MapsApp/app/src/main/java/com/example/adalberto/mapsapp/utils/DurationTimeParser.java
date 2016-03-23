package com.example.adalberto.mapsapp.utils;


public class DurationTimeParser {

    public static String parseDuration(String duration){
        String newFormat = duration.replace("hours", "hr");
        newFormat = newFormat.replace("mins", "min.");

        return newFormat;
    }

    public static int getDurationInMinutes(String duration) {
        int minutes = 0;

        try {
            String[] words = duration.split(" ");

            if (words.length > 2) {
                int hours = Integer.parseInt(words[0]);
                minutes = hours*60;
                minutes = Integer.parseInt(words[2]);
            } else {
                minutes = Integer.parseInt(words[0]);
            }
        }catch (Exception ex){

        }

        return minutes;
    }
}
