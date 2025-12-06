package enigma.shared.state;


/**
 * Immutable, concise snapshot of the engine-visible machine state.
 * Example:
 * <pre>MachineState s = engine.machineData(); // read-only snapshot</pre>
 *
 * Important (concise):
 * - {@code ogCodeState} and {@code curCodeState} are string serializations of the
 *   code configuration (e.g. "<1,2,3><ODX><I>") used for compact display/transport.
 * - {@code stringsProcessed} is a point-in-time counter snapshot and may change while observed.
 * - The numeric counts reflect the loaded specification (not runtime rotor order).
 *
 * @param numOfRotors number of rotors defined in the loaded spec
 * @param numOfReflectors number of reflectors defined in the loaded spec
 * @param stringsProcessed snapshot of processed-strings counter
 * @param ogCodeState canonical code string used for reset (serialized CodeConfig)
 * @param curCodeState currently active code string (serialized CodeConfig)
 */
public record MachineState(
        int numOfRotors,
        int numOfReflectors,
        int stringsProcessed,
        CodeState ogCodeState,
        CodeState curCodeState
) {
    /**
     * Human-readable, tester-friendly summary of the machine’s state.
     * Outputs rotor/reflector counts, processed message count,
     * and both original + current code configurations.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Machine State\n");
        sb.append("------------------------------\n");
        sb.append("Rotors Defined        : ").append(numOfRotors).append("\n");
        sb.append("Reflectors Defined    : ").append(numOfReflectors).append("\n");
        sb.append("Strings Processed     : ").append(stringsProcessed).append("\n\n");

        sb.append("Original Configuration : ")
                .append(ogCodeState == null ? "<none>" : ogCodeState.toString())
                .append("\n");

        sb.append("Current Configuration  : ")
                .append(curCodeState == null ? "<none>" : curCodeState.toString())
                .append("\n");

        return sb.toString();
    }
}
