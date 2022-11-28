package fuzs.puzzleslib.client.core;

import fuzs.puzzleslib.util.PuzzlesUtil;
import net.minecraft.client.KeyMapping;

/**
 * useful methods for client related things that require mod loader specific abstractions
 */
public interface ClientAbstractions {
    /**
     * instance of the client factories SPI
     */
    ClientAbstractions INSTANCE = PuzzlesUtil.loadServiceProvider(ClientAbstractions.class);

    /**
     * checks if a <code>keyMapping</code> is active (=pressed), Forge replaces this everywhere, so we need an abstraction
     *
     * @param keyMapping    the key mapping to check if pressed
     * @param keyCode       current key code
     * @param scanCode      scan code
     * @return              is <code>keyMapping</code> active
     */
    boolean isKeyActiveAndMatches(KeyMapping keyMapping, int keyCode, int scanCode);
}
