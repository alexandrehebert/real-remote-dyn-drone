//	LittleRobot.java --- a little simulated robot

package fr.upmc.dtgui.example.robot;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.PositioningData;
import fr.upmc.dtgui.robot.RobotActuatorCommand;
import fr.upmc.dtgui.robot.RobotStateData;

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
 * <p>Robot sensors and actuators</p>
 * 
 * To allow for its control through teleoperation, the little robot has two
 * complementary threads: one to send sensor data and the other to receive
 * actuator data.  Both use a blocking queue to store outgoing sensor data and
 * incoming actuator commands.  The two blocking queues are shared with the
 * observer and controller processes (also Java Threads in fine).
 * 
 * The sensor thread sends data at a fixed rhythm, which is its sending rate.
 * The actuator thread wait for data, and when the data has been processed,
 * it imposes a minimal delay before getting more actuator data by sleeping
 * between authorized receptions.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	this.x >= 0 && this.x <= World.MAX_X
 * 				this.y >= 0 && this.y <= World.MAX_Y
 * 				this.speed >= 0.0 && this.speed <= 20.0
 * 				this.energyLevel >= 0 && this.energyLevel <= 100.0
 * 				this.direction >= 0.0 && this.direction < 360.0
 * 				this.sine == sine(this.direction)
 * 				this.cosine == cosine(this.direction)
 * 				(this.steeringAngle >= -15.0 && this.steeringAngle <= -0.1 ||
 * 				 this.steeringAngle == 0.0 ||
 * 				 this.steeringAngle >= 0.1 && this.steeringAngle <= 15.0)
 * </pre>
 * 
 * <p>Created on : 2011-09-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			LittleRobot		extends Thread
										implements InstrumentedRobot
{

	/** Maximum energy level in the robot.								*/
	protected static double		MAX_ENERGY_LEVEL = 100.0 ;	// percentage

	/** Minimal value for the steering angle in absolute value.			*/
	protected static double		STEERING_TOLERANCE = 0.1 ;	// degrees

	/** The legnth in meter between the two axles of the robot.			*/
	protected static double		WHEEL_BASE = 1.0 ;			// meters

	/** Autonomy of the robot at 10 m/s, in second.						*/
	protected static int		AUTONOMY_10 = 7200 ;		// 2 hours
	/** Autonomy of the robot at 20 m/s, in second.						*/
	protected static int		AUTONOMY_20 = 3600 ;		// 1 hour

	/** Unique name of the robot.										*/
	final protected String	robotName ;
	protected static LittleRobot	myself ;

	/** Position of the robot along the abcissa axis, in meters.		*/
	protected double	x = 2100.0 ;						// m
	/** Position of the robot along the ordinate axis, in meters.		*/
	protected double	y = 900.0 ;							// m
	/** Current speed in m/s, >= 0.0 and <= 20.0.						*/
	protected double	speed = 10.0 ;						// m/s
	/** Current energy level as a percentage of remaining energy.		*/
	protected double	energyLevel = 100.0 ;				// percentage
	/**
	 * Direction in degrees where 0 goes along the X axis and 90
	 * along the Y axis (so, count clockwise with 0 at 3 o'clock).
	 */
	protected double	direction = 45.0 ;					// degrees
	/** Sine of the direction angle.									*/
	protected double	sine = Math.sin(Math.toRadians(direction)) ;
	/** Cosine of the direction angle.									*/
	protected double	cosine = Math.cos(Math.toRadians(direction)) ;
	/**
	 * Steering angle of the robot, interpreted as a change in direction ;
	 * positive angles mean a left turn on the screen, while negative ones
	 * mean a right turn.
	 */
	protected double	steeringAngle = 0.0 ;				// degrees

	/**
	 * Thread responsible for receiving actuator commands and updating
	 * the robot state variables.
	 */
	final protected ActuatorDataReceptor	adr ;
	/** Thread responsible for sending the sensor data to requesters.	*/
	final protected SensorDataSender		sds ;

	/** Simulation time step in milliseconds */
	protected int		timeStep = 100 ;
	/** Current simulation  time in milliseconds */
	protected int		currentTime = 0 ;

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
	 *
	 * @param robotName	unique name of the robot.
	 * @param x			initial x coordinate.
	 * @param y			initial y coordinate.
	 * @param direction	initial direction.
	 */
	public					LittleRobot(
		String robotName,
		double x,
		double y,
		double direction
		)
	{
		super();
		LittleRobot.myself = this ;
		this.robotName = robotName ;
		this.setX(x) ;
		this.setY(y) ;
		this.setDirection(direction) ;
		this.adr = new ActuatorDataReceptor(this) ;
		this.sds = new SensorDataSender(this) ;
	}

	// ---------------------------------------------------------------------
	// *** Getters and setters ***
	// ---------------------------------------------------------------------

	/** @return the robotName.										*/
	public String				getRobotName() { return robotName; }

	/** @return the x coordinate.									*/
	public synchronized double	getX() { return x; }

	/** @param x the x to set										*/
	public synchronized void	setX(double x) { this.x = x; }

	/** @return the y coordinate.									*/
	public synchronized double	getY() { return y; }

	/** @param y the y to set										*/
	public synchronized void	setY(double y) { this.y = y; }

	/** @return the energyLevel										*/
	public synchronized double	getEnergyLevel() { return energyLevel; }

	/** @param energyLevel the energyLevel to set					*/
	public synchronized void	setEnergyLevel(double energyLevel) {
		this.energyLevel = energyLevel;
	}

	/** @return the speed											*/
	public synchronized double	getSpeed() { return speed; }

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
	public synchronized void	setSpeed(double speed) {
		this.speed = speed ;
	}

	/** @return the direction										*/
	public synchronized double	getDirection() { return direction; }

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
	public synchronized void	setDirection(double direction) {
		this.direction = direction % 360.0 ;
		this.sine = Math.sin(Math.toRadians(this.direction)) ;
		this.cosine = Math.cos(Math.toRadians(this.direction)) ;
	}

	/** @return the steeringAngle									*/
	public synchronized double	getSteeringAngle() {
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
	public synchronized void	setSteeringAngle(double steeringAngle) {
		if (Math.abs(steeringAngle) <= STEERING_TOLERANCE) {
			this.steeringAngle = 0.0 ;
		} else {
			this.steeringAngle = steeringAngle;
		}
	}

	/**
	 * Returns the object reference of the actuator command data queue, a
	 * blocking queue used to synchronize senders of commands with the robot
	 * thread receiving them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	@return != null
	 * </pre>
	 *
	 * @return
	 */
	public BlockingQueue<RobotActuatorCommand> getActuatorDataQueue() {
		return adr.getCommandQueue();
	}

	/**
	 * Returns the object reference of the sensor data queue, a blocking
	 * queue used to synchronize the sending of sensor data from the robot
	 * with the receivers.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	@return != null
	 * </pre>
	 *
	 * @return
	 */
	public BlockingQueue<RobotStateData> getSensorDataQueue() {
		return this.sds.getDataQueue() ;
	}

	// ---------------------------------------------------------------------
	// *** Sensory and actuator data ***
	// ---------------------------------------------------------------------

	/**
	 * Reads and package the positioning data into one object for transmission
	 * as sensory data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return
	 */
	public synchronized PositioningData	getPositioningData() {
		return new PositioningData(
						this.getX(), this.getY(), this.getDirection()) ;
	}

	/**
	 * Reads the energy level and put it into an object for transmission
	 * as sensory data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return
	 */
	public synchronized EnergyData		getEnergyData() {
		return new EnergyData(this.getEnergyLevel()) ;
	}

	/**
	 * Reads the speed and put it into an object for transmission
	 * as sensory data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return
	 */
	public synchronized SpeedData		getSpeedData() {
		return new SpeedData(this.getSpeed()) ;
	}

	/**
	 * Reads the steering angle and put it into an object for transmission
	 * as sensory data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return
	 */
	public synchronized SteeringData	getSteeringData() {
		return new SteeringData(this.getSteeringAngle()) ;
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
	public void					setSimulationParameters(int timeStep) {
		this.timeStep = timeStep ;
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
	protected double			energyConsumptionPerSecond() {
		double ret = 0.0 ;
		
		double c1 = 100.0 / (LittleRobot.AUTONOMY_10 * Math.sqrt(10.0)) ;
		double c2 =
			(((100.0 / LittleRobot.AUTONOMY_20) -
								(100.0 / LittleRobot.AUTONOMY_10))
			/ ((20.0 - 10.0) * (20.0 - 10.0))) ;

		if (this.speed <= 10.0) {
			ret = c1 * Math.sqrt(this.speed) ;
		} else {
			ret = c2 * (this.speed - 10.0) * (this.speed - 10.0)
										+ (100.0 / LittleRobot.AUTONOMY_10) ;
		}
		return ret ;
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
	protected double			energyConsumption(int duration) {
		return this.energyConsumptionPerSecond() * duration / 1000.0 ;
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
	public synchronized void	start() {
		this.adr.start() ;
		this.sds.start() ;
		this.currentTime = 0 ;
		super.start() ;
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
	public void					run() {
		double energyLevel = this.getEnergyLevel() ;
		double steeringAngle = this.getSteeringAngle() ;
		double speed = this.getSpeed() ;
		double x = this.getX() ;
		double y = this.getY() ;
		double direction = this.getDirection() ;
		
		try {
			while (energyLevel > 0.0) {
				steeringAngle = this.getSteeringAngle() ;
				speed = this.getSpeed() ;
				x = this.getX() ;
				y = this.getY() ;
				direction = this.getDirection() ;
				if (Math.abs(steeringAngle) < STEERING_TOLERANCE) {
					double distance = (speed * this.timeStep)/1000 ;
					this.setX(x + this.cosine * distance) ;
					this.setY(y + this.sine * distance) ;
				} else {
					// 1. Compute de curve radius and the rotation angle
					double curveRadius =
							WHEEL_BASE /
								Math.toRadians(Math.abs(steeringAngle)) ;
					double rotationSpeed = speed / curveRadius ;
					double rotationAngle =
						((rotationSpeed * this.timeStep)/1000.0)
															% (2.0 * Math.PI) ;
					// 2. Compute the curve center of rotation
					double xc ;		// x coordinate of the curve center
					double yc ;		// y coordinate of the curve center
					if (steeringAngle > 0.0) {
						xc = x + curveRadius * Math.cos(Math.toRadians(
														direction + 90.0)) ;
						yc = y + curveRadius * Math.sin(Math.toRadians(
														direction + 90.0)) ;
					} else {
						xc = x + curveRadius * Math.cos(Math.toRadians(
														direction - 90.0)) ;
						yc = y +curveRadius * Math.sin(Math.toRadians(
														direction - 90.0)) ;
					}
					// 3. Compute the initial position in cartesian coordinate
					//    in the curve center reference
					double xiprime ;	// x coordinate of the initial position
					double yiprime ;	// y coordinate of the initial position
					xiprime = x - xc ;
					yiprime = y - yc ;
					// 4. Compute the intial position in polar coordinate in
					//    the curve center reference
					@SuppressWarnings("unused")
					double rhoi ;		// radius of the initial position
					double thetai ;		// angle of the initial position
					rhoi = curveRadius ;
					if (xiprime >= 0.0 && yiprime >= 0.0) {
						thetai = Math.atan(yiprime/xiprime) ;
					} else if (xiprime >= 0.0 && yiprime < 0.0) {
						thetai = Math.atan(yiprime/xiprime) + 2.0 * Math.PI ;
					} else if (xiprime >= 0.0 && yiprime < 0.0) {
						thetai = Math.atan(yiprime/xiprime) + Math.PI ;
					} else {
						thetai = Math.atan(yiprime/xiprime) + Math.PI ;
					}
					// 5. Compute the final point in polar coordinate in the
					//    curve center reference
					double rhof ;	// radius of the final point
					double thetaf ;	// angle of the final point ;
					rhof = curveRadius ;
					if (this.steeringAngle > 0.0) {
						thetaf = (thetai + rotationAngle) % (2.0 * Math.PI) ;
					} else {
						thetaf = (thetai - rotationAngle + 2.0 * Math.PI)
															% (2.0 * Math.PI) ;
					}
					// 6. Compute the final point in cartesian coordinate in
					//    the curve center reference
					double xfprime ;	// x coordinate of the final point
					double yfprime ;	// y coordinate of the final point
					xfprime = rhof * Math.cos(thetaf) ;
					yfprime = rhof * Math.sin(thetaf) ;
					// 7. Upodate the current position to the final position
					//    in cartesian coordinate of the world
					this.setX(xfprime + xc) ;
					this.setY(yfprime + yc) ;
					// 8. Update the direction
					if (steeringAngle > 0.0) {
						this.setDirection(
							Math.toDegrees(thetaf + 0.5 * Math.PI) % 360.0) ;
					} else {
						this.setDirection(
							Math.toDegrees(thetaf - 0.5 * Math.PI) % 360.0) ;
					}
 				}
				energyLevel = energyLevel - this.energyConsumption(timeStep) ;
				this.setEnergyLevel(energyLevel) ;
				currentTime = currentTime + timeStep ;
				Thread.sleep(timeStep) ;
			}
		} catch(InterruptedException e) {
			return ;
		}
	}

	/**
	 * The class <code>SensorDataSender</code> implements the thread the robot
	 * uses to send its sensor data through a blocking queue used to synchronize
	 * itself with the receivers of the data.
	 * 
	 * All sensory data are represented by objects which classes are subclasses
	 * of RobotStateData.  At each sending, the blocking queue used as buffer to
	 * send the data to the receivers is cleared of old data if any, and then
	 * fresh data are put in:
	 * 
	 * <ul>
	 * <li>positioning data (x coordinate, y coordinate and current
	 *   direction</li>
	 * <li>energy level</li>
	 * <li>speed</li>
	 * <li>steering angle</li>
	 * </ul>
	 *
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2011-09-16</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	class SensorDataSender extends Thread {

		final protected BlockingQueue<RobotStateData>	dataQueue ;
		final protected LittleRobot						lr ;

		public						SensorDataSender(LittleRobot lr) {
			super();
			this.lr = lr;
			dataQueue = new ArrayBlockingQueue<RobotStateData>(4) ;
		}

		public BlockingQueue<RobotStateData> getDataQueue() {
			return dataQueue;
		}

		@Override
		public void					run() {
			while (true) {
				dataQueue.clear() ;
				dataQueue.add(lr.getPositioningData()) ;
				dataQueue.add(lr.getEnergyData()) ;
				dataQueue.add(lr.getSpeedData()) ;
				dataQueue.add(new SteeringData(lr.steeringAngle)) ;
				try {
					Thread.sleep(100) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public synchronized void	start() {
			this.dataQueue.clear() ;
			super.start();
		}
	}

	public class		EnergyData		extends RobotStateData {

		public double	level ;

		public			EnergyData(double level) {
			super();
			this.level = level;
		}

	}

	public class		SpeedData		extends RobotStateData {

		public double	speed ;

		public			SpeedData(double speed) {
			super();
			this.speed = speed;
		}

	}

	public class		SteeringData extends RobotStateData {

		public double		steeringAngle ;

		public SteeringData(double steeringAngle) {
			super();
			this.steeringAngle = steeringAngle;
		}

	}

	/**
 	 * The class <code>ActuatorDataReceptor</code> implements the thread the
 	 * robot uses to receive its actuator commands through a blocking queue
 	 * used to synchronize itself with the senders of the commands.
 	 * 
 	 * All of the actuator data are represented as objects which classes are
 	 * subclasses of the class RobotActuatorCommand.  The thread repeatedly
 	 * tries to take an item from the blocking queue used as a buffer for
 	 * incoming commands, and thus becomes blocked if no data is currently
 	 * available.  Currently two kinds of commands are recognized:
 	 *
 	 * <ul>
 	 * <li>speed changes</li>
 	 * <li>steering angle changes</li>
 	 * </ul>
 	 * 
 	 * <p><strong>Invariant</strong></p>
 	 * 
 	 * <pre>
 	 * invariant	true
 	 * </pre>
 	 * 
 	 * <p>Created on : 2011-09-16</p>
 	 * 
 	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 	 * @version	$Name$ -- $Revision$ -- $Date$
 	 */
	class				ActuatorDataReceptor extends Thread {

		final protected BlockingQueue<RobotActuatorCommand> commandQueue ;
		final protected LittleRobot lr ;

		public				ActuatorDataReceptor(LittleRobot lr) {
			super();
			this.lr = lr ;
			this.commandQueue =
				new ArrayBlockingQueue<RobotActuatorCommand>(1) ;
		}

		public BlockingQueue<RobotActuatorCommand> getCommandQueue() {
			return commandQueue;
		}

		@Override
		public synchronized void	start() {
			this.commandQueue.clear() ;
			super.start();
		}

		@Override
		public void					run() {
			RobotActuatorCommand rac = null ;
			while (true) {
				try {
					rac = this.commandQueue.take() ;
					rac.performOn(lr) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static RobotActuatorCommand makeSpeedChange(double newSpeed) {
		return LittleRobot.myself.new SpeedChange(newSpeed) ;
	}

	/**
	 * The class <code>SpeedChange</code> represents a command issued by
	 * a teleoperation station to change the current speed of the robot
	 * to a new value.
	 *
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	this.speed >= 0.0 && this.speed <= 20.0
	 * </pre>
	 * 
	 * <p>Created on : 2011-10-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	class				SpeedChange		extends RobotActuatorCommand {

		public double	speed ;

		/**
		 * <pre>
		 * pre	speed >= 0.0 && speed <= 20.0
		 * post	this.speed == speed
		 * </pre>
		 *
		 * @param speed	new speed for the robot.
		 */
		public			SpeedChange(double speed) {
			super();
			this.speed = speed;
		}

		@Override
		public void		performOn(InstrumentedRobot lr) {
			((LittleRobot)lr).setSpeed(this.speed) ;
		}

	}

	public static RobotActuatorCommand makeSteeringChange(
		double newSteeringAngle
		)
	{
		return LittleRobot.myself.new SteeringChange(newSteeringAngle) ;
	}

	/**
	 * The class <code>SteeringChange</code> represents a command issued by
	 * a teleoperation station to change the current steering angle of the
	 * robot to a new value.
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	(this.steeringAngle >= -15.0 && this.steeringAngle <= -0.1
	 * 				 || this.steeringAngle == 0.0 ||
	 * 				 this.steeringAngle >= 0.1 && this.steeringAngle <= 15.0)
	 * </pre>
	 * 
	 * <p>Created on : 2011-10-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	class				SteeringChange		extends RobotActuatorCommand {

		public double	steeringAngle ;

		public			SteeringChange(double steeringAngle) {
			super();
			this.steeringAngle = steeringAngle;
		}

		@Override
		public void		performOn(InstrumentedRobot lr) {
			((LittleRobot)lr).setSteeringAngle(this.steeringAngle) ;
		}
	}
}

// $Id$