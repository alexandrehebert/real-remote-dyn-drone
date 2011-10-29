//	WithSensors.java --- 

package fr.upmc.dtgui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation <code>WithSensors</code> indicates that the annotated class
 * has sensory data and uses sensor annotations to define these.
 * 
 * <p>Created on : 2011-10-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface WithSensors {

}

// $Id$