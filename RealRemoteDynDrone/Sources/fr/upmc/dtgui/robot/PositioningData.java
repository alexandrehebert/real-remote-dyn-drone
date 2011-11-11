//	PositioningData.java --- 

package fr.upmc.dtgui.robot;

public class		PositioningData		extends RobotStateData {

	public double x ;
	public double y ;
	public double direction ;

	public			PositioningData(double x, double y, double direction) {
		super();
		this.x = x;
		this.y = y;
		this.direction = direction ;
	}
        
        public String toString() {
            return "pd{x=" + x + ";y=" + y + ";d=" + direction + "}";
        }

}

// $Id$