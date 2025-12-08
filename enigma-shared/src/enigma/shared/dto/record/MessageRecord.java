package enigma.shared.dto.record;

/**
 * Record of a processed message in the machine history.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <h2>Purpose</h2>
 * <p>MessageRecord captures the input, output, and processing duration for a
 * single message processed by the Enigma machine. These records are grouped
 * by original code configuration in the engine's history.</p>
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li><b>originalText:</b> Input message (plaintext or ciphertext)</li>
 *   <li><b>processedText:</b> Output message (encrypted or decrypted result)</li>
 *   <li><b>durationNanos:</b> Processing duration in nanoseconds
 *       <ul>
 *         <li>Measured from start to end of engine.process()</li>
 *         <li>Includes validation, machine processing, and trace generation</li>
 *         <li>Useful for performance analysis</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Usage in History</h2>
 * <p>MessageRecords are stored in lists grouped by original code configuration.
 * Each configuration gets its own list of records, allowing you to see all
 * messages processed from a particular starting position.</p>
 *
 * <p><b>Example History Structure</b>:</p>
 * <pre>
 * Original Code: <1,2,3><O,D,X><I>
 *   • Input="HELLO", Output="XYZAB", Duration=1234567ns
 *   • Input="WORLD", Output="PQRST", Duration=987654ns
 *
 * Original Code: <3,2,1><A,A,A><II>
 *   • Input="TEST", Output="MNOP", Duration=456789ns
 * </pre>
 *
 * <h2>String Format</h2>
 * <p>toString() produces: {@code Input="HELLO", Output="XYZAB", Duration=1234567ns}</p>
 *
 * @param originalText input message
 * @param processedText output message
 * @param durationNanos processing duration in nanoseconds
 * @since 1.0
 */
public record MessageRecord(
        String originalText,
        String processedText,
        long durationNanos
) {
    /**
     * Returns a formatted string representation of the message record.
     *
     * <p>Format: {@code #. <source> --> <processed> (n nano-seconds)}</p>
     *
     * @return formatted record string
     */
    @Override
    public String toString() {
        return "<" + originalText + "> --> <" + processedText + "> (" + durationNanos + " nano-seconds)";
    }
}