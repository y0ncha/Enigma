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
        int intermediate = keyboard.toIdx(input);

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
                reflectExit
        );
        intermediate = reflectExit;

        // Backward pass (left→right)
        List<RotorTrace> backwardSteps = backwardTransform(rotors, intermediate);
        if (!backwardSteps.isEmpty()) {
            intermediate = backwardSteps.getLast().exitIndex();
        }

        char outputChar = keyboard.toChar(intermediate);

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
                    keyboard.toChar(entryIndex),
                    keyboard.toChar(exitIndex)
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
                    keyboard.toChar(entryIndex),
                    keyboard.toChar(exitIndex)
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
            char c = rotor.getPosition();   // numeric window index (0-based)
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

        StringBuilder out = new StringBuilder();

        out.append("\n+----------------------------------------------------------+\n");
        out.append("|                      ENIGMA MACHINE                      |\n");
        out.append("+----------------------------------------------------------+\n\n");

        // Header for wiring table
        out.append("Detailed Wiring (Right Column → Left Column)\n\n");

        int numRows = kb.size();

        // Prepare reflector display (left of rotors)
        enigma.machine.component.reflector.Reflector refl = code.getReflector();

        // Use component toString() outputs for columns
        String reflCol = refl.toString();

        List<String> rotorCols = new ArrayList<>();
        for (Rotor r : leftToRight) rotorCols.add(r.toString());

        // Build idx column (matches rotor column height)
        final int colInner = 9;
        final int idxInner = 5;
        final String gap = "  ";

        java.util.function.BiFunction<String,Integer,String> center = (s,w) -> {
            if (s == null) s = "";
            if (s.length() >= w) return s.substring(0,w);
            int left = (w - s.length())/2;
            int right = w - s.length() - left;
            return " ".repeat(left) + s + " ".repeat(right);
        };

        // Compose idx column using same visual style as rotor.toString()
        StringBuilder idxSb = new StringBuilder();
        // for rotor/ref columns
        int colWidth = colInner + 4; // total width per column
        // leading padding to match other columns which start with two spaces
        String lead = "  ";

        // small ID box for Idx - use same inner width as other columns so header aligns
        idxSb.append(lead).append("┌").append("─".repeat(colInner)).append("┐").append('\n');
        idxSb.append(lead).append("│").append(center.apply("Idx", colInner)).append("│").append('\n');
        idxSb.append(lead).append("└").append("─".repeat(colInner)).append("┘").append('\n');

        // main tall box top
        idxSb.append(lead).append("┌").append("─".repeat(colInner)).append("┐").append('\n');

        // data rows: numbers (0-based) as requested by you
        for (int r = 0; r < numRows; r++) {
            String num = String.valueOf(r);
            idxSb.append(lead).append("│").append(center.apply(num, colInner)).append("│").append('\n');
        }

        // bottom
        idxSb.append(lead).append("└").append("─".repeat(colInner)).append("┘").append('\n');

        // Alphabet column (rightmost) build
        StringBuilder alphaSb = new StringBuilder();
        alphaSb.append(lead).append("┌").append("─".repeat(colInner)).append("┐").append('\n');
        alphaSb.append(lead).append("│").append(center.apply("KB", colInner)).append("│").append('\n');
        alphaSb.append(lead).append("└").append("─".repeat(colInner)).append("┘").append('\n');
        alphaSb.append(lead).append("┌").append("─".repeat(colInner)).append("┐").append('\n');
        for (int r = 0; r < numRows; r++) {
            char c = kb.toChar(r);
            alphaSb.append(lead).append("│").append(center.apply(String.valueOf(c), colInner)).append("│").append('\n');
        }
        alphaSb.append(lead).append("└").append("─".repeat(colInner)).append("┘").append('\n');

        // Now split columns into lines
        java.util.List<String[]> cols = new ArrayList<>();
        cols.add(idxSb.toString().split("\\R", -1));
        cols.add(reflCol.split("\\R", -1));
        for (String s : rotorCols) cols.add(s.split("\\R", -1));
        cols.add(alphaSb.toString().split("\\R", -1));

        // compute max lines and widths (use max line length per column)
        int maxLines = 0;
        int[] widths = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            String[] arr = cols.get(i);
            int w = 0;
            for (String lineStr : arr) {
                if (lineStr != null) w = Math.max(w, lineStr.length());
            }
            widths[i] = w;
            maxLines = Math.max(maxLines, arr.length);
        }

        // Stitch lines together, right-pad pieces to column width for alignment
        for (int line = 0; line < maxLines; line++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < cols.size(); c++) {
                String[] arr = cols.get(c);
                String piece = line < arr.length ? arr[line] : "";
                int pad = widths[c] - piece.length();
                row.append(piece);
                if (pad > 0) row.append(" ".repeat(pad));
                if (c < cols.size() - 1) row.append(gap);
            }
            out.append(row).append('\n');
        }

        out.append('\n');

        return out.toString();
    }
 }