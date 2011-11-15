//	LittleFakeRobot.java --- 
package fr.upmc.dtgui.tests;

import java.lang.Thread;

import fr.upmc.dtgui.annotations.RealActuatorData;
import fr.upmc.dtgui.annotations.RealRange;
import fr.upmc.dtgui.annotations.RealSensorData;
import fr.upmc.dtgui.annotations.VariationType;
import fr.upmc.dtgui.annotations.MeasurementUnit;
import fr.upmc.dtgui.annotations.WithActuators;
import fr.upmc.dtgui.annotations.WithSensors;
import fr.upmc.dtgui.robot.Robot;

/**
 * The class <code>LittleRobot</code> implements a simple simulated robot
 * that evolves in a 2D environment.
 *
 * <p><strong>Description</strong></p>
 * 
 * The little robot simulates movements in a 2D virtual environment.  At all
 * time, the robot has a given position (x,y) and has a direction given in
 * degrees.  To simplify the display, position and direction are interpreted
 * in a referential coordinate system where the origin (0,0) is in the upper
 * left corner, while larger positive x are towards the right and larger
 * positive y are towards the down.  The measurement unit used in this virtual
 * environment is the meter, that is the scale is that 1 unit represents 1
 * meter.
 * 
 * The little robot moves at a certain speed, in meters per second.  The robot
 * is assumed to have a control over direction through a steering which has a
 * certain angle that changes the direction as long as it is maintained
 * different from 0, like the steering wheel of a car.  To avoid computational
 * errors, a minimal tolerance of 0.1 degrees is put on the steering angle, so
 * any angle between -0.1 and +0.1 degrees will be considered as 0.  
 * 
 * The little robot also simulates consumption of energy over time.  For
 * simplicity, energy is considered in percentage, going down from 100% to
 * 0%.
 * 
 * The simulation of the dynamics of the robot follows a simple thread loop
 * with a time step parameter.  At each time step, the position and the
 * direction are updated from the old position, the speed, the old direction
 * and the steering angle.  Hence, the simulated robot is a Thread (in the
 * sense of Java), repeatedly executing an update on its position,
 * direction and energy level, and then waiting for the next time step.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	this.speed >= 0.0 && this.speed <= 20.0
 * 				this.energyLevel >= 0 && this.energyLevel <= 100.0
 * 				this.direction >= 0.0 && this.direction < 360.0
 * 				this.sine == sine(this.direction)
 * 				this.cosine == cosine(this.direction)
 * 				(this.steeringAngle >= -15.0 && this.steeringAngle <= -0.1 ||
 * 				 this.steeringAngle == 0.0 ||
 * 				 this.steeringAngle >= 0.1 && this.steeringAngle <= 15.0)
 * </pre>
 * 
 * <p>Created on : 2011-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
@WithSensors
@WithActuators
public class FirstLittleRobot extends Thread
        implements Robot {

    /** Maximum energy level in the robot.								*/
    protected static double MAX_ENERGY_LEVEL = 100.0;	// percentage
    /** Minimal value for the steering angle in absolute value.			*/
    protected static double STEERING_TOLERANCE = 0.1;	// degrees
    /** The length in meter between the two axles of the robot.			*/
    protected static double WHEEL_BASE = 1.0;			// meters
    /** Autonomy of the robot at 10 m/s, in second.						*/
    protected static int AUTONOMY_10 = 7200;		// 2 hours
    /** Autonomy of the robot at 20 m/s, in second.						*/
    protected static int AUTONOMY_20 = 3600;		// 1 hour
    /** Unique name of the robot.										*/
    final protected String robotName;
    /** Position of the robot along the abscissa axis, in meters.		*/
    protected double x = 200.0;							// m
    /** Position of the robot along the ordinate axis, in meters.		*/
    protected double y = 100.0;							// m
    /** Current speed in m/s, >= 0.0 and <= 20.0.						*/
    protected double speed = 20.0;						// m/s
    /** Current energy level as a percentage of remaining energy.		*/
    protected double energyLevel = 100.0;				// percentage
    /**
     * Direction in degrees where 0 goes along the X axis and 90
     * along the Y axis (so, count clockwise with 0 at 3 o'clock).
     */
    protected double direction = 45.0;					// degrees
    /** Sine of the direction angle.									*/
    protected double sine = Math.sin(Math.toRadians(direction));
    /** Cosine of the direction angle.									*/
    protected double cosine = Math.cos(Math.toRadians(direction));
    /**
     * Steering angle of the robot, interpreted as a change in direction ;
     * positive angles mean a left turn on the screen, while negative ones
     * mean a right turn.
     */
    protected double steeringAngle = 0.0;				// degrees
    /** Simulation time step in milliseconds */
    protected int timeStep = 100;
    /** Current simulation  time in milliseconds */
    protected int currentTime = 0;

    /**
     * Main class constructor ; creates the two threads to send sensor
     * data and receive actuator commands.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	robotName != null
     * 		x >= 0 && x <= World.MAX_X
     * 		y >= 0 && y <= World.MAX_Y
     * 		direction >= 0 && direction < 360
     * post	this.sdr != null && !this.adr.isAlive()
     * 		this.sds != null && !this.sds.isAlive()
     * </pre>
     */
    public FirstLittleRobot(
            String robotName,
            double x,
            double y,
            double direction) {
        super();
        this.robotName = robotName;
        this.setX(x);
        this.setY(y);
        this.setDirection(direction);
    }

    // ---------------------------------------------------------------------
    // *** Getters and setters ***
    // ---------------------------------------------------------------------
    /** @return the robotName.										*/
    public String getRobotName() {
        return robotName;
    }

    /** @return the x												*/
    @RealSensorData(groupName = "position",
    unit =
    @MeasurementUnit(name = "m"),
    dataRange =
    @RealRange(inf = 0.0, sup = 1000.0),
    maxReadingRate = 10.0,
    minReadingRate = 0.0,
    variation = VariationType.RANDOM)
    public synchronized double getX() {
        return x;
    }

    /** @param x the x to set										*/
    public synchronized void setX(double x) {
        this.x = x;
    }

    /** @return the y												*/
    @RealSensorData(groupName = "position",
    unit =
    @MeasurementUnit(name = "m"),
    dataRange =
    @RealRange(inf = 0.0, sup = 1000.0),
    maxReadingRate = 10.0,
    minReadingRate = 0.0,
    variation = VariationType.RANDOM)
    public synchronized double getY() {
        return y;
    }

    /** @param y the y to set										*/
    public synchronized void setY(double y) {
        this.y = y;
    }

    /** @return the energyLevel										*/
    @RealSensorData(groupName = "energy",
    unit =
    @MeasurementUnit(name = "percentage"),
    dataRange =
    @RealRange(inf = 0.0, sup = 100.0),
    maxReadingRate = 10.0,
    minReadingRate = 0.0,
    variation = VariationType.DECREASING)
    public synchronized double getEnergyLevel() {
        return energyLevel;
    }

    /** @param energyLevel the energyLevel to set					*/
    public synchronized void setEnergyLevel(double energyLevel) {
        this.energyLevel = energyLevel;
    }

    /** @return the speed											*/
    @RealSensorData(groupName = "speed",
    unit =
    @MeasurementUnit(name = "mps"),
    dataRange =
    @RealRange(inf = 0.0, sup = 20.0),
    maxReadingRate = 10.0,
    minReadingRate = 0.0,
    variation = VariationType.RANDOM)
    public synchronized double getSpeed() {
        return speed;
    }

    /**
     * Update the speed of the robot, given in meter per second.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	speed >= 0.0 && speed <= 20.0
     * post	this.speed == speed
     * </pre>
     *
     * @param speed	robot speed in meter per second
     */
    @RealActuatorData(groupName = "speed",
    unit =
    @MeasurementUnit(name = "mps"),
    dataRange =
    @RealRange(inf = 0.0, sup = 20.0),
    maxWritingRate = 10.0,
    minWritingRate = 0.0)
    public synchronized void setSpeed(double speed) {
        this.speed = speed;
    }

    /** @return the direction										*/
    @RealSensorData(groupName = "position",
    unit =
    @MeasurementUnit(name = "degrees"),
    dataRange =
    @RealRange(inf = 0.0, sup = 360.0),
    maxReadingRate = 10.0,
    minReadingRate = 0.0,
    variation = VariationType.RANDOM)
    public synchronized double getDirection() {
        return direction;
    }

    /**
     * Update the direction of the robot with a new angle.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	direction >= 0.0
     * post direction >= 0.0 && direction < 360.0
     * 		this.sine == Math.sin(Math.toRadians(this.direction))
     * 		this.cosine == Math.cos(Math.toRadians(this.direction))
     * </pre>
     *
     * @param direction	angle in the plane towards which the robot is heading:
     * 					given in degrees, counterclockwise, 0 at 3 o'clock.
     */
    public synchronized void setDirection(double direction) {
        this.direction = direction % 360.0;
        this.sine = Math.sin(Math.toRadians(this.direction));
        this.cosine = Math.cos(Math.toRadians(this.direction));
    }

    /** @return the steeringAngle									*/
    @RealSensorData(groupName = "steering",
    unit =
    @MeasurementUnit(name = "degrees"),
    dataRange =
    @RealRange(inf = -15.0, sup = 15.0),
    maxReadingRate = 10.0,
    minReadingRate = 0.0,
    variation = VariationType.RANDOM)
    public synchronized double getSteeringAngle() {
        return steeringAngle;
    }

    /**
     * Update the steering angle controlling the change in direction of the
     * robot.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	steeringAngle >= -15.0 && steeringAngle <= 15.0
     * post	(this.steeringAngle >= -15.0 && this.steeringAngle <= -0.1 ||
     * 			 this.steeringAngle == 0.0 ||
     * 			 this.steeringAngle >= 0.1 && this.steeringAngle <= 15.0)
     * </pre>
     *
     * @param steeringAngle	in degrees, positive meaning a left turn, and
     * 						negative a right one.
     */
    @RealActuatorData(groupName = "steering",
    unit =
    @MeasurementUnit(name = "degrees"),
    dataRange =
    @RealRange(inf = -15.0, sup = 15.0),
    maxWritingRate = 10.0,
    minWritingRate = 0.0)
    public synchronized void setSteeringAngle(double steeringAngle) {
        if (Math.abs(steeringAngle) <= STEERING_TOLERANCE) {
            this.steeringAngle = 0.0;
        } else {
            this.steeringAngle = steeringAngle;
        }
    }

    // ---------------------------------------------------------------------
    // *** Simulation ***
    // ---------------------------------------------------------------------
    /**
     * Update the length of the time step used in the simulation of the robot.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	timeStep > 0
     * post	this.timeStep == timeStep
     * </pre>
     *
     * @param timeStep	simulation time step in milliseconds
     */
    public void setSimulationParameters(int timeStep) {
        this.timeStep = timeStep;
    }

    /**
     * Compute the energy consumption of the robot in percentage of the maximum
     * energy level per second.  The function is in Math.sqrt of the speed when
     * the speed is less than 10.0 m/s, while it grows with the square of the
     * speed when it is larger than 10.0 m/s.
     * 
     * The autonomy of the robot at 10.0 m/s and 20.0 m/s are given as static
     * variables of the class LittleRobot.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	this.speed >= 0.0 && this.speed <= 20.0
     * post	this.speed == 10.0 => @return == 100.0 / LittleRobot.AUTONOMY_10
     * 		this.speed == 20.0 => @return == 100.0 / LittleRobot.AUTONOMY_20
     * </pre>
     *
     * @return
     */
    protected double energyConsumptionPerSecond() {
        double ret = 0.0;

        double c1 = 100.0 / (FirstLittleRobot.AUTONOMY_10 * Math.sqrt(10.0));
        double c2 =
                (((100.0 / FirstLittleRobot.AUTONOMY_20)
                - (100.0 / FirstLittleRobot.AUTONOMY_10))
                / ((20.0 - 10.0) * (20.0 - 10.0)));

        if (this.speed <= 10.0) {
            ret = c1 * Math.sqrt(this.speed);
        } else {
            ret = c2 * (this.speed - 10.0) * (this.speed - 10.0)
                    + (100.0 / FirstLittleRobot.AUTONOMY_10);
        }
        return ret;
    }

    /**
     * Simulates the energy consumption of the robot for some simulation time
     * elapsed.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	duration >= 0.0
     * post	@return >= 0.0 && @return <= 100.0
     * </pre>
     *
     * @param duration	time elapsed in milliseconds.
     * @return			percentage of energy consumption for the elapsed time.
     */
    protected double energyConsumption(int duration) {
        return this.energyConsumptionPerSecond() * duration / 1000.0;
    }

    /**
     * Reset the current time to 0, starts the two threads emitting sensor data
     * and receiving actuator commands, and then starts this thread.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	true				// no more preconditions.
     * post	this.adr.isAlive()
     * 		this.sds.isAlive()
     * 		this.isAlive()
     * </pre>
     * 
     * @see java.lang.Thread#start()
     */
    @Override
    public synchronized void start() {
        this.currentTime = 0;
        super.start();
    }

    /**
     * Simulates the robot by repeatedly calculating its next position as long
     * as it has some remaining energy.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	this.adr.isAlive()
     * 		this.sds.isAlive()
     * post	this.energyLevel == 0.0
     * </pre>
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while (this.energyLevel > 0.0) {
                if (Math.abs(this.steeringAngle) < STEERING_TOLERANCE) {
                    double distance = (this.speed * timeStep) / 1000;
                    this.x = this.x + this.cosine * distance;
                    this.y = this.y + this.sine * distance;
                } else {
                    // 1. Compute de curve radius and the rotation angle
                    double curveRadius =
                            WHEEL_BASE
                            / Math.toRadians(Math.abs(this.steeringAngle));
                    double rotationSpeed = this.speed / curveRadius;
                    double rotationAngle =
                            ((rotationSpeed * this.timeStep) / 1000.0)
                            % (2.0 * Math.PI);
                    // 2. Compute the curve center of rotation
                    double xc;		// x coordinate of the curve center
                    double yc;		// y coordinate of the curve center
                    if (this.steeringAngle > 0.0) {
                        xc = this.x
                                + curveRadius * Math.cos(Math.toRadians(
                                this.direction + 90.0));
                        yc = this.y
                                + curveRadius * Math.sin(Math.toRadians(
                                this.direction + 90.0));
                    } else {
                        xc = this.x
                                + curveRadius * Math.cos(Math.toRadians(
                                this.direction - 90.0));
                        yc = this.y
                                + curveRadius * Math.sin(Math.toRadians(
                                this.direction - 90.0));
                    }
                    // 3. Compute the initial position in cartesian coordinate
                    //    in the curve center reference
                    double xiprime;	// x coordinate of the initial position
                    double yiprime;	// y coordinate of the initial position
                    xiprime = this.x - xc;
                    yiprime = this.y - yc;
                    // 4. Compute the intial position in polar coordinate in
                    //    the curve center reference
                    @SuppressWarnings("unused")
                    double rhoi;		// radius of the initial position
                    double thetai;		// angle of the initial position
                    rhoi = curveRadius;
                    if (xiprime >= 0.0 && yiprime >= 0.0) {
                        thetai = Math.atan(yiprime / xiprime);
                    } else if (xiprime >= 0.0 && yiprime < 0.0) {
                        thetai = Math.atan(yiprime / xiprime) + 2.0 * Math.PI;
                    } else if (xiprime >= 0.0 && yiprime < 0.0) {
                        thetai = Math.atan(yiprime / xiprime) + Math.PI;
                    } else {
                        thetai = Math.atan(yiprime / xiprime) + Math.PI;
                    }
                    // 5. Compute the final point in polar coordinate in the
                    //    curve center reference
                    double rhof;	// radius of the final point
                    double thetaf;	// angle of the final point ;
                    rhof = curveRadius;
                    if (this.steeringAngle > 0.0) {
                        thetaf = (thetai + rotationAngle) % (2.0 * Math.PI);
                    } else {
                        thetaf = (thetai - rotationAngle + 2.0 * Math.PI)
                                % (2.0 * Math.PI);
                    }
                    // 6. Compute the final point in cartesian coordinate in
                    //    the curve center reference
                    double xfprime;	// x coordinate of the final point
                    double yfprime;	// y coordinate of the final point
                    xfprime = rhof * Math.cos(thetaf);
                    yfprime = rhof * Math.sin(thetaf);
                    // 7. Upodate the current position to the final position
                    //    in cartesian coordinate of the world
                    this.x = xfprime + xc;
                    this.y = yfprime + yc;
                    // 8. Update the direction
                    if (this.steeringAngle > 0.0) {
                        this.setDirection(
                                Math.toDegrees(thetaf + 0.5 * Math.PI) % 360.0);
                    } else {
                        this.setDirection(
                                Math.toDegrees(thetaf - 0.5 * Math.PI) % 360.0);
                    }
                }
                energyLevel = energyLevel - this.energyConsumption(timeStep);
                currentTime = currentTime + timeStep;
                Thread.sleep(timeStep);
            }
        } catch (InterruptedException e) {
            return;
        }
    }
}

// $Id$