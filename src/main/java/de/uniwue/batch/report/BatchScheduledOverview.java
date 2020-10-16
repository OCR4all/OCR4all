/**
 * File:     BatchScheduledOverview.java
 * Package:  de.uniwue.batch.report
 * 
 * Author:   Herbert Baier
 * Date:     28.09.2020
 */
package de.uniwue.batch.report;

import de.uniwue.batch.BatchWorkflow;

/**
 * BatchScheduledOverview is an immutable class that defines overviews for
 * scheduled processes.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class BatchScheduledOverview extends BatchWorkflowOverview {
	/**
	 * The scheduled index.
	 */
	private final int index;

	/**
	 * Creates an overviews for a scheduled process.
	 * 
	 * @param batch The batch workflow.
	 * @since 1.8
	 */
	public BatchScheduledOverview(int index, BatchWorkflow batch) {
		super(batch);

		this.index = index;
	}

	/**
	 * Returns the index.
	 *
	 * @return The index.
	 * @since 1.8
	 */
	public int getIndex() {
		return index;
	}

}
