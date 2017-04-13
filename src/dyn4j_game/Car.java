package dyn4j_game;

import org.dyn4j.dynamics.joint.WheelJoint;
import org.dyn4j.geometry.*;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by stijn on 31-3-2017.
 */
public class Car extends Vehicle{

    private GameObject carHull;
    private GameObject wheel1;
    private GameObject wheel2;
    private WheelJoint wj1;
    private WheelJoint wj2;

    public Car(boolean controlable){
        super(controlable);

        carHull = new GameObject();
        carHull.addFixture(new Rectangle(4,1));
        carHull.setMass(MassType.NORMAL);
        carHull.getFixture(0).setDensity(50);
        carHull.setMass(carHull.getFixture(0).createMass());

        wheel1 = new GameObject();
        wheel1.addFixture(new Circle(0.5));
        wheel1.setMass(MassType.NORMAL);
        wheel1.translate(1.5,-1.1);
        wheel1.getFixture(0).setFriction(15);
        wheel1.getFixture(0).setDensity(50);
        wheel1.setMass(wheel1.getFixture(0).createMass());

        wheel2 = new GameObject();
        wheel2.addFixture(new Circle(0.5));
        wheel2.setMass(MassType.NORMAL);
        wheel2.translate(-1.5,-1.1);
        wheel2.getFixture(0).setFriction(15);
        wheel2.getFixture(0).setDensity(50);
        wheel2.setMass(wheel2.getFixture(0).createMass());

        wj1 = new WheelJoint(carHull, wheel1, wheel1.getWorldCenter(), new Vector2(0,1));
        wj1.setFrequency(1000);
        wj1.setMaximumMotorTorque(1000);
        wj2 = new WheelJoint(carHull, wheel2, wheel2.getWorldCenter(), new Vector2(0,1));
        wj2.setFrequency(1000);
        wj2.setMaximumMotorTorque(1000);
    }

    public Car(Shape hullShape, double wheel1Size, double wheel2Size, boolean controlable){
        super(controlable);

        carHull = new GameObject();
        carHull.addFixture((Convex) hullShape);
        carHull.setMass(MassType.NORMAL);

        wheel1 = new GameObject();
        wheel1.addFixture(new Circle(wheel1Size));
        wheel1.setMass(MassType.NORMAL);
        wheel1.translate(1,-1.2);
        wheel1.getFixture(0).setFriction(5);

        wheel2 = new GameObject();
        wheel2.addFixture(new Circle(wheel2Size));
        wheel2.setMass(MassType.NORMAL);
        wheel2.translate(-1,-1.2);
        wheel2.getFixture(0).setFriction(5);

        wj1 = new WheelJoint(carHull, wheel1, wheel1.getWorldCenter(), new Vector2(0,1));
        wj1.setFrequency(10);
        wj1.setMaximumMotorTorque(50);
        wj2.setMotorSpeed(30);

        wj2 = new WheelJoint(carHull, wheel2, wheel2.getWorldCenter(), new Vector2(0,1));
        wj2.setFrequency(10);
        wj2.setMaximumMotorTorque(50);
        wj2.setMotorSpeed(30);
    }

    public GameObject getHull() {return carHull;}
    public GameObject getWheel1() {return wheel1;}
    public GameObject getWheel2() {return wheel2;}
    public WheelJoint getWj1() {return wj1;}
    public WheelJoint getWj2() {return wj2;}

    @Override
    public void addToWorld(GameWorld world) {
        world.addJoint(wj2);
        world.addJoint(wj1);
        world.addBody(wheel2);
        world.addBody(wheel1);
        world.addBody(carHull);
        world.addVehicle(this);
    }


    @Override
    public void applyControl(Set<Integer> keys) {

        if(keys.contains(KeyEvent.VK_D)){
            wj1.setMotorSpeed(5000);
            wj2.setMotorSpeed(5000);
            wj1.setMotorEnabled(true);
            wj2.setMotorEnabled(true);
        }
        if(keys.contains(KeyEvent.VK_A)) {
            wj1.setMotorSpeed(-5000);
            wj2.setMotorSpeed(-5000);
            wj1.setMotorEnabled(true);
            wj2.setMotorEnabled(true);
        }
        if(keys.isEmpty()){
            wj1.setMotorEnabled(false);
            wj2.setMotorEnabled(false);
        }
    }
}
