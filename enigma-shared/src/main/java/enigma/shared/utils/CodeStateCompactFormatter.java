package enigma.shared.utils;

import enigma.shared.state.CodeState;

import java.util.ArrayList;
import java.util.List;

public final class CodeStateCompactFormatter {

    public static final String NOT_CONFIGURED = "<not configured>";

    private static final String SEGMENT_DELIMITER = " | ";

    private CodeStateCompactFormatter() {
    }

    public static String originalCodeCompact(CodeState codeState) {
        if (isNotConfigured(codeState)) {
            return NOT_CONFIGURED;
        }

        List<String> segments = rotorSegments(codeState);
        if (segments.isEmpty()) {
            return NOT_CONFIGURED;
        }

        String reflector = codeState.reflectorId();
        if (reflector != null && !reflector.isBlank()) {
            segments.add("R:" + reflector.trim());
        }

        String plugboard = plugboardSegments(codeState.plugStr());
        if (!plugboard.isEmpty()) {
            segments.add("P:" + plugboard);
        }

        return String.join(SEGMENT_DELIMITER, segments);
    }

    public static String currentRotorsPositionCompact(CodeState codeState) {
        if (isNotConfigured(codeState)) {
            return NOT_CONFIGURED;
        }

        List<String> segments = rotorSegments(codeState);
        if (segments.isEmpty()) {
            return NOT_CONFIGURED;
        }
        return String.join(SEGMENT_DELIMITER, segments);
    }

    private static boolean isNotConfigured(CodeState codeState) {
        return codeState == null || codeState == CodeState.NOT_CONFIGURED;
    }

    private static List<String> rotorSegments(CodeState codeState) {
        List<Integer> rotorIds = codeState.rotorIds();
        String positions = codeState.positions();
        if (rotorIds == null || positions == null || rotorIds.isEmpty() || positions.isEmpty()) {
            return List.of();
        }

        int size = Math.min(rotorIds.size(), positions.length());
        List<String> segments = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            segments.add(rotorIds.get(i) + "-" + positions.charAt(i));
        }
        return segments;
    }

    private static String plugboardSegments(String plugStr) {
        if (plugStr == null || plugStr.isBlank()) {
            return "";
        }

        List<String> plugs = new ArrayList<>();
        for (int i = 0; i + 1 < plugStr.length(); i += 2) {
            plugs.add(plugStr.charAt(i) + "-" + plugStr.charAt(i + 1));
        }
        return String.join(",", plugs);
    }
}
