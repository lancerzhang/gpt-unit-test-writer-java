package com.example.demo.utils;

import java.util.Collections;
import java.util.List;

public class Utils {

    public static String convertToRanges(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "";
        }

        Collections.sort(numbers);

        StringBuilder sb = new StringBuilder();
        int start = numbers.get(0);
        int previous = start;

        for (int i = 1; i < numbers.size(); i++) {
            int current = numbers.get(i);
            if (current - previous > 1) {
                if (previous != start) {
                    sb.append(start).append("-").append(previous).append(", ");
                } else {
                    sb.append(start).append(", ");
                }
                start = current;
            }
            previous = current;
        }

        if (previous != start) {
            sb.append(start).append("-").append(previous);
        } else {
            sb.append(start);
        }

        return sb.toString();
    }


}
