package enigma.shared.dto.tracer;

import java.util.List;


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
                    .append(r.entryChar()).append('(').append(r.entryIndex()).append(')')
                    .append(" -> ")
                    .append(r.exitChar()).append('(').append(r.exitIndex()).append(')')
                    .append('\n');
        }

        sb.append("\nReflector:\n  ")
                .append(reflectorStep.entryChar()).append('(').append(reflectorStep.entryIndex()).append(')')
                .append(" -> ")
                .append(reflectorStep.exitChar()).append('(').append(reflectorStep.exitIndex()).append(')')
                .append('\n');

        sb.append("\nBackward path (left → right):\n");
        for (RotorTrace r : backwardSteps) {
            sb.append("  rotor ")
                    .append(r.rotorIndex())
                    .append(": ")
                    .append(r.entryChar()).append('(').append(r.entryIndex()).append(')')
                    .append(" -> ")
                    .append(r.exitChar()).append('(').append(r.exitIndex()).append(')')
                    .append('\n');
        }

        return sb.toString();
    }
}
