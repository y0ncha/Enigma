package enigma.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.plugboard.Plugboard;
import enigma.shared.dto.config.CodeConfig;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.keyboard.KeyboardImpl;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.rotor.Rotor;

import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.dto.tracer.RotorTrace;
import enigma.shared.dto.tracer.ReflectorTrace;
import enigma.shared.state.CodeState;
import enigma.machine.component.reflector.Reflector;

import java.util.ArrayList;
import java.util.List;

/**
 * Machine runtime implementation performing Enigma encryption.
 *
 * <p>Manages code configuration and processes characters through stepping,
 * forward pass, reflector, and backward pass. Maintains rotor positions
 * and generates signal traces for debugging.</p>
 *
 * @since 1.0
 */
public class MachineImpl implements Machine {

    // ---------------------------------------------------------
    // Fields
    // ---------------------------------------------------------
    private Code code;
    private Keyboard keyboard;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------
    /**
     * Construct an empty machine. Code must be set before processing.
     *
     * @since 1.0
     */
    public MachineImpl() {
        this.code = null;
    }

    // ---------------------------------------------------------
    // Machine interface implementation
    // ---------------------------------------------------------
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
        assertConfigured();

        String windowBefore = buildWindowString();
        List<Integer> advancedIndices = advance();

        // 1. Keyboard key -> index translation
        int intermediate = keyboard.toIdx(input);

        // 2. Plugboard swap
        intermediate = plugboardTransition(intermediate);

        // 3. Rotor step
        List<RotorTrace> forwardSteps = forwardTransform(intermediate);
        if (!forwardSteps.isEmpty()) {
            intermediate = forwardSteps.getLast().exitIndex();
        }

        // 4. Reflector step
        int reflectEntry = intermediate;
        int reflectExit = code.getReflector().process(reflectEntry);
        ReflectorTrace reflectorStep = new ReflectorTrace(
                reflectEntry,
                reflectExit
        );
        intermediate = reflectExit;

        // 5. Rotor step (backwards)
        List<RotorTrace> backwardSteps = backwardTransform(intermediate);
        if (!backwardSteps.isEmpty()) {
            intermediate = backwardSteps.getLast().exitIndex();
        }

        // 6. Plugboard swap (backwards)
        intermediate = plugboardTransition(intermediate);

        // 7. Index -> key translation
        char outputChar = keyboard.toChar(intermediate);
        String windowAfter = buildWindowString();

        // Return detailed trace
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

    // ---------------------------------------------------------
    // State Checkers
    // ---------------------------------------------------------
    private void assertConfigured() {
        if (code == null) {
            throw new IllegalStateException("Machine is not configured");
        }
        if (keyboard == null) {
            throw new IllegalStateException("Machine keyboard is not initialized");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        return code != null && keyboard != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeConfig getConfig() {
        return code.getConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        assertConfigured();
        code.reset();
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public int plugboardTransition(int input) {
        Plugboard plugboard = code.getPlugboard();
        return plugboard.swap(input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void plug(char a, char b) {
        Plugboard plugboard = code.getPlugboard();
        plugboard.plug(keyboard.toIdx(a),keyboard.toIdx(b));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeState getCodeState() {
        // If code is not configured, return a default 'not configured' state
        if (!isConfigured()) return CodeState.notConfigured();

        String positions = buildWindowString();
        List<Integer> notchDist = code.getNotchDist();
        String reflectorId = code.getReflector().getId();
        return new CodeState(code.getRotorIds(), positions, notchDist, reflectorId, "");
    }

    /**
     * Advance rotors starting from rightmost, propagating leftward on notch.
     *
     * @return list of rotor indices that advanced
     */
    private List<Integer> advance() {

        List<Integer> advanced = new ArrayList<>();
        List<Rotor> rotors = code.getRotors();

        int index = rotors.size() - 1;
        boolean shouldAdvance;

        do {
            Rotor rotor = rotors.get(index);
            shouldAdvance = rotor.advance();
            advanced.add(index);
            index--;
        } while (shouldAdvance && index >= 0);

        return List.copyOf(advanced);
    }

    /**
     * Process signal forward through rotors (keyboard to reflector).
     *
     * @param entryIndex initial index from keyboard
     * @return list of rotor traces in iteration order
     */
    private List<RotorTrace> forwardTransform(int entryIndex) {

        List<RotorTrace> steps = new ArrayList<>();
        List<Rotor> rotors = code.getRotors();

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
     * Process signal backward through rotors (reflector to keyboard).
     *
     * @param value input index from reflector
     * @return list of rotor traces in iteration order
     */
    private List<RotorTrace> backwardTransform(int value) {

        List<RotorTrace> steps = new ArrayList<>();
        List<Rotor> rotors = code.getRotors();

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
     * Build current window string from rotor positions.
     *
     * @return position string in left→right order
     */
    private String buildWindowString() {

        if (keyboard == null) {
            throw new IllegalStateException("Machine keyboard is not initialized");
        }

        StringBuilder sb = new StringBuilder();

        for (Rotor rotor : code.getRotors()) {
            char c = rotor.getPosition();
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Generate detailed visual representation of machine wiring.
     *
     * @return multi-line string showing complete machine configuration
     */
    @Override
    public String toString() {
        if (!isConfigured()) {
            return "Machine not configured";
        }

        List<Rotor> rotors = code.getRotors();
        Keyboard kb = keyboard;

        List<Rotor> leftToRight = new ArrayList<>();
        try {
            List<Integer> configuredIds = code.getRotorIds();
            java.util.Map<Integer, Rotor> idToRotor = new java.util.HashMap<>();
            for (Rotor r : rotors) idToRotor.put(r.getId(), r);

            boolean allFound = true;
            for (Integer id : configuredIds) {
                Rotor r = idToRotor.get(id);
                if (r == null) { allFound = false; break; }
                leftToRight.add(r);
            }
            if (!allFound) {
                leftToRight = new ArrayList<>(rotors);
            }
        }
        catch (Exception e) {
            leftToRight = new ArrayList<>(rotors);
        }

        StringBuilder out = new StringBuilder();

        out.append("A Look Under the Hood (Right Column → Left Column)\n");
        out.append("--------------------------------------------------------\n\n");

        int numRows = kb.size();

        // Prepare reflector display (left of rotors)
        Reflector refl = code.getReflector();

        // Use component toString() outputs for columns
        String reflCol = refl.toString();

        List<String> rotorCols = new ArrayList<>();
        for (Rotor r : leftToRight) rotorCols.add(r.toString());

        // Build idx column (matches rotor column height)
        final int colInner = 9;
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
