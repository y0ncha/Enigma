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
) {}
