package dyn4j_game;

import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * Created by stijn on 4-4-2017.
 */
public interface Controlable {

    public void applyControl(Set<Integer> keys);
}
