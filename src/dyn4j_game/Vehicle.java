package dyn4j_game;

import org.dyn4j.dynamics.Body;

import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * Created by stijn on 4-4-2017.
 */
public abstract class Vehicle extends GameObject implements Controlable{

    boolean isControlable = false;

    public Vehicle(){}
    public Vehicle(boolean isControlable){
        super();
        this.isControlable = isControlable;
    }

    public boolean isControlable(){return isControlable;}

    public abstract void addToWorld(GameWorld world);

    public abstract GameObject getHull();

    @Override
    public abstract void applyControl(Set<Integer> keys);
}
