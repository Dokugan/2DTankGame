package dyn4j_game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;


public class TankGame extends JFrame {
    /** The scale 45 pixels per meter */
    public static final double SCALE = 45.0;

    /** The conversion factor from nano to base */
    public static final double NANO_TO_BASE = 1.0e9;

    /**
     * Custom Body class to add drawing functionality.
     */

    /** The canvas to draw to */
    protected Canvas canvas;

    /** The dynamics engine */
    protected GameWorld world;

    /** Wether the example is stopped or not */
    protected boolean stopped;

    /** The time stamp for the last iteration */
    protected long last;

    public static Set<Vehicle> vehicles = new HashSet<>();
    public static Set<Integer> keys = new HashSet<>();

    /**
     * Default constructor for the window
     */
    Dimension size;

    KeyEvent keyPressed;

    public TankGame() {
        super("TankGame Without Tanks");
        // setup the JFrame
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // add a window listener
        this.addWindowListener(new WindowAdapter() {
            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            @Override
            public void windowClosing(WindowEvent e) {
                // before we stop the JVM stop the example
                stop();
                super.windowClosing(e);
            }
        });

        // create the size of the window
        size = Toolkit.getDefaultToolkit().getScreenSize();
        //this.setExtendedState(MAXIMIZED_BOTH);

        // create a canvas to paint to
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(size);
        this.canvas.setMinimumSize(size);
        this.canvas.setMaximumSize(size);

        // add the canvas to the JFrame
        this.add(this.canvas);
        this.canvas.setFocusable(false);

        // make the JFrame not resizable
        // (this way I dont have to worry about resize events)
        this.setResizable(false);

        // size everything
        this.pack();

        if (!this.isFocusable())
            this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keys.remove(e.getKeyCode());
            }
        });

        // make sure we are not stopped
        this.stopped = false;

        // setup the world
        this.initializeWorld();

    }

    /**
     * Creates game objects and adds them to the world.
     * <p>
     * Basically the same shapes from the Shapes test in
     * the TestBed.
     */
    protected void initializeWorld() {
        // create the world
        this.world = new GameWorld();
        world.setGravity(new Vector2(0,-50));

        // create all your bodies/joints

        // create the floor
        Rectangle floorRect = new Rectangle(15.0, 1.0);
        GameObject floor = new GameObject();
        ArrayList<Polygon> floorParts = createTerrain(size.getWidth(), size.getHeight(), size.getHeight()/4, 0.5f);
        for(Polygon floorPart : floorParts){
            floor.addFixture(new BodyFixture(floorPart));
        }
        floor.setMass(MassType.INFINITE);
        // move the floor down a bit
        floor.translate(0, -(size.getHeight()/2)/SCALE);
        //floor.rotate(Math.toRadians(-5));
        this.world.addBody(floor);

        Car car = new Car(true);
        //car.setMassType(MassType.NORMAL);
        //car.applyForce(new Vector2(-1000,0));
        double spawnHight = 0;
        for(Vector2 v: floorParts.get(0).getVertices()){
            if(v.y > spawnHight) spawnHight = v.y;
        }

        car.getHull().translate(new Vector2(10, spawnHight/2));
        car.getWheel1().translate(new Vector2(10, spawnHight/2));
        car.getWheel2().translate(new Vector2(10, spawnHight/2));
        car.addToWorld(this.world);

    }

    public ArrayList<Polygon> createTerrain(double width, double height, double displace, float roughness){

        int power = (int)Math.pow(2, Math.ceil(Math.log(width/2) / (Math.log(2))));
        double points[] = new double[power+1];


        points[0] = height/2 + (Math.random()*displace*2) - displace;
        points[power] = height/2 + (Math.random()*displace*2) - displace;
        displace *= roughness;

        for(int i = 1; i < power; i *=2){
            for (int j = (power/i)/2; j < power; j+= power/i){
                points[j] = ((points[j - (power / i) / 2] + points[j + (power / i) / 2]) / 2);
                points[j] += (Math.random()*displace*2) - displace;
            }
            displace *= roughness;
        }

        for (int i = 0; i < points.length; i++){
            points[i] = points[i] / 45;
        }

        ArrayList<Polygon> polys = new ArrayList<>();
        for(int i = 0; i < points.length -1; i++){
            List<Vector2> listv2 = new ArrayList<>();
            listv2.add(new Vector2(i,0));
            listv2.add(new Vector2(i,points[i]));
            listv2.add(new Vector2(i +1,points[i+1]));
            listv2.add(new Vector2(i +1,0));

            Collections.reverse(listv2);

            try {
                polys.add(Geometry.createPolygon(listv2.toArray(new Vector2[listv2.size()])));
            } catch (Exception e) {
                createTerrain(width, height, displace, roughness);
            }
        }

        return polys;
    }

    /**
     * Start active rendering the example.
     * <p>
     * This should be called after the JFrame has been shown.
     */
    public void start() {
        // initialize the last update time
        this.last = System.nanoTime();
        // don't allow AWT to paint the canvas since we are
        this.canvas.setIgnoreRepaint(true);
        // enable double buffering (the JFrame has to be
        // visible before this can be done)
        this.canvas.createBufferStrategy(2);
        // run a separate thread to do active rendering
        // because we don't want to do it on the EDT
        Thread thread = new Thread() {
            public void run() {
                while (!isStopped()) {
                    gameLoop();
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        // set the game loop thread to a daemon thread so that
        // it cannot stop the JVM from exiting
        thread.setDaemon(true);
        // start the game loop
        thread.start();
    }

    /**
     * The method calling the necessary methods to update
     * the game, graphics, and poll for input.
     */
    double scrollPos;

    protected void gameLoop() {



        //apply controls
        for(Vehicle v : world.vehicles){
            v.applyControl(keys);
        }

        // get the graphics object to render to
        Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();

        // before we render everything im going to flip the y axis and move the
        // origin to the center (instead of it being in the top left corner)
        AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
        AffineTransform move = AffineTransform.getTranslateInstance(0, -size.height/2);
        g.transform(yFlip);
        g.transform(move);

        AffineTransform scroll = AffineTransform.getTranslateInstance(scrollPos,0);
        g.transform(scroll);

//
//                g.transform(scroll);
        for (Vehicle v : world.vehicles){

            System.out.println(v.getWorldCenter().x * SCALE);
            if(v.getHull().getWorldCenter().x * SCALE > size.getWidth()/2){

                scrollPos = scrollPos - v.getHull().getChangeInPosition().x * SCALE;
            }
        }

        // render anything about the Example (will render the World objects)
        this.render(g);

        // dispose of the graphics object
        g.dispose();

        // blit/flip the buffer
        BufferStrategy strategy = this.canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }

        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();

        // update the World

        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - this.last;
        // set the last time
        this.last = time;
        // convert from nanoseconds to seconds
        double elapsedTime = diff / NANO_TO_BASE;
        // update the world with the elapsed time
        this.world.update(elapsedTime);
    }

    /**
     * Renders the example.
     * @param g the graphics object to render to
     */
    protected void render(Graphics2D g) {
        // lets draw over everything with a white background
        g.setColor(Color.WHITE);
        g.fillRect(0, -size.height/2, size.width - (int)scrollPos, size.height);

        // lets move the view up some
        g.translate(0.0, -1.0 * SCALE);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);

        // draw all the objects in the world
        for (int i = 0; i < this.world.getBodyCount(); i++) {
            // get the object
            GameObject go = (GameObject) this.world.getBody(i);
            // draw the object
            go.render(g);
        }
    }

    public void addVehicle(Vehicle v){vehicles.add(v);}

    /**
     * Stops the example.
     */
    public synchronized void stop() {
        this.stopped = true;
    }

    /**
     * Returns true if the example is stopped.
     * @return boolean true if stopped
     */
    public synchronized boolean isStopped() {
        return this.stopped;
    }

    /**
     * Entry point for the example application.
     * @param args command line arguments
     */
    public static TankGame window;
    public static void main(String[] args) {
        // set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // create the example JFrame
        TankGame window = new TankGame();

        // show it
        window.setVisible(true);

        // start it
        window.start();
    }
}