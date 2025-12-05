package enigma.shared.dto.tracer;

import java.util.List;

/**
 * Trace of a single character's path through the Enigma machine.
 *
 * <p>Records the input/output characters, rotor window states before and after
 * processing, which rotors advanced, and detailed forward/backward rotor traces
 * including the reflector step.</p>
 *
 * @param inputChar original input character
 * @param outputChar final output character
 * @param windowBefore rotor window before stepping (left→right)
 * @param windowAfter rotor window after processing (left→right)
 * @param advancedIndices rotors that advanced (0 = rightmost)
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
        List<Integer> advancedIndices,   // rotors that advanced this step (0 = rightmost)
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
                    .append(r.rotorIndex())
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
                    .append(r.rotorIndex())
                    .append(": ")
                    .append(r.entryIndex())
                    .append(" -> ")
                    .append(r.exitIndex())
                    .append('\n');
        }

        return sb.toString();
    }
}
