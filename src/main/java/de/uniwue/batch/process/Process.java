/**
 * File:     Process.java
 * Package:  de.uniwue.batch
 * 
 * Author:   Herbert Baier
 * Date:     22.09.2020
 */
package de.uniwue.batch.process;

/**
 * Defines processes.
 *
 * @author      Herbert Baier
 * @version     1.0
 * @since       1.8
 */
public enum Process {
	adjustment, preprocessing, segmentationDummy, lineSegmentation, recognition, resultGeneration
}
