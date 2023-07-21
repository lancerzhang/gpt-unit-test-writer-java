package com.example.demo.utils;

import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodDetails;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UtUtils {

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

    public static AbstractMap.SimpleEntry<String, String> filterAndConvertCoverageLines(MethodDetails details, CoverageDetails coverageDetails) {
        int startLine = details.getStartLine();
        int endLine = details.getEndLine();

        // Filter the not covered and partly covered lines to only include those within the method
        List<Integer> notCoveredLines = coverageDetails.getNotCoveredLines().stream()
                .filter(line -> line >= startLine && line <= endLine)
                .collect(Collectors.toList());
        List<Integer> partlyCoveredLines = coverageDetails.getPartlyCoveredLines().stream()
                .filter(line -> line >= startLine && line <= endLine)
                .collect(Collectors.toList());

        // Change notCoveredLines and partlyCoveredLines to string
        String notCoveredLinesString = convertToRanges(notCoveredLines);
        String partlyCoveredLinesString = convertToRanges(partlyCoveredLines);

        return new AbstractMap.SimpleEntry<>(notCoveredLinesString, partlyCoveredLinesString);
    }


}
