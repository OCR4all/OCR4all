/**
 * File:     BatchProcessOverview.java
 * Package:  de.uniwue.batch.report
 * 
 * Author:   Herbert Baier
 * Date:     25.09.2020
 */
package de.uniwue.batch.report;

import java.util.Date;
import java.util.List;

import de.uniwue.batch.BatchWorkflow;
import de.uniwue.batch.WorkflowConfiguration;

/**
 * Defines batch process overviews.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class BatchProcessOverview {
	/**
	 * The running step. 0 if not started.
	 */
	private final int step;
	/**
	 * The configuration.
	 */
	private final WorkflowConfiguration.ProcessConfiguration configuration;

	/**
	 * The available page ids.
	 */
	private final List<String> availablePageIds;

	/**
	 * The process begin time.
	 */
	private final Date begin;

	/**
	 * The process end time. Null if running.
	 */
	private final Date end;

	/**
	 * True if the process worker is running.
	 */
	private final boolean isRunning;

	/**
	 * The progress. This is a value between 0 and 1 inclusive.
	 */
	private final float progress;

	/**
	 * The message.
	 */
	private final String message;

	/**
	 * True if process worker was canceled.
	 */
	private final boolean isCanceled;

	/**
	 * True if there exist troubles performing process worker.
	 */
	private final boolean isTrouble;

	/**
	 * The standard output of the process.
	 */
	private final String standardOutput;

	/**
	 * The standard error of the process.
	 */
	private final String standardError;

	/**
	 * Creates a batch process overview.
	 * 
	 * @param step          The step.
	 * @param configuration
	 * @since 1.8
	 */
	public BatchProcessOverview(int step, BatchWorkflow.ProcessWorker processWorker) {
		super();
		this.step = step;

		configuration = processWorker.getConfiguration();
		availablePageIds = processWorker.getAvailablePageIds();

		begin = processWorker.getBegin();
		end = processWorker.getEnd();

		isRunning = processWorker.isRunning();
		progress = processWorker.getWrapper().getProgress();

		message = processWorker.getMessage();

		isCanceled = processWorker.isCanceled();
		isTrouble = processWorker.isTrouble();

		if (processWorker.getWrapper().getHandler() == null) {
			standardOutput = null;
			standardError = null;
		} else {
			standardOutput = processWorker.getWrapper().getHandler().getConsoleOut();
			standardError = processWorker.getWrapper().getHandler().getConsoleErr();
		}
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
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public WorkflowConfiguration.ProcessConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the available page ids.
	 *
	 * @return The available page ids.
	 * @since 1.8
	 */
	public List<String> getAvailablePageIds() {
		return availablePageIds;
	}

	/**
	 * Returns the process begin time.
	 *
	 * @return The process begin time.
	 * @since 1.8
	 */
	public String getBegin() {
		return begin == null ? null : begin.toString();
	}

	/**
	 * Returns the process end time. Null if running.
	 *
	 * @return The process end time.
	 * @since 1.8
	 */
	public String getEnd() {
		return end == null ? null : end.toString();
	}

	/**
	 * Returns true if the process worker is running.
	 * 
	 * @return True if the process worker is running.
	 * @since 1.8
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Returns the progress. This is a value between 0 and 1 inclusive.
	 *
	 * @return The progress.
	 * @since 1.8
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * Returns the message.
	 *
	 * @return The message.
	 * @since 1.8
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns true if there exist troubles performing this worker.
	 *
	 * @return True if there exist troubles performing this worker.
	 * @since 1.8
	 */
	public boolean isTrouble() {
		return isTrouble;
	}

	/**
	 * Returns true if process worker was canceled.
	 *
	 * @return True if process worker was canceled.
	 * @since 1.8
	 */
	public boolean isCanceled() {
		return isCanceled;
	}

	/**
	 * Returns the standard output of the process.
	 *
	 * @return The standard output of the process.
	 * @since 1.8
	 */
	public String getStandardOutput() {
		return standardOutput;
	}

	/**
	 * Returns the standard error of the process.
	 *
	 * @return The standard error of the process.
	 * @since 1.8
	 */
	public String getStandardError() {
		return standardError;
	}
}
