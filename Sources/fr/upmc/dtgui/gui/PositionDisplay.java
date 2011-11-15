//	PositionDisplay.java --- 

package fr.upmc.dtgui.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import fr.upmc.dtgui.robot.PositioningData;

/**
 * The class <code>PositionDisplay</code> implements a radar-like display for
 * the robot teleoperation GUI.  The robot has an absolute position in the
 * positive x, y quadrant which is repositioned in the referential of the
 * 1000x1000 meters of the display given its central position.  The robot is
 * represented by a dot, and its direction is made visible with a small line.
 * As the robot moves, a trace of its preceding position is left on the screen
 * to see its overall path.
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
public class			PositionDisplay		extends JPanel
{

	private static final long		serialVersionUID = 1L;

	/** Model tracking the robot position.								*/
	protected RobotDisplayModel		model ;
	protected Color					backgroundColor ;
	protected Color					foregroundColor ;
	protected Color					traceColor ;
	/** X position of the station (center) in the world coordinates.	*/
	protected int					absoluteX ;
	/** Y position of the station (center) in the world coordinates.	*/
	protected int					absoluteY ;
	/** X position of the station (center) in the station coordinates.	*/
	protected int					relativeX ;
	/** Y position of the station (center) in the station coordinates.	*/
	protected int					relativeY ;
	/** Distance from the station under which robot can be controlled.	*/
	protected int					controlRadius ;
	/** Size of the visibility area of the station in the X dimension.	*/
	protected int					sizeX ;
	/** Size of the visibility area of the station in the Y dimension.	*/
	protected int					sizeY ;

	/**
	 * Constructor for the Position display of the control station.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param backgroundColor	background color of the display.
	 * @param foregroundColor	color in which tracked objects are traced.
	 * @param traceColor		color used to trace the path of tracked objects.
	 * @param absoluteX			X of the station (center) in world coordinates.
	 * @param absoluteY			Y of the station (center) in world coordinates
	 * @param relativeX			X of the station (center) in station coordinates.
	 * @param relativeY			Y of the station (center) in station coordinates.
	 * @param controlRadius		distance from the station under which robot can be controlled.
	 * @param sizeX				size of the visibility area of the station in the X dimension.
	 * @param sizeY				size of the visibility area of the station in the Y dimension.
	 */
	public 						PositionDisplay(
		Color backgroundColor,
		Color foregroundColor,
		Color traceColor,
		int absoluteX,
		int absoluteY,
		int relativeX,
		int relativeY,
		int controlRadius,
		int sizeX,
		int sizeY
		)
	{
		super();
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.traceColor = traceColor;
		this.absoluteX = absoluteX;
		this.absoluteY = absoluteY;
		this.relativeX = relativeX ;
		this.relativeY = relativeY ;
		this.controlRadius = controlRadius ;
		this.sizeX = sizeX ;
		this.sizeY = sizeY ;
		this.model = new RobotDisplayModel(0, 0, 0, 0, this) ;
		this.setSize(this.sizeX, this.sizeY) ;
		this.setBackground(this.backgroundColor) ;
		this.setForeground(this.foregroundColor) ;
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)) ;
	}

	public RobotDisplayModel	getModel() {
		return model;
	}

	public void					setModel(RobotDisplayModel model) {
		this.model = model;
	}

	/**
	 * @return the sizeX
	 */
	public int					getSizeX() {
		return sizeX;
	}

	/**
	 * @return the sizeY
	 */
	public int					getSizeY() {
		return sizeY;
	}

	@Override
	protected void				paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setSize(this.sizeX, this.sizeY) ;
		g.setColor(this.foregroundColor) ;
		g.drawOval(this.relativeX - this.controlRadius,
						this.relativeY - this.controlRadius,
						2 * this.controlRadius, 2 * this.controlRadius) ;
	}

	public void					draw(PositioningData pd) {
		int x = (((int) Math.round(pd.x)) - this.absoluteX) + this.relativeX ;
		int y = (((int) Math.round(pd.y)) - this.absoluteY) + this.relativeY ;
		Graphics g = this.getGraphics() ;
		if (x >= 0 && x <= this.sizeX && y >= 0 && y <= this.sizeY) {
			this.model.setVisible(true) ;
		}
		g.setColor(this.backgroundColor) ;
		if (this.model.visible) {
			this.model.draw(g) ;
			g.setColor(this.traceColor) ;
			g.fillOval(this.model.currentX - 5, this.model.currentY - 5,
																	10, 10) ;
		}
		g.setColor(this.foregroundColor) ;
		this.model.update(x, y,
			(int) Math.round(10.0 * Math.cos(Math.toRadians(pd.direction))),
			(int) Math.round(10.0 * Math.sin(Math.toRadians(pd.direction)))) ;
		if (this.model.visible) {
			this.model.draw(g) ;
		}
		g.drawOval(this.relativeX - this.controlRadius,
				this.relativeY - this.controlRadius,
				2 * this.controlRadius, 2 * this.controlRadius) ;
	}
	
}

/**
 * The class <code>RobotDisplayModel</code> represent a model of the way a
 * robot has to be displayed, i.e. essentially its position and its current
 * direction.  It is updated with new sensory data coming from the robot.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	this.currentDeltaX >= 0 && this.currentDeltaX <= 10
 * 				this.currentDeltaY >= 0 && this.currentDeltaY <= 10
 * </pre>
 * 
 * <p>Created on : 2011-10-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
class				RobotDisplayModel {
	protected boolean			visible ;
	protected int				currentX ;
	protected int				currentY ;
	protected int				currentDeltaX ;
	protected int				currentDeltaY ;
	protected PositionDisplay	myPositionDisplay ;

	public				RobotDisplayModel(
		int currentX,
		int currentY,
		int currentDeltaX,
		int currentDeltaY,
		PositionDisplay myPositionDisplay
		)
	{
		super();
		this.visible = false ;
		this.currentX = currentX;
		this.currentY = currentY;
		this.currentDeltaX = currentDeltaX;
		this.currentDeltaY = currentDeltaY;
		this.myPositionDisplay = myPositionDisplay ;
	}

	public boolean		isVisible() {
		return visible;
	}

	public void			setVisible(boolean visible) {
		this.visible = visible;
	}

	public void			update(
		int currentX,
		int currentY,
		int currentDeltaX,
		int currentDeltaY
		)
	{
		this.currentX = currentX;
		this.currentY = currentY;
		this.currentDeltaX = currentDeltaX;
		this.currentDeltaY = currentDeltaY;
	}

	public void			draw(Graphics g) {
		if (this.visible) {
			if (this.inDisplay()) {
				g.fillOval(this.currentX - 5, this.currentY - 5, 10, 10) ;
				g.drawLine(this.currentX, this.currentY,
					this.currentX + this.currentDeltaX,
					this.currentY + this.currentDeltaY) ;
			}
		}
	}

	public boolean		inDisplay() {
		return	this.currentX >= 0 &&
					this.currentX <= this.myPositionDisplay.getSizeX() &&
				this.currentY >= 0 &&
					this.currentY <= this.myPositionDisplay.getSizeY() ;
		
	}
}

// $Id$