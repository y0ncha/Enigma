package enigma.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.keyboard.KeyboardImpl;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.rotor.Rotor;

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

        int index = rotors.size() - 1; // start at RIGHTMOST
        boolean shouldAdvance;

        do {
            Rotor rotor = rotors.get(index);
            shouldAdvance = rotor.advance();
            advanced.add(index);        // record natural index (left→right convention)
            index--;                    // move leftward
        } while (shouldAdvance && index >= 0);

        return List.copyOf(advanced);
    }

    private List<RotorTrace> forwardTransform(List<Rotor> rotors, int entryIndex) {

        List<RotorTrace> steps = new ArrayList<>();

        // iterate from RIGHTMOST (last index) → LEFTMOST (0)
        for (int i = rotors.size() - 1; i >= 0; i--) {
            Rotor rotor = rotors.get(i);
            int exitIndex = rotor.process(entryIndex, Direction.FORWARD);

            steps.add(new RotorTrace(
                    rotor.getId(),
                    i,
                    entryIndex,
                    exitIndex,
                    keyboard.lightKey(entryIndex),
                    keyboard.lightKey(exitIndex)
            ));
            entryIndex = exitIndex;
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

        List<RotorTrace> steps = new ArrayList<>();

        // iterate from LEFTMOST (0) → RIGHTMOST (last index)
        for (int i = 0; i < rotors.size(); i++) {
            Rotor rotor = rotors.get(i);
            int entryIndex = value;
            int exitIndex = rotor.process(entryIndex, Direction.BACKWARD);

            steps.add(new RotorTrace(
                    rotor.getId(),
                    i,
                    entryIndex,
                    exitIndex,
                    keyboard.lightKey(entryIndex),
                    keyboard.lightKey(exitIndex)
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

        // user-facing left→right view: iterate rotors from leftmost (index 0) to rightmost
        for (Rotor rotor : rotors) {
            int pos = rotor.getPosition();   // numeric window index (0-based)
            char c = keyboard.lightKey(pos);         // convert index -> alphabet char
            sb.append(c);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        if (!isConfigured()) {
            return "Machine not configured";
        }

        List<Rotor> rotors = code.getRotors(); // right → left internally
        Keyboard kb = keyboard;

        // Convert to left→right for user-facing display
        List<Rotor> leftToRight = new ArrayList<>(rotors);
        java.util.Collections.reverse(leftToRight);

        StringBuilder sb = new StringBuilder();

        sb.append("\n+----------------------------------------------------------+\n");
        sb.append("|                      ENIGMA MACHINE                      |\n");
        sb.append("+----------------------------------------------------------+\n\n");

        // Reflector
        sb.append("Reflector: ").append(code.getReflector().getId()).append("\n\n");

        // Header for wiring table
        sb.append("Detailed Wiring (Right Column → Left Column)\n\n");

        int numRows = kb.size();

        // Prepare reflector display (left of rotors)
        enigma.machine.component.reflector.Reflector refl = code.getReflector();

        // Column inner widths
        final int idxInner = 5;     // index column inner width (e.g. '  1  ')
        final int colInner = 9;     // reflector and rotor inner width
        final String gap = "  ";   // gap between columns

        // helpers for centering text inside a fixed-width cell
        // center a string to width w
        java.util.function.BiFunction<String,Integer,String> center = (s,w) -> {
            if (s == null) s = "";
            if (s.length() >= w) return s.substring(0,w);
            int left = (w - s.length())/2;
            int right = w - s.length() - left;
            return " ".repeat(left) + s + " ".repeat(right);
        };

        // Draw small ID boxes above each column (Idx, Ref, Rotor N)
        // Box widths equal the column inner widths so they align perfectly
        StringBuilder idTop = new StringBuilder();
        StringBuilder idMid = new StringBuilder();
        StringBuilder idBot = new StringBuilder();

        // start padding
        idTop.append("  ");
        idMid.append("  ");
        idBot.append("  ");

        // Idx box
        idTop.append("┌").append("─".repeat(idxInner)).append("┐").append(gap);
        idMid.append("│").append(center.apply("Idx", idxInner)).append("│").append(gap);
        idBot.append("└").append("─".repeat(idxInner)).append("┘").append(gap);

        // Reflector box
        idTop.append("┌").append("─".repeat(colInner)).append("┐");
        idMid.append("│").append(center.apply("Ref " + (refl.getId() == null ? "" : refl.getId()), colInner)).append("│");
        idBot.append("└").append("─".repeat(colInner)).append("┘");

        // Rotor boxes
        for (Rotor r : leftToRight) {
            idTop.append(gap).append("┌").append("─".repeat(colInner)).append("┐");
            idMid.append(gap).append("│").append(center.apply("Rotor " + r.getId(), colInner)).append("│");
            idBot.append(gap).append("└").append("─".repeat(colInner)).append("┘");
        }

        sb.append(idTop).append("\n");
        sb.append(idMid).append("\n");
        sb.append(idBot).append("\n");

        // Top border line for all columns
        StringBuilder topLine = new StringBuilder();
        topLine.append("  ").append("┌").append("─".repeat(idxInner)).append("┐").append(gap)
               .append("┌").append("─".repeat(colInner)).append("┐");
        for (int i = 0; i < leftToRight.size(); i++) {
            topLine.append(gap).append("┌").append("─".repeat(colInner)).append("┐");
        }
        sb.append(topLine).append("\n");

        // (no inner label/separator row; boxed IDs above are sufficient)

        // Data rows: index, reflector char, then each rotor R|L
        for (int row = 0; row < numRows; row++) {
            StringBuilder rowLine = new StringBuilder();
            // index column (1-based)
            rowLine.append("  │").append(center.apply(String.valueOf(row + 1), idxInner)).append("│").append(gap);

            // reflector value
            char rc = kb.lightKey(refl.process(row));
            rowLine.append("│").append(center.apply(String.valueOf(rc), colInner)).append("│");

            // rotor columns
            for (Rotor rotor : leftToRight) {
                int rightVal = rotor.getWireRight(row);
                int leftVal = rotor.getWireLeft(row);
                String cell = center.apply(kb.lightKey(rightVal) + " | " + kb.lightKey(leftVal), colInner);
                rowLine.append(gap).append("│").append(cell).append("│");
            }

            sb.append(rowLine).append("\n");
        }

        // Bottom border line
        StringBuilder bottomLine = new StringBuilder();
        bottomLine.append("  └").append("─".repeat(idxInner)).append("┘").append(gap)
                  .append("└").append("─".repeat(colInner)).append("┘");
        for (int i = 0; i < leftToRight.size(); i++) {
            bottomLine.append(gap).append("└").append("─".repeat(colInner)).append("┘");
        }
        sb.append(bottomLine).append("\n\n");

        return sb.toString();
    }
 }
