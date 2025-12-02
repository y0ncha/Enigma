package enigma.machine;

import enigma.machine.code.Code;
import enigma.machine.keyboard.Keyboard;
import enigma.machine.keyboard.KeyboardImpl;
import enigma.machine.rotor.Direction;
import enigma.machine.rotor.Rotor;

import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.dto.tracer.RotorTrace;
import enigma.shared.dto.tracer.ReflectorTrace;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link Machine} implementation that coordinates rotor stepping,
 * forward/backward transformations and reflector processing.
 *
 * @since 1.0
 */
public class MachineImpl implements Machine {

    // --- Fields --------------------------------------------------
    private Code code;
    private Keyboard keyboard;


    // --- Ctor ----------------------------------------------------
    /**
     * Construct an empty machine. The machine is created without a configured
     * {@link Keyboard} or {@link Code}; callers must set the code (and provide
     * a keyboard via the public API) before using {@link #process(char)}.
     *
     * @since 1.0
     */
    public MachineImpl() {
        this.keyboard = null;
        this.code = null;
    }

    // --- Methods -------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public void setCode(Code code) {
        this.code = code;
        this.keyboard = new KeyboardImpl(code.getAlphabet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SignalTrace process(char input) {
        ensureConfigured();

        List<Rotor> rotors = code.getRotors();

        // Window before stepping (left→right as user sees it)
        String windowBefore = buildWindowString(rotors);

        // Step rotors (same timing as in process) and record which advanced
        List<Integer> advancedIndices = advance(rotors);

        // Keyboard encoding
        int intermediate = keyboard.process(input);

        // Forward pass (right→left)
        List<RotorTrace> forwardSteps = forwardTransform(rotors, intermediate);
        if (!forwardSteps.isEmpty()) {
            intermediate = forwardSteps.getLast().exitIndex();
        }

        // Reflector
        int reflectEntry = intermediate;
        int reflectExit = code.getReflector().process(reflectEntry);
        ReflectorTrace reflectorStep = new ReflectorTrace(
                reflectEntry,
                reflectExit,
                keyboard.lightKey(reflectEntry),
                keyboard.lightKey(reflectExit)
        );
        intermediate = reflectExit;

        // Backward pass (left→right)
        List<RotorTrace> backwardSteps = backwardTransform(rotors, intermediate);
        if (!backwardSteps.isEmpty()) {
            intermediate = backwardSteps.getLast().exitIndex();
        }

        char outputChar = keyboard.lightKey(intermediate);

        // Window after processing (rotors already stepped at the top)
        String windowAfter = buildWindowString(rotors);

        return new SignalTrace(
                input,
                outputChar,
                windowBefore,
                windowAfter,
                advancedIndices,
                forwardSteps,
                reflectorStep,
                backwardSteps
        );
    }

    // --- State Checkers ---------------------------------------------
    private void ensureConfigured() {
        if (code == null) {
            throw new IllegalStateException("Machine is not configured with a code");
        }
        if (keyboard == null) {
            throw new IllegalStateException("Machine is not configured with a keyboard");
        }
    }

    @Override
    public boolean isConfigured() {
        return code != null && keyboard != null;
    }

    // --- Helpers -------------------------------------------------

    /**
     * Advance rotors starting from the rightmost rotor and record which rotors moved.
     *
     * @param rotors list of rotors in right→left order
     * @return immutable list of rotor indices that advanced (0 = rightmost)
     */
    private List<Integer> advance(List<Rotor> rotors) {

        List<Integer> advanced = new ArrayList<>();

        int rotorIndex = 0;
        boolean shouldAdvance;

        do {
            Rotor rotor = rotors.get(rotorIndex);
            shouldAdvance = rotor.advance();
            advanced.add(rotorIndex);      // this rotor advanced
            rotorIndex++;
        } while (shouldAdvance && rotorIndex < rotors.size());

        return List.copyOf(advanced);
    }

    private List<RotorTrace> forwardTransform(List<Rotor> rotors, int value) {
        ensureConfigured();

        List<RotorTrace> steps = new ArrayList<>();

        for (int i = 0; i < rotors.size(); i++) {
            Rotor rotor = rotors.get(i);
            int entryIndex = value;
            int exitIndex = rotor.process(entryIndex, Direction.FORWARD);
            char entryChar = keyboard.lightKey(entryIndex);
            char exitChar = keyboard.lightKey(exitIndex);

            steps.add(new RotorTrace(
                    i,
                    entryIndex,
                    exitIndex,
                    entryChar,
                    exitChar
            ));

            value = exitIndex;
        }

        return List.copyOf(steps);
    }

    /**
     * Apply backward transformation through rotors while recording traces.
     *
     * @param rotors list of rotors from right to left
     * @param value input index to transform
     * @return immutable list of RotorTrace (rightmost first)
     */
    private List<RotorTrace> backwardTransform(List<Rotor> rotors, int value) {
        ensureConfigured();

        List<RotorTrace> steps = new ArrayList<>();

        for (int i = rotors.size() - 1; i >= 0; i--) {
            Rotor rotor = rotors.get(i);
            int entryIndex = value;
            int exitIndex = rotor.process(entryIndex, Direction.BACKWARD);
            char entryChar = keyboard.lightKey(entryIndex);
            char exitChar = keyboard.lightKey(exitIndex);

            steps.add(new RotorTrace(
                    i,
                    entryIndex,
                    exitIndex,
                    entryChar,
                    exitChar
            ));

            value = exitIndex;
        }

        return List.copyOf(steps);
    }

    /**
     * Build the window string representing the current rotor positions.
     * @param rotors list of rotors from right to left
     * @return window string by format
     */
    private String buildWindowString(List<Rotor> rotors) {

        if (keyboard == null) {
            throw new IllegalStateException("Machine is not configured with a keyboard");
        }

        StringBuilder sb = new StringBuilder();

        // user-facing left→right view: iterate physical rotors from leftmost to rightmost
        for (int i = rotors.size() - 1; i >= 0; i--) {
            int pos = rotors.get(i).getPosition();       // numeric window index
            char c = keyboard.lightKey(pos);             // convert index -> alphabet char
            sb.append(c);
        }
        return sb.toString();
    }
}
