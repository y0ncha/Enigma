package enigma.shared.dto.tracer;

import java.util.List;

/**
 * Trace of a single character's path through the Enigma machine.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <p>Records the complete encryption process for one character including:</p>
 * <ul>
 *   <li>Input and output characters</li>
 *   <li>Rotor window states before and after processing</li>
 *   <li>Which rotors advanced (stepping record)</li>
 *   <li>Forward pass through rotors (right→left)</li>
 *   <li>Reflector transformation</li>
 *   <li>Backward pass through rotors (left→right)</li>
 * </ul>
 *
 * <h2>Field Semantics</h2>
 * <ul>
 *   <li><b>inputChar:</b> original input character from user</li>
 *   <li><b>outputChar:</b> final encrypted output character</li>
 *   <li><b>windowBefore:</b> rotor positions as chars before stepping (e.g., "ODX")</li>
 *   <li><b>windowAfter:</b> rotor positions as chars after processing (e.g., "ODY")</li>
 *   <li><b>advancedIndices:</b> list of rotor indices that stepped (0 = leftmost)</li>
 *   <li><b>forwardSteps:</b> rotor transformations from right→left (toward reflector)</li>
 *   <li><b>reflectorStep:</b> symmetric transformation at leftmost position</li>
 *   <li><b>backwardSteps:</b> rotor transformations from left→right (back to keyboard)</li>
 * </ul>
 *
 * <h2>Rotor Position Model</h2>
 * <p>Window strings use char positions (alphabet characters) in left→right order
 * matching the user's visual perspective of the machine.</p>
 *
 * @param inputChar original input character
 * @param outputChar final output character
 * @param windowBefore rotor window before stepping (left→right, e.g., "ODX")
 * @param windowAfter rotor window after processing (left→right)
 * @param advancedIndices rotors that advanced (index 0 = leftmost)
 * @param forwardSteps rotor traces for forward pass (right→left)
 * @param reflectorStep reflector transformation trace
 * @param backwardSteps rotor traces for backward pass (left→right)
 * @since 1.0
 */
public record SignalTrace(
        char inputChar,                  // original input char
        char outputChar,                 // final output char
        String windowBefore,             // rotor window before stepping (left→right)
        String windowAfter,              // rotor window after processing (left→right)
        List<Integer> advancedIndices,   // rotors that advanced this step (index 0 = leftmost)
        List<RotorTrace> forwardSteps,   // rotor steps, right→left
        ReflectorTrace reflectorStep,    // reflector step
        List<RotorTrace> backwardSteps   // rotor steps, left→right
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Input:  ").append(inputChar).append('\n');
        sb.append("Output: ").append(outputChar).append('\n');
        sb.append("Window: before=").append(windowBefore)
                .append(" after=").append(windowAfter).append('\n');
        sb.append("Stepped: ").append(advancedIndices).append('\n');

        sb.append("\nForward path (right → left):\n");
        for (RotorTrace r : forwardSteps) {
            sb.append("  rotor ")
                    .append(r.rotorIndex()).append(" (id=").append(r.id()).append(")")
                    .append(": ")
                    .append(r.entryIndex())
                    .append(" -> ")
                    .append(r.exitIndex())
                    .append('\n');
        }

        sb.append("\nReflector:\n  ")
                .append(reflectorStep.entryIndex())
                .append(" -> ")
                .append(reflectorStep.exitIndex())
                .append('\n');

        sb.append("\nBackward path (left → right):\n");
        for (RotorTrace r : backwardSteps) {
            sb.append("  rotor ")
                    .append(r.rotorIndex()).append(" (id=").append(r.id()).append(")")
                    .append(": ")
                    .append(r.entryIndex())
                    .append(" -> ")
                    .append(r.exitIndex())
                    .append('\n');
        }

        return sb.toString();
    }
}
