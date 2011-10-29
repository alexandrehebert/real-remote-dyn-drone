//	RobotSelector.java --- 

package fr.upmc.dtgui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.Robot;

/**
 * The class <code>RobotSelector</code> defines a panel to gather buttons
 * used to select the robot that will be currently under the control of the
 * teleoperation station.  Buttons are added and removed when robots enter
 * or exit the control area.  Buttons are put in a Button group so that only
 * one can be selected at any time.
 *
 * <p><strong>Description</strong></p>
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
public class		RobotSelector		extends JPanel {

	private static final long			serialVersionUID = 1L;
	protected TeleoperationGUI			tgui ;
	protected ButtonGroup				selector ;
	protected int						sizeX ;
	protected int						sizeY ;
	protected Hashtable<Robot,JButton>	buttonMapping ;


	public			RobotSelector(
		TeleoperationGUI tgui,
		int sizeX,
		int sizeY
		)
	{
		super();
		this.tgui = tgui;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.buttonMapping = new Hashtable<Robot,JButton>() ;
		this.selector = new ButtonGroup() ;
		this.setSize(this.sizeX, this.sizeY) ;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
		this.setVisible(true) ;
	}

	public void		registerRobot(InstrumentedRobot lr) {
		JButton b = new JButton(lr.getRobotName()) ;
		b.setActionCommand("select") ;
		b.setSelected(false) ;
		b.setVisible(true) ;
		this.add(b) ;
		final InstrumentedRobot lr1 = lr ;
		final TeleoperationGUI tgui = this.tgui ;
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("select")) {
					tgui.selectRobot(lr1) ;
				}
			}
		}) ;
		this.selector.add(b) ;
		this.buttonMapping.put(lr1, b) ;
		this.validate() ;
	}

	public void		unregisterRobot(InstrumentedRobot lr) {
		JButton b = this.buttonMapping.remove(lr) ;
		this.remove(b) ;
		this.selector.remove(b) ;
	}
}

// $Id$