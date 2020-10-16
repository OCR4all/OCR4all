/**
 * File:     ResultGenerationStrategy.java
 * Package:  de.uniwue.batch.process
 * 
 * Author:   Herbert Baier
 * Date:     30.09.2020
 */
package de.uniwue.batch.process;

/**
 * Defines result generation strategies.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public enum ResultGenerationStrategy {
	combine("fillUp"), groundTruth("gt"), prediction("pred");

	/**
	 * The default strategy.
	 */
	public static final ResultGenerationStrategy defaultStrategy = combine;

	/**
	 * The argument in the batch workflow configuration.
	 */
	public static final String argument = "--strategy";

	/**
	 * The process name.
	 */
	private final String processName;

	/**
	 * Creates a result generation strategy.
	 * 
	 * @param processName The process name.
	 * @since 1.8
	 */
	private ResultGenerationStrategy(String processName) {
		this.processName = processName;
	}

	/**
	 * Returns the process name.
	 *
	 * @return The process name.
	 * @since 1.8
	 */
	public String getProcessName() {
		return processName;
	}

}
