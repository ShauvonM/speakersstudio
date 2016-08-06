package com.thespeakers_studio.thespeakersstudioapp.utils;

import android.util.Log;

import java.util.TreeMap;

/**
 * Created by smcgi_000 on 7/19/2016.
 */
public class OutlineHelper {
    private TreeMap<Integer, String> map;

    public OutlineHelper() {
        map = new TreeMap<>();

        map.put(1000, "m");
        map.put(900, "cm");
        map.put(500, "d");
        map.put(400, "cd");
        map.put(100, "c");
        map.put(90, "xc");
        map.put(50, "l");
        map.put(40, "xl");
        map.put(10, "x");
        map.put(9, "ix");
        map.put(5, "v");
        map.put(4, "iv");
        map.put(1, "i");
    }

    public String toRoman(int number) {
        int l = map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number - l);
    }

    // utility method to get either "1. 2. 3." "a. b. c." or "i. ii. iii. iv." etc
    public String getBullet(int level, int index) {
        switch(level) {
            case 1:
                return String.valueOf(index) + ".";
            case 2:
                return " abcdefghijklmnopqrstuvwxyz".toCharArray()[index] + ".";
            case 3:
                return toRoman(index) + ".";
            default:
                return "";
        }
    }
}
