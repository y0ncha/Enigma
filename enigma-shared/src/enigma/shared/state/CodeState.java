package enigma.shared.state;

import java.util.List;

public record CodeState(
        List<Integer> rotorIds,
        String positions,
        List<Integer> notchDist,
        String reflectorId
) {
    @Override
    public String toString() {

        // Format rotor IDs: <1,2,3>
        String ids = rotorIds.toString().replaceAll("[\\[\\] ]", "");

        // Format window positions with notch distances: <A(2),O(5),X(20)>
        StringBuilder posBuilder = new StringBuilder();
        for (int i = 0; i < positions.length(); i++) {
            char windowChar = positions.charAt(i);
            int dist = notchDist.get(i);

            posBuilder.append(windowChar)
                    .append("(")
                    .append(dist)
                    .append(")");

            if (i < positions.length() - 1) {
                posBuilder.append(",");
            }
        }

        // Reflector ID is expected to be a full Roman numeral string

        return "<%s><%s><%s>".formatted(ids, posBuilder, reflectorId);
    }
}
