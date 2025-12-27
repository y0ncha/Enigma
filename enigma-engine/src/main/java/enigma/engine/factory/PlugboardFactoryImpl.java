package enigma.engine.factory;

import enigma.shared.alphabet.Alphabet;
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

    private final int size;

    /**
     * Create plugStr factory bound to alphabet.
     *
     * @param alphabet alphabet for sizing plugStr
     */
    public PlugboardFactoryImpl(Alphabet alphabet) {
        this.size = alphabet.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugboard create(Alphabet alphabet, String plugStr) {
        Plugboard plugboard = new PlugboardImpl(size);
        for (int i = 0; i + 1 < plugStr.length(); i += 2) {
            int a = alphabet.indexOf(plugStr.charAt(i));
            int b = alphabet.indexOf(plugStr.charAt(i + 1));
            plugboard.plug(a, b);
        }
        return plugboard;
    }
}
