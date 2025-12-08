package enigma.machine;

import enigma.machine.component.code.Code;
import enigma.shared.dto.config.CodeConfig;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.keyboard.KeyboardImpl;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.rotor.Rotor;

import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.dto.tracer.RotorTrace;
import enigma.shared.dto.tracer.ReflectorTrace;
import enigma.shared.state.CodeState;

import java.util.ArrayList;
import java.util.List;

/**
 * Machine runtime implementation.
 *
 * <p>Concise contract:
 * - Holds a runtime {@link Code} and {@link Keyboard} and performs the
 *   mechanical Enigma processing: stepping, forward (right→left), reflector,
 *   and backward (left→right) passes for each input character.
 * - Public API is small: configure via {@link #setCode(Code)} and process
 *   characters with {@link #process(char)}. The implementation is deterministic
 *   and not responsible for high-level validation (engine handles config validation).
 * </p>
 *
 * Example:
 * <pre>
 *   MachineImpl m = new MachineImpl();
 *   m.setCode(code);            // attach runtime Code
 *   SignalTrace t = m.process('A');
 * </pre>
 *
 * Important invariants:
 * - Rotors passed in {@link Code#getRotors()} are expected left→right (index 0 = leftmost).
 * - {@link Keyboard} is the boundary for char↔index conversion; Machine uses indices internally.
 * - Methods throw {@link IllegalStateException} when the machine is not configured.
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
     * Construct an empty machine. The machine is created without a configured
     * {@link Keyboard} or {@link Code}; callers must set the code via
     * {@link #setCode(Code)} before using {@link #process(char)}.
     *
     * @since 1.0
     */
    public MachineImpl() {
        this.keyboard = null;
        this.code = null;
    }

    // ---------------------------------------------------------
    // Machine interface implementation
    // ---------------------------------------------------------
    /**
     * {@inheritDoc}
     *
     * <p>Concise: attach a runtime {@link Code} and create a {@link Keyboard}
     * based on the code alphabet. Caller must ensure the {@code Code} is
     * valid (engine performs validation).</p>
     *
     * @param code runtime code (rotors, reflector, alphabet)
     */
    @Override
    public void setCode(Code code) {
        this.code = code;
        this.keyboard = new KeyboardImpl(code.getAlphabet());
    }

    /**
     * Process one input character through the complete Enigma encryption path.
     *
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Step rotors (rightmost first, propagate left on notch)</li>
     *   <li>Convert input char → int index (via keyboard)</li>
     *   <li>Forward pass: transform right→left through rotors</li>
     *   <li>Reflector: symmetric mapping at leftmost position</li>
     *   <li>Backward pass: transform left→right through rotors</li>
     *   <li>Convert final int index → output char (via keyboard)</li>
     * </ol>
     *
     * @param input input character (must be in the machine alphabet)
     * @return trace containing output char and per-step details
     * @throws IllegalStateException if machine is not configured with code and keyboard
     * @throws IllegalArgumentException if input char is not in the alphabet
     */
    @Override
    public SignalTrace process(char input) {
        ensureConfigured();

        // Window before stepping (left→right as user sees it)
        String windowBefore = buildWindowString();

        // Step rotors (same timing as in process) and record which advanced
        List<Integer> advancedIndices = advance();

        // Keyboard encoding
        int intermediate = keyboard.toIdx(input);

        // Forward pass (right→left)
        List<RotorTrace> forwardSteps = forwardTransform(intermediate);
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
        List<RotorTrace> backwardSteps = backwardTransform(intermediate);
        if (!backwardSteps.isEmpty()) {
            intermediate = backwardSteps.getLast().exitIndex();
        }

        char outputChar = keyboard.toChar(intermediate);

        // Window after processing (rotors already stepped at the top)
        String windowAfter = buildWindowString();

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeConfig getConfig() {
        return code.getConfig();
    }

    @Override
    public void reset() {
        ensureConfigured();
        code.reset();
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------


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
     * Advance rotors starting from the rightmost rotor and record which rotors moved.
     *
     * <p><b>Stepping Logic:</b></p>
     * <ul>
     *   <li>Rightmost rotor (highest index) always advances</li>
     *   <li>If a rotor reaches its notch after advancing, the rotor to its left also advances</li>
     *   <li>Propagation continues leftward until a rotor does not reach its notch</li>
     * </ul>
     *
     * <p><b>Indexing Convention:</b> Rotors are stored left→right, so:</p>
     * <ul>
     *   <li>index 0 = leftmost rotor</li>
     *   <li>index size-1 = rightmost rotor (steps first)</li>
     * </ul>
     *
     * @return immutable list of rotor indices that advanced (using left→right indexing)
     */
    private List<Integer> advance() {

        List<Integer> advanced = new ArrayList<>();
        List<Rotor> rotors = code.getRotors();  // left→right array

        // Start at rightmost rotor (highest index in left→right array)
        int index = rotors.size() - 1;
        boolean shouldAdvance;

        do {
            Rotor rotor = rotors.get(index);
            shouldAdvance = rotor.advance();
            advanced.add(index);        // record index (0 = leftmost per left→right convention)
            index--;                    // move leftward (decrease index)
        } while (shouldAdvance && index >= 0);

        return List.copyOf(advanced);
    }

    /**
     * Apply forward transformation through rotors while recording traces.
     *
     * <p><b>Physical Direction:</b> Forward direction processes signal from
     * keyboard (right side) toward reflector (left side).</p>
     *
     * <p><b>Implementation:</b> Since rotors are stored left→right in the array,
     * we iterate from highest index (rightmost) down to 0 (leftmost).</p>
     *
     * @param entryIndex initial index from keyboard (0..alphabetSize-1)
     * @return immutable list of RotorTrace (rightmost first, in iteration order)
     */
    private List<RotorTrace> forwardTransform(int entryIndex) {

        List<RotorTrace> steps = new ArrayList<>();
        List<Rotor> rotors = code.getRotors();  // left→right array

        // Process right→left: iterate from RIGHTMOST (last index) → LEFTMOST (0)
        for (int i = rotors.size() - 1; i >= 0; i--) {
            Rotor rotor = rotors.get(i);
            int exitIndex = rotor.process(entryIndex, Direction.FORWARD);

            steps.add(new RotorTrace(
                    rotor.getId(),
                    i,  // rotor position in left→right array
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
     * <p><b>Physical Direction:</b> Backward direction processes signal from
     * reflector (left side) back toward keyboard (right side).</p>
     *
     * <p><b>Implementation:</b> Since rotors are stored left→right in the array,
     * we iterate from index 0 (leftmost) up to size-1 (rightmost).</p>
     *
     * @param value input index from reflector (0..alphabetSize-1)
     * @return immutable list of RotorTrace (leftmost first, in iteration order)
     */
    private List<RotorTrace> backwardTransform(int value) {

        List<RotorTrace> steps = new ArrayList<>();
        List<Rotor> rotors = code.getRotors();  // left→right array

        // Process left→right: iterate from LEFTMOST (0) → RIGHTMOST (last index)
        for (int i = 0; i < rotors.size(); i++) {
            Rotor rotor = rotors.get(i);
            int entryIndex = value;
            int exitIndex = rotor.process(entryIndex, Direction.BACKWARD);

            steps.add(new RotorTrace(
                    rotor.getId(),
                    i,  // rotor position in left→right array
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
     *
     * <p>Positions are represented as characters from the alphabet (e.g., "ODX").
     * The string is built in left→right order matching user's visual perspective.</p>
     *
     * @return window string in left→right format
     */
    private String buildWindowString() {


        if (keyboard == null) {
            throw new IllegalStateException("Machine is not configured with a keyboard");
        }

        StringBuilder sb = new StringBuilder();

        // user-facing left→right view: iterate rotors from leftmost (index 0) to rightmost
        for (Rotor rotor : code.getRotors()) {
            char c = rotor.getPosition();   // window position character
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Generate a detailed visual representation of the machine's wiring configuration.
     *
     * <p><b>Display Format:</b></p>
     * <ul>
     *   <li>Index column: 0-based row numbers (matches internal representation)</li>
     *   <li>Reflector column: pair labels computed as min(i, partner) + 1</li>
     *   <li>Rotor columns: left | right wiring, leftmost rotor printed first</li>
     *   <li>Keyboard column: alphabet symbols in order</li>
     * </ul>
     *
     * <p><b>Important:</b> Rows follow XML index order (0..alphabetSize-1).
     * No lexicographic sorting by letters is applied.</p>
     *
     * @return multi-line string showing complete machine wiring
     */
    @Override
    public String toString() {
        if (!isConfigured()) {
            return "Machine not configured";
        }

        List<Rotor> rotors = code.getRotors(); // left → right (index 0 = leftmost)
        Keyboard kb = keyboard;

        // Build left-to-right rotor rendering order based on configured rotor IDs.
        // The Code stores rotors in left→right order matching the config ids.
        // To guarantee the printed leftmost rotor matches the configured first rotor id,
        // map config ids to rotor instances and render according to that sequence.
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
