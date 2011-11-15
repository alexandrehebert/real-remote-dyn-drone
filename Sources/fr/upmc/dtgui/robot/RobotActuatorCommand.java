//	RobotActuatorCommand.java --- 

package fr.upmc.dtgui.robot;

/**
 * The class <code>RobotActuatorCommand</code> is meant to be the common
 * supertype of all classes of actuator data to be exchanged between the
 * teleoperation station and the robot.
 *
 * <p>Created on : 2011-10-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public abstract class	RobotActuatorCommand {

	public abstract void	performOn(InstrumentedRobot lr) ;

}

// $Id$