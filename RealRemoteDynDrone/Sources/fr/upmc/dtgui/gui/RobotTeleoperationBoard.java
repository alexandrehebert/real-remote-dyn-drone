//	RobotTeleoperationBoard.java --- 

package fr.upmc.dtgui.gui;

import java.util.concurrent.BlockingQueue;
import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.RobotStateData;

/**
 * The interface <code>RobotTeleoperationBoard</code> defines the minimal set
 * of functionalities of robot teleoperation boards expected by the hosting
 * teleoperation stations.
 *
 * <p>Created on : 2011-10-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public interface		RobotTeleoperationBoard {

	/**
	 * The method <code>makeSensorDataReceptor</code> creates a thread to
	 * receive sensor data from the robot.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param positionDisplay	the display on which the robot is visualized.
	 * @param sensorDataQueue	the data queue used to receive robot positions.
	 * @param absoluteX			hosting station absolute position (x coordinate)
	 * @param absoluteY			hosting station absolute position (y coordinate)
	 * @param controlRadius		radius of the circle within which robot are
	 * 							teleoperable.
	 * @return					a thread to receive sensor data from the robot.
	 */
	public SensorDataReceptorInterface	makeSensorDataReceptor(
				PositionDisplay positionDisplay,
				BlockingQueue<RobotStateData> sensorDataQueue,
				int absoluteX,
				int absoluteY,
				int controlRadius
				) ;

	/**
	 * connect a robot to the current teleoperation board when the robot
	 * becomes teleoperable (within the control radius of the hosting station).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param lr	the robot to be connected.
	 */
	public void		connectRobot(InstrumentedRobot lr);

	/**
	 * disconnect a robot from the current teleoperation board when the robot
	 * is no longer teleoperable (not within the control radius of the hosting
	 * station).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param lr	the robot to be disconnected.
	 */
	public void		disconnectRobot(InstrumentedRobot lr);

	/**
	 * makes the board visible or invisible, when the robot is controllable and
	 * the operator selects it to act upon it or deselects it to select another
	 * robot.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param b		true is the robot has to become controllable.
	 */
	public void		setVisible(boolean b);

	/**
	 * check if a robot is currently connected to the board.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return		true if a robot is currently connected to the board.
	 */
	public boolean	isRobotConnected() ;

	/**
	 * process some sensory data coming from the robot.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param rsd
	 */
	public void		processSensorData(RobotStateData rsd) ;

}

// $Id$