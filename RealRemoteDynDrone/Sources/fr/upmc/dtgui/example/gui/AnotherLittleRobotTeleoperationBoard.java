//	AnotherLittleRobotTeleoperationBoard.java --- 

package fr.upmc.dtgui.example.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
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

import fr.upmc.dtgui.example.robot.AnotherLittleRobot;
import fr.upmc.dtgui.example.robot.AnotherLittleRobot.SteeringData;
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
 * The class <code>AnotherLittleRobotTeleoperationBoard</code> defines a
 * teleoperation board for robot instances of the class
 * <code>AnotherLittleRobot</code>.  It is similar to the one defined by the
 * class <code>LittleRobotTeleoperationBoard</code> but with only steering
 * angle display and control slider bars.
 *
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2011-10-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class		AnotherLittleRobotTeleoperationBoard
										extends JPanel
										implements RobotTeleoperationBoard
{

	private static final long	serialVersionUID = 1L ;

	protected TeleoperationGUI	tgui ;
	protected SteeringPanel		stp ;
	protected Robot				lr ;

	public			AnotherLittleRobotTeleoperationBoard(
		TeleoperationGUI tgui,
		int size
		)
	{
		super() ;
		this.tgui = tgui ;
		this.setSize(size, 250) ;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) ;
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)) ;
		this.stp = new SteeringPanel() ;
		this.add(stp) ;
		this.setVisible(false) ;
	}

	@Override
	protected void	paintComponent(Graphics g) {
		super.paintComponent(g) ;
		this.setSize(1000, 250) ;
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)) ;
	}

	public void		connectRobot(InstrumentedRobot lr2) {
		this.lr = lr2 ;
		this.stp.connectRobot(lr2) ;
	}

	public void		disconnectRobot(InstrumentedRobot lr2) {
		this.stp.disconnectRobot(lr2) ;
		this.lr = null ;
	}

	@Override
	public boolean isRobotConnected() {
		return this.lr != null ;
	}

	public void		updateSteeringAngle(SteeringData sd) {
		this.stp.updateSteeringAngle(sd) ;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.dtgui.gui.RobotTeleoperationBoard#processSensorData(fr.upmc.dtgui.robot.RobotStateData)
	 */
	@Override
	public void processSensorData(RobotStateData rsd) {
		if (rsd instanceof PositioningData) {
			this.tgui.getPositionDisplay().draw((PositioningData) rsd) ;
		} else if (rsd instanceof SteeringData) {
			this.updateSteeringAngle((SteeringData) rsd) ;
		}
	}

	class				SteeringPanel extends JPanel {

		private static final long			serialVersionUID = 1L;
		protected SteeringDisplayPanel		sdp ;
		protected SteeringControllerPanel	scp ;
		protected InstrumentedRobot			lr ;

		public			SteeringPanel() {
			this.setLayout(new BorderLayout()) ;
			this.setSize(450, 250) ;
			this.sdp = new SteeringDisplayPanel() ;
			this.scp = new SteeringControllerPanel() ;
			this.add(sdp, BorderLayout.NORTH) ;
			this.add(scp, BorderLayout.SOUTH) ;
			this.setVisible(true) ;
		}

		public void		disconnectRobot(InstrumentedRobot lr2) {
			this.scp.disconnectRobot(lr2) ;
			this.lr = null ;
		}

		public void		connectRobot(InstrumentedRobot lr2) {
			this.lr = lr2 ;
			this.scp.connectRobot(lr2) ;
		}

		public void		updateSteeringAngle(SteeringData sd) {
			this.sdp.updateSteeringAngle(sd) ;
		}

	}

	class				SteeringDisplayPanel extends JPanel {

		private static final long	serialVersionUID = 1L;
		protected BoundedRangeModel steeringModel ;
		protected JPanel			jpProgressBar ;
		protected JPanel			speedLabelPanel ;

		public			SteeringDisplayPanel() {
			this.setLayout(new BorderLayout()) ;
			this.setSize(450, 125) ;
			jpProgressBar = new JPanel() ;
			jpProgressBar.setLayout(new FlowLayout()) ;
			this.steeringModel = new DefaultBoundedRangeModel(0, 0, -15, 15) ;
			JSlider steeringSlider = new JSlider(this.steeringModel) ;
			steeringSlider.setMajorTickSpacing(5);
			steeringSlider.setMinorTickSpacing(1);
			steeringSlider.setPaintTicks(true);
			steeringSlider.setPaintLabels(true);
			jpProgressBar.add(steeringSlider) ;
			this.add(jpProgressBar, BorderLayout.NORTH) ;
			JLabel steeringLabel = new JLabel("Current steering angle (degrees)") ;
			speedLabelPanel = new JPanel() ;
			speedLabelPanel.add(steeringLabel) ;
			this.add(speedLabelPanel, BorderLayout.SOUTH) ;
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4)) ;
			this.setVisible(true) ;
		}

		public void		updateSteeringAngle(SteeringData sd) {
			this.steeringModel.setValue((int) Math.round(sd.steeringAngle)) ;
		}

	}

	class				SteeringControllerPanel extends JPanel {

		private static final long	serialVersionUID = 1L;
		protected InstrumentedRobot	lr ;
		protected JPanel			steeringLabelPanel ;
		protected JPanel			steeringSliderPanel ;
		protected JSlider			steeringSlider ;

		public			SteeringControllerPanel() {
			this.setLayout(new BorderLayout()) ;
			this.setSize(450, 125) ;
			JLabel steeringLabel = new JLabel("Steering angle control (degrees)") ;
			steeringLabelPanel = new JPanel() ;
			steeringLabelPanel.add(steeringLabel) ;
			this.add(steeringLabelPanel, BorderLayout.SOUTH) ;
			DefaultBoundedRangeModel steeringModel =
					new DefaultBoundedRangeModel(0, 0, -15, 15) ;
			steeringSlider = new JSlider(steeringModel) ;
			steeringSlider.setMajorTickSpacing(5);
			steeringSlider.setMinorTickSpacing(1);
			steeringSlider.setPaintTicks(true);
			steeringSlider.setPaintLabels(true);
			steeringSliderPanel = new JPanel() ;
			steeringSliderPanel.add(steeringSlider) ;
			this.add(steeringSliderPanel, BorderLayout.NORTH) ;
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4)) ;
			this.setVisible(true) ;
		}

		public void		disconnectRobot(InstrumentedRobot lr2) {
			this.steeringSlider.addChangeListener(null) ;
			this.lr = null ;
		}

		public void		connectRobot(InstrumentedRobot lr) {
			this.lr = lr ;
			this.steeringSlider.addChangeListener(
				new SteeringActuatorDataListener(lr.getActuatorDataQueue())) ;
		}

	}

	class			SteeringActuatorDataListener implements ChangeListener {

		final protected BlockingQueue<RobotActuatorCommand> commandQueue ;

		public		SteeringActuatorDataListener(
				BlockingQueue<RobotActuatorCommand> commandQueue
				)
		{
			super();
			this.commandQueue = commandQueue;
		}

		@Override
		public void		stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource() ;
			double newSteeringAngle = source.getValue() ;
			final RobotActuatorCommand sc =
					AnotherLittleRobot.makeSteeringChange(newSteeringAngle) ;
			(new ActuatorDataSender(sc, this.commandQueue)).start() ;
		}

	}


	@Override
	public SensorDataReceptorInterface	makeSensorDataReceptor(
		PositionDisplay positionDisplay,
		BlockingQueue<RobotStateData> sensorDataQueue,
		int absoluteX,
		int absoluteY,
		int controlRadius
		)
	{
		return new SensorDataReceptor(positionDisplay, sensorDataQueue,
										absoluteX, absoluteY, controlRadius) ;
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
	class			SensorDataReceptor	extends Thread
										implements SensorDataReceptorInterface
	{

		final protected PositionDisplay					positionDisplay ;
		protected AnotherLittleRobotTeleoperationBoard	tBoard ;
		final protected BlockingQueue<RobotStateData>	dataQueue ;
		protected int									absoluteX ;
		protected int									absoluteY ;
		protected int									controlRadius ;
		protected boolean								shouldContinue ;

		public				SensorDataReceptor(
			PositionDisplay positionDisplay,
			BlockingQueue<RobotStateData> dataQueue,
			int absoluteX,
			int absoluteY,
			int controlRadius
			)
		{
			super();
			this.positionDisplay = positionDisplay;
			this.dataQueue = dataQueue;
			this.absoluteX = absoluteX ;
			this.absoluteY = absoluteY ;
			this.controlRadius = controlRadius ;
		}

		public synchronized void	cutoff() {
			this.shouldContinue = false;
		}

		public synchronized void	setTBoard(RobotTeleoperationBoard tBoard) {
			this.tBoard = (AnotherLittleRobotTeleoperationBoard) tBoard ;
		}

		@Override
		public synchronized void	start() {
			this.shouldContinue = true ;
			super.start();
		}

		@Override
		public void			run() {
			RobotStateData rsd = null ;
			Vector<RobotStateData> current = new Vector<RobotStateData>(4) ;
			while (this.shouldContinue) {
				try {
					rsd = this.dataQueue.take() ;		// wait if empty...
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				current.add(rsd) ;
				int n = this.dataQueue.drainTo(current) ;	// do not wait...
				for (int i = 0 ; i <= n ; i++) {
					rsd = current.elementAt(i) ;
					try {
						if (rsd instanceof PositioningData) {
							final PositioningData pd = (PositioningData) rsd ;
							SwingUtilities.invokeAndWait(
								new Runnable() {
									public void run() {
										positionDisplay.draw(pd) ;
									}
								}) ;
						} else {
							if (this.tBoard != null) {
								final RobotStateData rsd1 = rsd ;
								SwingUtilities.invokeAndWait(
									new Runnable() {
										public void run() {
											tBoard.processSensorData(rsd1) ;
										}
									}) ;
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				current.clear() ;
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
	class			ActuatorDataSender	extends Thread
										implements ActuatorDataSenderInterface
	{

		protected RobotActuatorCommand					rac ;
		protected BlockingQueue<RobotActuatorCommand>	commandQueue ;

		public			ActuatorDataSender(
			RobotActuatorCommand rac,
			BlockingQueue<RobotActuatorCommand> commandQueue
			)
		{
			super();
			this.rac = rac;
			this.commandQueue = commandQueue;
		}

		@Override
		public void			run() {
			try {
				SwingUtilities.invokeAndWait(
					new Runnable() {
						public void run() {
							commandQueue.clear() ;
							commandQueue.add(rac) ; }
					}) ;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}

	}

}

// $Id$