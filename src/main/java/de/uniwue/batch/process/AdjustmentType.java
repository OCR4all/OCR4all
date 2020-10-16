/**
 * File:     AdjustmentType.java
 * Package:  de.uniwue.batch.process
 * 
 * Author:   Herbert Baier
 * Date:     30.09.2020
 */
package de.uniwue.batch.process;

/**
 * Defines adjustment types.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public enum AdjustmentType {
	pdf, image;

	/**
	 * The argument in the batch workflow configuration.
	 */
	public static final String argument = "--type";

}
