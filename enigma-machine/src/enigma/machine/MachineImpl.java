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

        // Convert to left→right for user-facing display.
        // `code.getRotors()` returns rotors in right→left order, so copy
        // and reverse to produce left→right for rendering (index 0 = leftmost).
        // Build left-to-right rotor rendering order based on configured rotor IDs
        // The Code may store rotors in either order; to guarantee the printed
        // leftmost rotor matches the configured first rotor id, map config ids
        // to rotor instances and render according to that sequence.
        List<Rotor> leftToRight = new ArrayList<>();
        try {
            List<Integer> configuredIds = code.getRotorIds(); // expected left->right
            // Map rotor id -> Rotor instance
            java.util.Map<Integer, Rotor> idToRotor = new java.util.HashMap<>();
            for (Rotor r : rotors) idToRotor.put(r.getId(), r);

            boolean allFound = true;
            for (Integer id : configuredIds) {
                Rotor r = idToRotor.get(id);
                if (r == null) { allFound = false; break; }
                leftToRight.add(r);
            }
            if (!allFound) {
                // Fallback: use rotor objects order as provided
                leftToRight = new ArrayList<>(rotors);
            }
        } catch (Exception e) {
            // Any unexpected error: fallback to raw rotor list
            leftToRight = new ArrayList<>(rotors);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("\n+----------------------------------------------------------+\n");
        sb.append("|                      ENIGMA MACHINE                      |\n");
        sb.append("+----------------------------------------------------------+\n\n");

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

        // Rotor boxes (use the same left-to-right order we will render the data rows in)
        for (Rotor r : leftToRight) {
            idTop.append(gap).append("┌").append("─".repeat(colInner)).append("┐");
            idMid.append(gap).append("│").append(center.apply("Rotor " + r.getId(), colInner)).append("│");
            idBot.append(gap).append("└").append("─".repeat(colInner)).append("┘");
        }

        sb.append(idTop).append("\n");
        sb.append(idMid).append("\n");
        sb.append(idBot).append("\n");

        // IMPORTANT: wiring table must match the XML spec and the configured code.
        //
        // - Row index `row` is the 0-based contact on the RIGHT column (Idx = row+1).
        // - For the reflector we show the numeric pair label taken from the XML
        //   `<BTE-Reflect input="X" output="Y"/>`. Because the reflector is symmetric
        //   and the XML always stores the smaller index as `input`, we label each pair
        //   with `pairLabel = min(row, refl.process(row)) + 1` so both ends of a wire
        //   (e.g. 1 and 25) show the same number.
        //
        // - For rotors, `leftToRight` is built from `code.getRotorIds()` (left→right),
        //   so the printed columns "Rotor <id>" match the configured rotor order.
        //   `getWireRight(i)` / `getWireLeft(i)` must return the static XML wiring
        //   (0-based indices into the keyboard alphabet), and we render them as
        //   `rightLetter | leftLetter` using `keyboard.lightKey(...)`.
        //
        // If you change how rotors / reflector are stored, keep these invariants so
        // the table stays compatible with the XML and the active code configuration.
        for (int row = 0; row < numRows; row++) {
            StringBuilder rowLine = new StringBuilder();

            // index column (1-based)
            rowLine.append("  │").append(center.apply(String.valueOf(row + 1), idxInner)).append("│").append(gap);

            // reflector cell: show the wired pair label (use the XML 'input' label)
            // compute partner index and use the smaller index as the pair id (1-based)
            int a = row;                    // 0-based index entering on right side
            int b = refl.process(row);      // partner index (0-based)
            int pairLabel = Math.min(a, b) + 1; // 1-based label matching XML input
            String reflCell = String.valueOf(pairLabel);
            rowLine.append("│").append(center.apply(reflCell, colInner)).append("│");

            // rotor columns (leftmost -> rightmost)
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
