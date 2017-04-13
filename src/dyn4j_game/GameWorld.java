package dyn4j_game;

import org.dyn4j.dynamics.World;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by stijn on 4-4-2017.
 */
public class GameWorld extends World {

    public Set<Vehicle> vehicles = new HashSet<>();

    public GameWorld(){};

    public void addVehicle(Vehicle v){
        this.vehicles.add(v);
    }
}
