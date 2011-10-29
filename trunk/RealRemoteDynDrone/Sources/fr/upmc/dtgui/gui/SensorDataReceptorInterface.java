//	SensorDataReceptorInterface.java --- 

package fr.upmc.dtgui.gui;

/**
 * The interface <code>SensorDataReceptorInterface</code> defines a common
 * supertype for all sensor data receptor threads  used by teleoperation
 * stations to receive the sensory data from robots.
 *
 * <p>Created on : 2011-10-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public interface SensorDataReceptorInterface {

	public void		start();
	public void		cutoff();
	public void		setTBoard(RobotTeleoperationBoard board);

}

// $Id$