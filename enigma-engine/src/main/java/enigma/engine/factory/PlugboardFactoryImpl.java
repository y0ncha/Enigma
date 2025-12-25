package enigma.engine.factory;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.plugboard.Plugboard;
import enigma.machine.component.plugboard.PlugboardImpl;

/**
 * Factory implementation for creating {@link Plugboard} instances.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * @since 1.0
 */
public class PlugboardFactoryImpl implements PlugboardFactory {

    private int size;

    public PlugboardFactoryImpl(Alphabet alphabet) {
        this.size = alphabet.size();
    }

    @Override
    public Plugboard create() {
        return new PlugboardImpl(size);
    }
}
