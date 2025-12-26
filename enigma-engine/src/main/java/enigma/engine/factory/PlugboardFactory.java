package enigma.engine.factory;

import enigma.shared.alphabet.Alphabet;
import enigma.machine.component.plugboard.Plugboard;

/**
 * Factory interface for creating {@link Plugboard} instances.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * @since 1.0
 */
public interface PlugboardFactory {
    /**
     * Create identity plugStr (no character swaps).
     *
     * @return new plugStr instance
     */
    Plugboard create(Alphabet alphabet, String plugStr);
}
