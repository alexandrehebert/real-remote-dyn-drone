//	TeleoperationGUI.java --- 

package fr.upmc.dtgui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.Robot;

/**
 * The class <code>TeleoperationGUI</code> is a JFrame representing the
 * overall graphical user interface of a control station for robots.  It
 * has a radar-like display to show the current position of the robot as
 * well as its current direction.  It also has a teleoperation panel,
 * including a panel for the current energy level of the robot, and
 * two panels for control: speed and steering angle.  The two control
 * panel are themselves composed of two subpanels, one for displaying the
 * current value and the other to allow for changing this value.  For
 * readability reasons (Swing does not provide good widgets to show a value
 * with a clear scale besides a slider bar), both of these panels use slider
 * bars.  In the subpanel displaying the current value, actions on the slider
 * bar are not activated.
 * 
 * The control station represented by this Teleoperation GUI is connected
 * to the robot through the reception of sensory data and the sending of
 * actuator commands.  Both of these are implemented as threads, one for
 * sensor data reception and the other for actuator command sending.  The
 * sensor reception thread is created once and for all and it interacts
 * with the Swing GUI by inserting tasks to be scheduled within the main
 * event processing thread.  Threads to send actuator commands are created
 * each time a new data must be sent; they schedule the sending also as a
 * task in the main event proecssing thread, wait for the execution of this
 * task and then simply die. 
 *
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	this.absoluteX >= 0 && this.absoluteX <= World.MAX_X
 * 				this.absoluteY >= 0 && this.absoluteY <= World.MAX_Y
 * 				this.relativeX > 0 && this.relativeX == sizeX/2
 *      		this.relativeY > 0 && this.relativeY == sizeY/2
 *      		this.controlRadius <= sizeX && this.controlRadius <= sizeY
 *     			this. sizeX <= World.MAX_X
 * 				this.sizeY <= World.MAX_Y
 * </pre>
 * 
 * <p>Created on : 2011-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public abstract class	TeleoperationGUI	extends JFrame
{

	private static final long		serialVersionUID = 1L;

	/**
	 * The upper part of the station window, featuring a radar-like display
	 * allowing to see the position of the different robots.
	 */
	protected PositionDisplay		positionDisplay ;
	/**
	 * The lower part of the station window, which will contain a set of
	 * buttons for choosing among the different robots the one that will be
	 * operated, and a blank space to add the teleoperation board specific
	 * to each robot.
	 */
	protected JPanel				lowerBoard ;
	/**
	 * A panel which will contain the buttons used to select the robot to
	 * be teleoperated.
	 */
	protected RobotSelector			robotSelector ;
	/**
	 * The robot which is currently selected to be teleoperated.
	 */
	protected InstrumentedRobot		currentlySelected ;

	/**
	 * Mapping from robots to their teleoperation board.
	 */
	protected Hashtable<Robot, RobotTeleoperationBoard>		boards ;
	/**
	 * Mapping from robots to their sensor data receptor.
	 */
	protected Hashtable<Robot,SensorDataReceptorInterface>	sensors ;

	/** X position of the station (center) in the world coordinates.	*/
	final protected int				absoluteX ;
	/** Y position of the station (center) in the world coordinates.	*/
	final protected int				absoluteY ;
	/** X position of the station (center) in the station coordinates.	*/
	final protected int				relativeX ;
	/** Y position of the station (center) in the station coordinates.	*/
	final protected int				relativeY ;
	/** Distance from the station under which robot can be controlled.	*/
	final protected int				controlRadius ;
	/** Size of the visibility area of the station in the X dimension.	*/
	final protected int				sizeX ;
	/** Size of the visibility area of the station in the Y dimension.	*/
	final protected int				sizeY ;

	/**
	 * Creation of teleoperation station graphical user interface (GUI). 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	absoluteX >= 0 && absoluteX <= World.MAX_X
	 * 		absoluteY >= 0 && absoluteY <= World.MAX_Y
	 * 		relativeX > 0 && relativeX == sizeX/2
	 *      relativeY > 0 && relativeY == sizeY/2
	 *      controlRadius <= sizeX && controlRadius <= sizeY
	 *      sizeX <= World.MAX_X
	 *      sizeY <= World.MAX_Y
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param absoluteX
	 * @param absoluteY
	 * @param relativeX
	 * @param relativeY
	 * @param controlRadius
	 * @param sizeX
	 * @param sizeY
	 * @throws HeadlessException
	 */
	public				TeleoperationGUI(
		String panelName,
		int absoluteX,
		int absoluteY,
		int relativeX,
		int relativeY,
		int controlRadius,
		int sizeX,
		int sizeY
		) throws HeadlessException
	{
		super("Teleoperation Panel " + panelName + " @ (" + absoluteX + ", "
				+ absoluteY + ")");
		this.setVisible(false) ;
		this.absoluteX = absoluteX;
		this.absoluteY = absoluteY;
		this.relativeX = relativeX ;
		this.relativeY = relativeY ;
		this.controlRadius = controlRadius ;
		this.sizeX = sizeX ;
		this.sizeY = sizeY ;
		this.setSize(this.sizeX, this.sizeY + 250) ;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		this.setLocationRelativeTo(null) ;

		this.positionDisplay =
			new PositionDisplay(Color.green, Color.BLACK, Color.LIGHT_GRAY,
								this.absoluteX, this.absoluteY,
								this.relativeX, this.relativeY,
								this.controlRadius, this.sizeX, this.sizeY) ;
		this.getContentPane().setLayout(new BorderLayout()) ;
		this.add(positionDisplay, BorderLayout.NORTH) ;
	
		this.lowerBoard = new JPanel() ;
		this.lowerBoard.setSize(sizeX, 250) ;
		this.lowerBoard.setLayout(
						new BoxLayout(this.lowerBoard, BoxLayout.X_AXIS)) ;
		this.robotSelector = new RobotSelector(this, 50, 250) ;
		this.lowerBoard.add(this.robotSelector, BorderLayout.EAST) ;
		this.lowerBoard.setBorder(
						BorderFactory.createLineBorder(Color.BLACK, 4)) ;
		this.lowerBoard.setVisible(true) ;
		this.add(this.lowerBoard, BorderLayout.SOUTH) ;

		this.boards = new Hashtable<Robot,RobotTeleoperationBoard>() ;
		this.sensors = new Hashtable<Robot,SensorDataReceptorInterface>() ;
	}

	/** @return the absoluteX										*/
	public int			getAbsoluteX()		{ return absoluteX; }

	/** @return the absoluteY										*/
	public int			getAbsoluteY()		{ return absoluteY; }

	/** @return the sizeX											*/
	public int			getSizeX()			{ return sizeX; }

	/** @return the sizeY											*/
	public int			getSizeY()			{ return sizeY; }

	/** @return the controlRadius									*/
	public int			getControlRadius()	{ return controlRadius; }

	/** @return the positionDisplay									*/
	public PositionDisplay getPositionDisplay() { return positionDisplay; }

	/**
	 * check whther the robot lr has already been detected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param lr	robot to be checked.
	 * @return		true if lr has already been detected.
	 */
	public boolean		detected(Robot lr) {
		return this.sensors.containsKey(lr) ;
	}

	/**
	 * check if the robot lr is currently controllable (to be controlled, it
	 * must also be selected by the teleoperator).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param lr	robot to be checked.
	 * @return		true if lr is currently controllable.
	 */
	public boolean		controllable(Robot lr) {
		boolean ret = false ;
		if (this.boards.containsKey(lr)) {
			ret = (this.boards.get(lr)).isRobotConnected() ;
		}
		return ret ;
	}

	public void			start() {
		this.setVisible(true) ;
	}

	/**
	 * makes a robot visible (but not yet controllable) from this teleoperation
	 * station when it enters its visibility area.  A sensor data receptor
	 * thread as well as a teleoperation board are created for this robot.
	 * The sensor data receptor is connected to the robot and to the position
	 * display and started to begin the reception of positioning data from the
	 * robot.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	this.detected(lr) && !this.controllable(lr)
	 * </pre>
	 *
	 * @param lr
	 */
	public void			detectRobot(InstrumentedRobot lr) {
		// if this robot was not already detected, then detect
		RobotTeleoperationBoard board = null ;
		SensorDataReceptorInterface sdr = null ;
		if (!this.detected(lr)) {
			board = this.createBoard(lr) ;
			sdr = this.createSensorDataReceptor(lr, board) ;
			this.sensors.put(lr, sdr) ;
			this.boards.put(lr, board) ;
			sdr.start() ;
			this.validate() ;
		}
	}

	/**
	 * create the teleoperation board fort the robot lr.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param lr	robot for which the teleoperation board must be created.
	 * @return		the teleoperation board for the robot lr.
	 */
	public abstract RobotTeleoperationBoard	createBoard(InstrumentedRobot lr) ;

	/**
	 * create the sensor data receptor thread for the robot lr and connects it
	 * to its teleoperation board.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param lr	the robot for which the thread must be created.
	 * @param board	the teleoperation board of the robot lr.
	 * @return
	 */
	public abstract SensorDataReceptorInterface createSensorDataReceptor(
			InstrumentedRobot lr,
			RobotTeleoperationBoard board
			) ;

	/**
	 * makes a robot no longer visible from this teleoperation station when it
	 * exits its visibility area.  The sensor data receptor thread for this
	 * robot is deleted.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.detected(lr)
	 * post	!this.detected(lr)
	 * </pre>
	 *
	 * @param lr
	 */
	public void			undetectRobot(InstrumentedRobot lr) {
		// if this robot was detected, then undetect
		if (this.detected(lr)) {
			SensorDataReceptorInterface sdr = this.sensors.remove(lr) ;
			sdr.cutoff() ;
			this.boards.remove(lr) ;
			this.validate() ;
		}
	}

	/**
	 * make a robot controllable by this control station when it enters its
	 * control area.  The button used to select this robot to control it is
	 * created and the teleoperation board associated to the robot is also
	 * connected to the sensor data receptor thread receiving the sensory
	 * data from the robot, as well as to the robot itself.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.detected(lr) && !this.controllable(lr)
	 * post	this.detected(lr) && this.controllable(lr)
	 * </pre>
	 *
	 * @param lr		the robot to be made controllable.
	 */
	public void			makeControllable(InstrumentedRobot lr) {
		if (this.detected(lr) && !this.controllable(lr)) {
			this.robotSelector.registerRobot(lr) ;
			RobotTeleoperationBoard board = this.boards.get(lr) ;
			SensorDataReceptorInterface sdr = this.sensors.get(lr) ;
			sdr.setTBoard(board) ;
			this.lowerBoard.add((Component)board, BorderLayout.WEST) ;
			board.connectRobot(lr) ;
			this.validate() ;
		}
	}

	/**
	 * make a robot no longer controllable (but still visible) by this control
	 * station when it exits its control area.  The button used to select this
	 * robot to control it is deleted and the teleoperation board associated
	 * to the robot is also deleted.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.detected && this.controllable(lr)
	 * post	this.detected && !this.controllable(lr) &&
	 * 			lr != this.currentlySelected
	 * </pre>
	 *
	 * @param lr		the robot to make no longer controllable.
	 */
	public void			makeUncontrollable(InstrumentedRobot lr) {
		if (this.controllable(lr)) {
			this.robotSelector.unregisterRobot(lr) ;
			SensorDataReceptorInterface sdr = this.sensors.get(lr) ;
			sdr.setTBoard(null) ;
			RobotTeleoperationBoard board = this.boards.get(lr) ;
			board.setVisible(false) ;
			board.disconnectRobot(lr) ;
			this.lowerBoard.remove((Component)board) ;
			if (lr == this.currentlySelected) {
				this.currentlySelected = null ;
			}
			this.validate() ;
		}
	}

	/**
	 * Make a robot the currently controlled robot through this teleoperation
	 * station.  The teleoperation board associated to the robot becomes the
	 * one visible in the lower panel of the station.  The teleoperation board
	 * of the currently controlled robot prior to this new selection is made
	 * invisible, but kept for further selection in the future.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.detected(lr) && this.controllable(lr)
	 * post	this.detected(lr) && this.controlled(lr) &&
	 * 			lr == this.currentlySelected
	 * </pre>
	 *
	 * @param lr	the robot to be selected.
	 */
	public void			selectRobot(InstrumentedRobot lr) {
		RobotTeleoperationBoard board ;

		if (lr != this.currentlySelected) {
			if (this.currentlySelected != null) {
				board = this.boards.get(this.currentlySelected) ;
				if (board != null) {
					board.setVisible(false) ;
				}
			}
			board = this.boards.get(lr) ;
			board.setVisible(true) ;
			this.currentlySelected = lr ;
			this.validate() ;
		}
	}
}

// $Id$