package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.InstrumentedRobot;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public interface ConnectableRobot {

    public void disconnectRobot(InstrumentedRobot lr);
    
    public void connectRobot(InstrumentedRobot lr);
    
}