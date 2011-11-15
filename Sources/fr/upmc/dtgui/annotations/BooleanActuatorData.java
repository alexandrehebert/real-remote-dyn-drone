//	BooleanActuatorData.java --- 

package fr.upmc.dtgui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation <code>BooleanActuatorData</code> tags setter methods of a
 * robot with information concerning how to use the corresponding boolean
 * data in the robot representation as a control over the robot's behavior.
 *
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	minWritingRate() <= maxWritingRate()
 * </pre>
 * 
 * <p>Created on : 2011-09-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface BooleanActuatorData {

	/**
	 * A group name allowing to put together into one aggregated
	 * actuator with several data.
	 */
	String			groupName() ;
	/**
	 * The maximum frequency, in Hz, at which the actuator can be called.
	 */
	double			maxWritingRate() ; // in Hz
	/**
	 * The minimum frequency, in Hz, at which the actuator must be called.
	 */
	double			minWritingRate() ; // in Hz

}

// $Id$