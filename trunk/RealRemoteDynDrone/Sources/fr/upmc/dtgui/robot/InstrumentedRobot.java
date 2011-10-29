//	InstrumentedRobot.java --- 

package fr.upmc.dtgui.robot;

import java.util.concurrent.BlockingQueue;

public interface		InstrumentedRobot	extends Robot {

	BlockingQueue<RobotStateData>		getSensorDataQueue();
	BlockingQueue<RobotActuatorCommand>	getActuatorDataQueue();
	double	getX();
	double	getY();
	void	start();

}

// $Id$