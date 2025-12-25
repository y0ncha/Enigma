package enigma.engine.factory;

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
     * Create identity plugboard (no character swaps).
     *
     * @return new plugboard instance
     */
    Plugboard create();
}
