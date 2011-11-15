//	IntegerRange.java --- 

package fr.upmc.dtgui.annotations;

/**
 * The annotation <code>IntegerRange</code> is used in an annotation
 * <code>IntegerSensorData</code> ou <code>IntegerActuatorData</code> to
 * define the range of admissible values for the corresponding data.
 *
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	inf() <= sup()
 * </pre>
 * 
 * <p>Created on : 2011-09-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public @interface IntegerRange {
	int		inf() ;
	int		sup() ;
}

// $Id$