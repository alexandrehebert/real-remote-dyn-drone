//	RobotTeleoperationBoard.java --- 
package fr.upmc.r2d2.tools;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.upmc.dtgui.example.robot.LittleRobot.EnergyData;
import fr.upmc.dtgui.example.robot.LittleRobot.SpeedData;
import fr.upmc.dtgui.example.robot.LittleRobot.SteeringData;
import fr.upmc.dtgui.gui.ActuatorDataSenderInterface;
import fr.upmc.dtgui.gui.PositionDisplay;
import fr.upmc.dtgui.gui.RobotTeleoperationBoard;
import fr.upmc.dtgui.gui.SensorDataReceptorInterface;
import fr.upmc.dtgui.gui.TeleoperationGUI;
import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.PositioningData;
import fr.upmc.dtgui.robot.Robot;
import fr.upmc.dtgui.robot.RobotActuatorCommand;
import fr.upmc.dtgui.robot.RobotStateData;

/**
 * The class <code>RobotTeleoperationBoard</code> implements a teleoperation
 * panel for a little robot.  It is separated into three areas: an energy level
 * display, a speed control display and a steering control display.  The two
 * control panel are themselves composed of two subpanels, one for displaying
 * the current value and the other to allow for changing this value.  For
 * readability reasons (Swing does not provide good widgets to show a value
 * with a clear scale besides a slider bar), both of these panels use slider
 * bars.  In the subpanel displaying the current value, actions on the slider
 * bar are not activated.
 *
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2011-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public abstract class AbstractTeleoperationBoard
        extends JPanel
        implements RobotTeleoperationBoard {

    private static final long serialVersionUID = 1L;
    protected TeleoperationGUI tgui;
    protected Robot lr;
    protected AbstractPanel[] panels;
    protected int nbSensors = 1;

    public AbstractTeleoperationBoard(
            TeleoperationGUI tgui,
            int size) {
        super();
        this.tgui = tgui;
        this.setSize(size, 250);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        for (AbstractPanel jp : (panels = getPanels()))
            this.add(jp);
        this.setVisible(false);
    }
    
    public abstract AbstractPanel[] getPanels();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(1000, 250);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    @Override
    public void connectRobot(InstrumentedRobot lr) {
        this.lr = lr;
        for (AbstractPanel jp : panels)
            jp.connectRobot(lr);
    }

    @Override
    public void disconnectRobot(InstrumentedRobot lr) {
        for (AbstractPanel jp : panels)
            jp.disconnectRobot(lr);
        this.lr = null;
    }

    @Override
    public boolean isRobotConnected() {
        return this.lr != null;
    }

    public void updateEnergy(EnergyData ed) {
        this.ecv.updateEnergy(ed);
    }

    public void updateSpeed(SpeedData sd) {
        this.sp.updateSpeed(sd);
    }

    public void updateSteeringAngle(SteeringData sd) {
        this.stp.updateSteeringAngle(sd);
    }

    /**
     * process incoming sensor data.
     * 
     * @see fr.upmc.dtgui.gui.RobotTeleoperationBoard#processSensorData(fr.upmc.dtgui.robot.RobotStateData)
     */
    public void processSensorData(RobotStateData rsd) {
        // Series of instanceof tests are particularly non-object-oriented,
        // but here we do not want to expose in advance the processing
        // methods defined above because the code of this teleoperation board
        // is not supposed to be known at the time the code on the robot side
        // that sends these sensory data is generated.
        if (rsd instanceof PositioningData) {
            this.tgui.getPositionDisplay().draw((PositioningData) rsd);
        } else if (rsd instanceof EnergyData) {
            this.updateEnergy((EnergyData) rsd);
        } else if (rsd instanceof SpeedData) {
            this.updateSpeed((SpeedData) rsd);
        } else if (rsd instanceof SteeringData) {
            this.updateSteeringAngle((SteeringData) rsd);
        }
    }

    /**
     * The factory method to create the sensor data receptor thread for this
     * teleoperation board.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	absoluteX >= 0 && absoluteX <= World.MAX_X
     * 		absoluteY >= 0 && absoluteY <= World.MAX_Y
     * post	true				// no more postconditions.
     * </pre>
     * 
     * @see fr.upmc.dtgui.gui.RobotTeleoperationBoard#makeSensorDataReceptor(fr.upmc.dtgui.gui.PositionDisplay, java.util.concurrent.BlockingQueue, int, int, int)
     */
    public SensorDataReceptorInterface makeSensorDataReceptor(
            PositionDisplay positionDisplay,
            BlockingQueue<RobotStateData> dataQueue,
            int absoluteX,
            int absoluteY,
            int controlRadius) {
        return new SensorDataReceptor(
                positionDisplay, dataQueue, absoluteX, absoluteY, controlRadius);
    }

    /**
     * The class <code>SensorDataReceptor</code> implements threads used to
     * receive sensor data from the controlled robot through a robot state data
     * blocking queue.  The thread repeatedly looks into the blocking queue for
     * new data, waiting if no data is available, and schedules into the main
     * processing thread of the GUI the updates to the different data that has
     * to be displayed.
     *
     * <p><strong>Invariant</strong></p>
     * 
     * <pre>
     * invariant	true
     * </pre>
     * 
     * <p>Created on : 2011-09-19</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SensorDataReceptor extends Thread
            implements SensorDataReceptorInterface {

        final protected PositionDisplay positionDisplay;
        protected AbstractTeleoperationBoard tBoard;
        final protected BlockingQueue dataQueue;
        protected int absoluteX;
        protected int absoluteY;
        protected int controlRadius;
        protected boolean shouldContinue;

        public SensorDataReceptor(
                PositionDisplay positionDisplay,
                BlockingQueue dataQueue,
                int absoluteX,
                int absoluteY,
                int controlRadius) {
            super();
            this.positionDisplay = positionDisplay;
            this.dataQueue = dataQueue;
            this.absoluteX = absoluteX;
            this.absoluteY = absoluteY;
            this.controlRadius = controlRadius;
        }

        public synchronized void cutoff() {
            this.shouldContinue = false;
        }

        public synchronized void setTBoard(
                RobotTeleoperationBoard tBoard) {
            this.tBoard = (AbstractTeleoperationBoard) tBoard;
        }

        @Override
        public synchronized void start() {
            this.shouldContinue = true;
            super.start();
        }

        @Override
        public void run() {
            if (nbSensors == 0) return;
            
            MessageData rsd = null;
            Vector<MessageData> current = new Vector(nbSensors);
            
            while (this.shouldContinue) {
                try {
                    rsd = (MessageData) this.dataQueue.take();		// wait if empty...
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                current.add(rsd);
                int n = this.dataQueue.drainTo(current);	// do not wait...
                for (int i = 0; i <= n; i++) {
                    rsd = current.elementAt(i);
                    try {
                        if (rsd instanceof PositioningData) {
                            final PositioningData pd = (PositioningData) rsd;
                            SwingUtilities.invokeAndWait(
                                    new Runnable() {

                                        public void run() {
                                            positionDisplay.draw(pd);
                                        }
                                    });
                        } else {
                            if (this.tBoard != null) {
                                final MessageData rsd1 = rsd;
                                SwingUtilities.invokeAndWait(
                                        new Runnable() {

                                            public void run() {
                                                if (tBoard != null) {
                                                    tBoard.processSensorData(rsd1);
                                                }
                                            }
                                        });
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                current.clear();
            }
        }
    }

    /**
     * The class <code>ActuatorDataSender</code> implements threads used to send
     * actuator commands to the controlled robot through its actuator command
     * blocking queue.  The thread schedules the addition of the new command into
     * the blocking queue as a task in the Swing event processing loop, waits for
     * the execution of this task and then dies.  Each time a new command is
     * inserted in the blocking queue, the queue is cleared of any unprocessed
     * data to always favor the freshness of data rather than the whole history
     * of data sent.
     *
     * <p><strong>Invariant</strong></p>
     * 
     * <pre>
     * invariant	true
     * </pre>
     * 
     * <p>Created on : 2011-09-19</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class ActuatorDataSender extends Thread
            implements ActuatorDataSenderInterface {

        protected MessageData rac;
        protected BlockingQueue commandQueue;

        public ActuatorDataSender(
                MessageData rac,
                BlockingQueue commandQueue) {
            super();
            this.rac = rac;
            this.commandQueue = commandQueue;
        }

        @Override
        public void run() {
            try {
                SwingUtilities.invokeAndWait(
                        new Runnable() {

                            public void run() {
                                commandQueue.clear();
                                commandQueue.add(rac);
                            }
                        });
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }
        }
    }
    
}

// $Id$