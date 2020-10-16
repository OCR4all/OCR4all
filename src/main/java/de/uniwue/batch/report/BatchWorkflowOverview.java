/**
 * File:     BatchWorkflowOverview.java
 * Package:  de.uniwue.batch.report
 * 
 * Author:   Herbert Baier
 * Date:     24.09.2020
 */
package de.uniwue.batch.report;

import java.util.Date;

import de.uniwue.batch.BatchWorkflow;
import de.uniwue.batch.WorkflowConfiguration;

/**
 * BatchWorkflowOverview is an immutable class that defines batch workflow
 * overviews.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class BatchWorkflowOverview {
	/**
	 * The id.
	 */
	private final String id;

	/**
	 * The project.
	 */
	private final String project;

	/**
	 * The project type.
	 */
	private final WorkflowConfiguration.Type type;

	/**
	 * The processing mode.
	 */
	private final WorkflowConfiguration.Processing processing;

	/**
	 * The state.
	 */
	private final BatchWorkflow.State state;

	/**
	 * True if the workflow was canceled.
	 */
	private final boolean isCanceled;

	/**
	 * The created time.
	 */
	private final Date created;

	/**
	 * The start time.
	 */
	private final Date start;

	/**
	 * The finish time.
	 */
	private final Date finish;

	/**
	 * The running step. 0 if not started.
	 */
	private final int step;

	/**
	 * The number of steps.
	 */
	private final int stepNumber;

	/**
	 * Creates a batch workflow overview.
	 * 
	 * @param batch The batch workflow.
	 * @since 1.8
	 */
	public BatchWorkflowOverview(BatchWorkflow batch) {
		super();

		id = batch.getId();

		project = batch.getConfiguration().getProject();
		type = batch.getConfiguration().getType();
		processing = batch.getConfiguration().getProcessing();

		state = batch.getState();

		isCanceled = batch.isCanceled();
		created = batch.getCreated();
		start = batch.getStart();
		finish = batch.getFinish();

		step = batch.getStep();
		stepNumber = batch.getStepNumber();
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the project.
	 *
	 * @return The project.
	 * @since 1.8
	 */
	public String getProject() {
		return project;
	}

	/**
	 * Returns the project type.
	 *
	 * @return The project type.
	 * @since 1.8
	 */
	public WorkflowConfiguration.Type getType() {
		return type;
	}

	/**
	 * Returns the processing mode.
	 *
	 * @return The processing mode.
	 * @since 1.8
	 */
	public WorkflowConfiguration.Processing getProcessing() {
		return processing;
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public BatchWorkflow.State getState() {
		return state;
	}

	/**
	 * Returns true if the workflow was canceled.
	 *
	 * @return True if the workflow was canceled.
	 * @since 1.8
	 */
	public boolean isCanceled() {
		return isCanceled;
	}

	/**
	 * Returns the created time.
	 *
	 * @return The created time.
	 * @since 1.8
	 */
	public String getCreated() {
		return created == null ? null : created.toString();
	}

	/**
	 * Returns the start time.
	 *
	 * @return The start time.
	 * @since 1.8
	 */
	public String getStart() {
		return start == null ? null : start.toString();
	}

	/**
	 * Returns the finish time.
	 *
	 * @return The finish time.
	 * @since 1.8
	 */
	public String getFinish() {
		return finish == null ? null : finish.toString();
	}

	/**
	 * Returns the running step. 0 if not started.
	 *
	 * @return The running step.
	 * @since 1.8
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Returns the number of steps.
	 *
	 * @return The number of steps.
	 * @since 1.8
	 */
	public int getStepNumber() {
		return stepNumber;
	}

}
