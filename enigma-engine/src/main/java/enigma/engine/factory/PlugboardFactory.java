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
    // TODO document
    Plugboard create();
}
