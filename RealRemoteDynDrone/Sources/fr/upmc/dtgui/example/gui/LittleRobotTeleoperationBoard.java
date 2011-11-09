//	RobotTeleoperationBoard.java --- 
package fr.upmc.dtgui.example.gui;

import fr.upmc.r2d2.tools.MessageData;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.upmc.dtgui.example.robot.LittleRobot;
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
public class LittleRobotTeleoperationBoard
        extends JPanel
        implements RobotTeleoperationBoard {

    private static final long serialVersionUID = 1L;
    protected TeleoperationGUI tgui;
    protected EnergyPanel ecv;
    protected SpeedPanel sp;
    protected SteeringPanel stp;
    protected Robot lr;

    public LittleRobotTeleoperationBoard(
            TeleoperationGUI tgui,
            int size) {
        super();
        this.tgui = tgui;
        this.setSize(size, 250);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        this.ecv = new EnergyPanel();
        this.add(this.ecv);
        this.sp = new SpeedPanel();
        this.add(sp);
        this.stp = new SteeringPanel();
        this.add(stp);
        this.setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(1000, 250);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public void connectRobot(InstrumentedRobot lr) {
        this.lr = lr;
        this.sp.connectRobot(lr);
        this.stp.connectRobot(lr);
    }

    public void disconnectRobot(InstrumentedRobot lr) {
        this.sp.disconnectRobot(lr);
        this.stp.disconnectRobot(lr);
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
     * The class <code>EnergyPanel</code> defines the JPanel that shows to
     * the teleoperator the level of remaining energy in the robot.
     * 
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class EnergyPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected BoundedRangeModel energyModel;
        protected JPanel jpEnergySlider;
        protected JPanel jpEcvLabel;

        public EnergyPanel() {
            this.setSize(50, 250);
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            // JSlider to get nice labels
            this.energyModel = new DefaultBoundedRangeModel(0, 0, 0, 100);
            JSlider energySlider = new JSlider(energyModel);
            energySlider.setOrientation(JSlider.VERTICAL);
            energySlider.setMajorTickSpacing(20);
            energySlider.setMinorTickSpacing(5);
            energySlider.setPaintTicks(true);
            energySlider.setPaintLabels(true);
            jpEnergySlider = new JPanel();
            jpEnergySlider.add(energySlider);
            this.add(jpEnergySlider);

            // The label that says what information is diplayed
            JLabel ecvLabel = new JLabel("Remaining energy");
            jpEcvLabel = new JPanel();
            jpEcvLabel.setLayout(new BorderLayout());
            jpEcvLabel.add(ecvLabel, BorderLayout.NORTH);
            this.add(jpEcvLabel);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            this.setVisible(true);
        }

        @Override
        public void setVisible(boolean aFlag) {
            super.setVisible(aFlag);
            this.jpEnergySlider.setVisible(aFlag);
            this.jpEcvLabel.setVisible(aFlag);
        }

        public void updateEnergy(EnergyData ed) {
            this.energyModel.setValue((int) Math.round(ed.level));
        }
    }

    /**
     * The class <code>SpeedPanel</code> defines the JPanel used to show the
     * current speed of the robot to the teleoperator and the control panel
     * allowing to change this speed.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SpeedPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected SpeedDisplayPanel sdp;
        protected SpeedControllerPanel scp;
        protected InstrumentedRobot lr;

        public SpeedPanel() {
            this.setLayout(new BorderLayout());
            this.setSize(450, 250);
            this.sdp = new SpeedDisplayPanel();
            this.scp = new SpeedControllerPanel();
            this.add(sdp, BorderLayout.NORTH);
            this.add(scp, BorderLayout.SOUTH);
            this.setVisible(true);
        }

        public void disconnectRobot(InstrumentedRobot lr2) {
            this.scp.disconnectRobot(lr);
            this.lr = null;
        }

        public void connectRobot(InstrumentedRobot lr2) {
            this.lr = lr2;
            this.scp.connectRobot(lr2);
        }

        public void updateSpeed(SpeedData sd) {
            this.sdp.updateSpeed(sd);
        }
    }

    /**
     * The class <code>SpeedDisplayPanel</code> defines the JPanel showing the
     * current speed of the robot.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SpeedDisplayPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected BoundedRangeModel speedModel;
        protected JPanel jpProgressBar;
        protected JPanel speedLabelPanel;

        public SpeedDisplayPanel() {
            this.setLayout(new BorderLayout());
            this.setSize(450, 125);
            jpProgressBar = new JPanel();
            jpProgressBar.setLayout(new FlowLayout());
            this.speedModel = new DefaultBoundedRangeModel(0, 0, 0, 20);
            JSlider speedSlider = new JSlider(speedModel);
            speedSlider.setMajorTickSpacing(5);
            speedSlider.setMinorTickSpacing(1);
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);
            jpProgressBar.add(speedSlider);
            this.add(jpProgressBar, BorderLayout.NORTH);
            JLabel speedLabel = new JLabel("Current speed (m/s)");
            speedLabelPanel = new JPanel();
            speedLabelPanel.add(speedLabel);
            this.add(speedLabelPanel, BorderLayout.SOUTH);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            this.setVisible(true);
        }

        public void updateSpeed(SpeedData sd) {
            this.speedModel.setValue((int) Math.round(sd.speed));
        }
    }

    /**
     * The class <code>SpeedControllerPanel</code> defines the JPanel allowing
     * to change the current speed of the robot.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SpeedControllerPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected Robot lr;
        protected JPanel speedLabelPanel;
        protected JPanel speedSliderPanel;
        protected JSlider speedSlider;

        public SpeedControllerPanel() {
            this.setLayout(new BorderLayout());
            this.setSize(450, 125);
            JLabel speedLabel = new JLabel("Speed control (m/s)");
            speedLabelPanel = new JPanel();
            speedLabelPanel.add(speedLabel);
            this.add(speedLabelPanel, BorderLayout.SOUTH);
            DefaultBoundedRangeModel speedModel =
                    new DefaultBoundedRangeModel(0, 0, 0, 20);
            speedSlider = new JSlider(speedModel);
            speedSlider.setMajorTickSpacing(5);
            speedSlider.setMinorTickSpacing(1);
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);
            speedSliderPanel = new JPanel();
            speedSliderPanel.add(speedSlider);
            this.add(speedSliderPanel, BorderLayout.NORTH);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            this.setVisible(true);
        }

        public void disconnectRobot(InstrumentedRobot lr) {
            this.speedSlider.addChangeListener(null);
            this.lr = null;
        }

        public void connectRobot(InstrumentedRobot lr) {
            this.lr = lr;
            this.speedSlider.addChangeListener(
                    new SpeedActuatorDataListener(lr.getActuatorDataQueue()));
        }
    }

    /**
     * The class <code>SteeringPanel</code> defines the JPanel that shows the
     * current steering angle to the teleoperator and the controller to change
     * this angle.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SteeringPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected SteeringDisplayPanel sdp;
        protected SteeringControllerPanel scp;
        protected InstrumentedRobot lr;

        public SteeringPanel() {
            this.setLayout(new BorderLayout());
            this.setSize(450, 250);
            this.sdp = new SteeringDisplayPanel();
            this.scp = new SteeringControllerPanel();
            this.add(sdp, BorderLayout.NORTH);
            this.add(scp, BorderLayout.SOUTH);
            this.setVisible(true);
        }

        public void disconnectRobot(InstrumentedRobot lr2) {
            this.scp.disconnectRobot(lr2);
            this.lr = null;
        }

        public void connectRobot(InstrumentedRobot lr2) {
            this.lr = lr2;
            this.scp.connectRobot(lr2);
        }

        public void updateSteeringAngle(SteeringData sd) {
            this.sdp.updateSteeringAngle(sd);
        }
    }

    /**
     * The class <code>SteeringDisplayPanel</code> defines the JPanel that
     * displays the current steering angle.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SteeringDisplayPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected BoundedRangeModel steeringModel;
        protected JPanel jpProgressBar;
        protected JPanel speedLabelPanel;

        public SteeringDisplayPanel() {
            this.setLayout(new BorderLayout());
            this.setSize(450, 125);
            jpProgressBar = new JPanel();
            jpProgressBar.setLayout(new FlowLayout());
            this.steeringModel = new DefaultBoundedRangeModel(0, 0, -15, 15);
            JSlider steeringSlider = new JSlider(this.steeringModel);
            steeringSlider.setMajorTickSpacing(5);
            steeringSlider.setMinorTickSpacing(1);
            steeringSlider.setPaintTicks(true);
            steeringSlider.setPaintLabels(true);
            jpProgressBar.add(steeringSlider);
            this.add(jpProgressBar, BorderLayout.NORTH);
            JLabel steeringLabel = new JLabel("Current steering angle (degrees)");
            speedLabelPanel = new JPanel();
            speedLabelPanel.add(steeringLabel);
            this.add(speedLabelPanel, BorderLayout.SOUTH);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            this.setVisible(true);
        }

        public void updateSteeringAngle(SteeringData sd) {
            this.steeringModel.setValue((int) Math.round(sd.steeringAngle));
        }
    }

    /**
     * The class <code>SteeringControllerPanel</code> defines the JPanel
     * allowing to change the current steering angle.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SteeringControllerPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected InstrumentedRobot lr;
        protected JPanel steeringLabelPanel;
        protected JPanel steeringSliderPanel;
        protected JSlider steeringSlider;

        public SteeringControllerPanel() {
            this.setLayout(new BorderLayout());
            this.setSize(450, 125);
            JLabel steeringLabel = new JLabel("Steering angle control (degrees)");
            steeringLabelPanel = new JPanel();
            steeringLabelPanel.add(steeringLabel);
            this.add(steeringLabelPanel, BorderLayout.SOUTH);
            DefaultBoundedRangeModel steeringModel =
                    new DefaultBoundedRangeModel(0, 0, -15, 15);
            steeringSlider = new JSlider(steeringModel);
            steeringSlider.setMajorTickSpacing(5);
            steeringSlider.setMinorTickSpacing(1);
            steeringSlider.setPaintTicks(true);
            steeringSlider.setPaintLabels(true);
            steeringSliderPanel = new JPanel();
            steeringSliderPanel.add(steeringSlider);
            this.add(steeringSliderPanel, BorderLayout.NORTH);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            this.setVisible(true);
        }

        public void disconnectRobot(InstrumentedRobot lr2) {
            this.steeringSlider.addChangeListener(null);
            this.lr = null;
        }

        public void connectRobot(InstrumentedRobot lr) {
            this.lr = lr;
            this.steeringSlider.addChangeListener(
                    new SteeringActuatorDataListener(lr.getActuatorDataQueue()));
        }
    }

    /**
     * The class <code>SpeedActuatorDataListener</code> defines the listener
     * for speed change events coming from the teleoperator and transmit them
     * to the robot through its actuator data queue.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SpeedActuatorDataListener implements ChangeListener {

        final protected BlockingQueue<RobotActuatorCommand> commandQueue;

        public SpeedActuatorDataListener(
                BlockingQueue<RobotActuatorCommand> commandQueue) {
            super();
            this.commandQueue = commandQueue;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            int newSpeed = source.getValue();
            final RobotActuatorCommand sc =
                    LittleRobot.makeSpeedChange(newSpeed);
            (new ActuatorDataSender(sc, this.commandQueue)).start();
        }
    }

    /**
     * The class <code>SteeringActuatorDataListener</code> defines the listener
     * for steering angle change events coming from the teleoperator an
     * transmit them to the robot through its actuator data queue.
     *
     * <p>Created on : 2011-10-14</p>
     * 
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     * @version	$Name$ -- $Revision$ -- $Date$
     */
    class SteeringActuatorDataListener implements ChangeListener {

        final protected BlockingQueue<RobotActuatorCommand> commandQueue;

        public SteeringActuatorDataListener(
                BlockingQueue<RobotActuatorCommand> commandQueue) {
            super();
            this.commandQueue = commandQueue;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            double newSteeringAngle = source.getValue();
            final RobotActuatorCommand sc =
                    LittleRobot.makeSteeringChange(newSteeringAngle);
            (new ActuatorDataSender(sc, this.commandQueue)).start();
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
        protected LittleRobotTeleoperationBoard tBoard;
        final protected BlockingQueue<RobotStateData> dataQueue;
        protected int absoluteX;
        protected int absoluteY;
        protected int controlRadius;
        protected boolean shouldContinue;

        public SensorDataReceptor(
                PositionDisplay positionDisplay,
                BlockingQueue<RobotStateData> dataQueue,
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
            this.tBoard = (LittleRobotTeleoperationBoard) tBoard;
        }

        @Override
        public synchronized void start() {
            this.shouldContinue = true;
            super.start();
        }

        @Override
        public void run() {
            RobotStateData rsd = null;
            Vector<RobotStateData> current = new Vector<RobotStateData>(4);
            while (this.shouldContinue) {
                try {
                    rsd = this.dataQueue.take();		// wait if empty...
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
                                final RobotStateData rsd1 = rsd;
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

        protected RobotActuatorCommand rac;
        protected BlockingQueue<RobotActuatorCommand> commandQueue;

        public ActuatorDataSender(
                RobotActuatorCommand rac,
                BlockingQueue<RobotActuatorCommand> commandQueue) {
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