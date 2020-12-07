/**
 * File:     BatchProcessManager.java
 * Package:  de.uniwue.controller
 * 
 * Author:   Herbert Baier
 * Date:     21.09.2020
 */
package de.uniwue.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import de.uniwue.batch.BatchWorkflow;
import de.uniwue.batch.WorkflowConfiguration;
import de.uniwue.batch.report.BatchScheduledOverview;
import de.uniwue.batch.report.BatchWorkflowDetail;
import de.uniwue.batch.report.BatchWorkflowOverview;
import de.uniwue.batch.report.ProcessManagerOverview;

/**
 * Defines batch process managers.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
@Service
@ApplicationScope
public class BatchProcessManager {
	/**
	 * Defines queue positions.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Position {
		first, last, index
	}

	/**
	 * The processes. The key is the batch id.
	 */
	private final Hashtable<String, BatchWorkflow> processes = new Hashtable<>();

	/**
	 * The running processes. The key is the batch id.
	 */
	private final Hashtable<String, BatchWorkflow> running = new Hashtable<>();

	/**
	 * The scheduled processes.
	 */
	private final List<BatchWorkflow> scheduled = new ArrayList<>();

	/**
	 * The created time.
	 */
	private final Date created = new Date();

	/**
	 * The state update time.
	 */
	private Date stateUpdated = null;

	/**
	 * True if the schedule is running. Otherwise it is paused.
	 */
	private boolean isRunning = true;

	/**
	 * Schedule the processes.
	 * 
	 * @since 1.8
	 */
	private synchronized void schedule() {
		// expunge done processes from running table and search for a sequential process
		boolean isSequentialRunning = false;
		Set<String> projects = new HashSet<>();
		for (BatchWorkflow batch : new ArrayList<>(running.values()))
			if (batch.isDone())
				running.remove(batch.getId());
			else {
				projects.add(batch.getConfiguration().getProject());

				if (WorkflowConfiguration.Processing.sequential.equals(batch.getConfiguration().getProcessing()))
					isSequentialRunning = true;
			}

		synchronized (scheduled) {
			for (BatchWorkflow batch : new ArrayList<>(scheduled))
				if (!BatchWorkflow.State.scheduled.equals(batch.getState()))
					scheduled.remove(batch);

			// if a sequential process is running, do not schedule additional processes
			if (isRunning && !isSequentialRunning)
				for (BatchWorkflow batch : new ArrayList<>(scheduled)) {
					if (WorkflowConfiguration.Processing.sequential.equals(batch.getConfiguration().getProcessing())) {
						if (running.isEmpty()) {
							scheduled.remove(batch);

							start(batch);
						}

						break;
					} else if (!projects.contains(batch.getConfiguration().getProject())) {
						scheduled.remove(batch);

						start(batch);

						projects.add(batch.getConfiguration().getProject());
					}
				}
		}
	}

	/**
	 * Starts the batch workflow.
	 * 
	 * @param batch The batch to start.
	 * @since 1.8
	 */
	private void start(BatchWorkflow batch) {
		batch.start(new BatchWorkflow.Callback() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * de.uniwue.batch.BatchWorkflow.Callback#finish(de.uniwue.batch.BatchWorkflow)
			 */
			@Override
			public void finish(BatchWorkflow batchWorkflow) {
				schedule();
			}
		});

		if (BatchWorkflow.State.running.equals(batch.getState()))
			running.put(batch.getId(), batch);
	}

	/**
	 * Returns the batch process.
	 * 
	 * @param id The process id.
	 * @return The batch process.
	 * @throws IllegalArgumentException Throws if the batch project is unknown.
	 * @since 1.8
	 */
	private BatchWorkflow getBatchtProcess(String id) throws IllegalArgumentException {
		BatchWorkflow batch = processes.get(id);

		if (batch == null)
			throw new IllegalArgumentException("unknown project id " + id + ".");

		return batch;
	}

	/**
	 * Returns the process manager overview.
	 * 
	 * @return The process manager overview.
	 * @since 1.8
	 */
	public ProcessManagerOverview getOverview() {
		ProcessManagerOverview.ProcessOverview processOverview = new ProcessManagerOverview.ProcessOverview();

		for (BatchWorkflow batch : new ArrayList<BatchWorkflow>(processes.values()))
			switch (batch.getState()) {
			case canceled:
				processOverview.incrementCanceled();
				break;
			case completed:
				processOverview.incrementCompleted();
				break;
			case interrupted:
				processOverview.incrementInterrupted();
				break;
			case running:
				processOverview.incrementRunning();
				break;
			case scheduled:
				processOverview.incrementScheduled();
				break;
			default:
				break;
			}

		return new ProcessManagerOverview(created, stateUpdated, isRunning, processOverview);
	}

	/**
	 * Updates the process manager state.
	 * 
	 * @param isRun True if the schedule should run. Otherwise it should pause.
	 * @return The process manager overview.
	 * @since 1.8
	 */
	private synchronized ProcessManagerOverview update(boolean isRun) {
		if (isRunning != isRun) {
			isRunning = isRun;

			stateUpdated = new Date();
		}

		schedule();

		return getOverview();
	}

	/**
	 * Runs the process manager and returns its overview.
	 * 
	 * @return The process manager overview.
	 * @since 1.8
	 */
	public ProcessManagerOverview run() {
		return update(true);
	}

	/**
	 * Pauses the process manager and returns its overview.
	 * 
	 * @return The process manager overview.
	 * @since 1.8
	 */
	public ProcessManagerOverview pause() {
		return update(false);
	}

	/**
	 * Returns the process.
	 * 
	 * @param id The scheduled process id.
	 * @return The scheduled process. Null if the process is not scheduled.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public BatchScheduledOverview getScheduledProcess(String id) throws IllegalArgumentException {
		BatchWorkflow batch = getBatchtProcess(id);

		int index = 1;
		for (BatchWorkflow scheduled : new ArrayList<>(this.scheduled)) {
			if (scheduled.equals(batch))
				return new BatchScheduledOverview(index, batch);

			index++;
		}

		return null;
	}

	/**
	 * Returns the scheduled process.
	 * 
	 * @param id The process id.
	 * @return The process.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public BatchWorkflowOverview getProcess(String id) throws IllegalArgumentException {
		return new BatchWorkflowOverview(getBatchtProcess(id));
	}

	/**
	 * Returns the process details.
	 * 
	 * @param id The process id.
	 * @return The process details.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public BatchWorkflowDetail getDetails(String id) throws IllegalArgumentException {
		return getBatchtProcess(id).getDetails();
	}

	/**
	 * Returns the process configuration.
	 * 
	 * @param id The process id.
	 * @return The process configuration.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public WorkflowConfiguration getConfiguration(String id) throws IllegalArgumentException {
		return getBatchtProcess(id).getConfiguration();
	}

	/**
	 * Returns the processes sorted by created time.
	 * 
	 * @return The processes sorted by created time.
	 * @since 1.8
	 */
	public List<BatchWorkflowOverview> getProcesses() {
		List<BatchWorkflowOverview> overviews = new ArrayList<BatchWorkflowOverview>();
		for (BatchWorkflow batch : new ArrayList<BatchWorkflow>(processes.values()))
			overviews.add(new BatchWorkflowOverview(batch));

		Collections.sort(overviews,
				(BatchWorkflowOverview o1, BatchWorkflowOverview o2) -> o1.getCreated().compareTo(o2.getCreated()));

		return overviews;
	}

	/**
	 * Returns the running processes sorted by start time.
	 * 
	 * @return The running processes sorted by start time.
	 * @since 1.8
	 */
	public List<BatchWorkflowOverview> getRunningProcesses() {
		List<BatchWorkflowOverview> overviews = new ArrayList<BatchWorkflowOverview>();
		for (BatchWorkflow batch : new ArrayList<BatchWorkflow>(running.values()))
			overviews.add(new BatchWorkflowOverview(batch));

		Collections.sort(overviews, (BatchWorkflowOverview o1, BatchWorkflowOverview o2) -> {
			if (o1.getStart() == null)
				return 1;
			else if (o2.getStart() == null)
				return -1;
			else
				return o1.getStart().compareTo(o2.getStart());
		});

		return overviews;
	}

	/**
	 * Returns the done processes sorted by finish time.
	 * 
	 * @return The done processes sorted by finish time.
	 * @since 1.8
	 */
	public List<BatchWorkflowOverview> getDoneProcesses() {
		List<BatchWorkflowOverview> overviews = new ArrayList<BatchWorkflowOverview>();
		for (BatchWorkflow batch : new ArrayList<BatchWorkflow>(processes.values()))
			if (batch.isDone())
				overviews.add(new BatchWorkflowOverview(batch));

		Collections.sort(overviews, (BatchWorkflowOverview o1, BatchWorkflowOverview o2) -> {
			if (o1.getFinish() == null)
				return 1;
			else if (o2.getFinish() == null)
				return -1;
			else
				return o1.getFinish().compareTo(o2.getFinish());
		});

		return overviews;
	}

	/**
	 * Returns the scheduled processes in the queue order.
	 * 
	 * @return The scheduled processes in the queue order.
	 * @since 1.8
	 */
	public List<BatchScheduledOverview> getScheduledProcesses() {
		List<BatchScheduledOverview> overviews = new ArrayList<>();
		int index = 0;
		for (BatchWorkflow batch : new ArrayList<BatchWorkflow>(scheduled))
			overviews.add(new BatchScheduledOverview(++index, batch));

		return overviews;
	}

	/**
	 * Schedules the batch process.
	 * 
	 * @param configuration The configuration.
	 * @return The scheduled process.
	 * @throws IllegalArgumentException Throws on workflow configuration troubles.
	 * @throws IllegalStateException    Throws if the input folder of project does
	 *                                  not exist.
	 * @since 1.8
	 */
	public synchronized BatchWorkflowOverview schedule(WorkflowConfiguration configuration)
			throws IllegalArgumentException, IllegalStateException {
		BatchWorkflow batch = new BatchWorkflow(configuration);

		processes.put(batch.getId(), batch);

		scheduled.add(batch);

		schedule();

		return new BatchWorkflowOverview(batch);
	}

	/**
	 * Schedules the process to the desired index. The first index is 1.
	 * 
	 * @param id    The process id.
	 * @param index The index.
	 * @return The scheduled processes.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public synchronized List<BatchScheduledOverview> schedule(String id, int index) throws IllegalArgumentException {
		return schedule(id, Position.index, index - 1);
	}

	/**
	 * Schedules the process to the desired position.
	 * 
	 * @param id       The process id.
	 * @param position The position.
	 * @return The scheduled processes.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public synchronized List<BatchScheduledOverview> schedule(String id, Position position)
			throws IllegalArgumentException {
		return schedule(id, position, -1);
	}

	/**
	 * Schedules the process to the desired position.
	 * 
	 * @param id       The process id.
	 * @param position The position.
	 * @param index    The index.
	 * @return The scheduled processes.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	private synchronized List<BatchScheduledOverview> schedule(String id, Position position, int index)
			throws IllegalArgumentException {
		BatchWorkflow batch = getBatchtProcess(id);

		synchronized (scheduled) {
			if (scheduled.remove(batch))
				switch (position) {
				case first:
					scheduled.add(0, batch);
					break;

				case last:
					scheduled.add(batch);
					break;

				case index:
					if (index < 0 || index > scheduled.size())
						throw new IllegalArgumentException("the index " + index
								+ " is out of range, current allowed range [1.." + (1 + scheduled.size()) + "]");

					scheduled.add(index, batch);
					break;

				default:
					break;
				}
		}

		schedule();

		return getScheduledProcesses();
	}

	/**
	 * Cancels the process.
	 * 
	 * @param id The process id.
	 * @return The process.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public synchronized BatchWorkflowOverview cancel(String id) throws IllegalArgumentException {
		BatchWorkflow batch = getBatchtProcess(id);

		batch.cancel();

		schedule();

		return new BatchWorkflowOverview(batch);
	}

	/**
	 * Expunges the process if it is done.
	 * 
	 * @param id The process id.
	 * @return The process.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	public synchronized BatchWorkflowOverview expunge(String id) throws IllegalArgumentException {
		BatchWorkflow batch = getBatchtProcess(id);

		if (batch.isDone())
			processes.remove(id);

		schedule();

		return new BatchWorkflowOverview(batch);
	}

	/**
	 * Expunges the done processes.
	 * 
	 * @return The expunged process sorted by finish time.
	 * @since 1.8
	 */
	public synchronized List<BatchWorkflowOverview> expunge() {
		List<BatchWorkflowOverview> expunge = new ArrayList<BatchWorkflowOverview>();
		for (BatchWorkflow batch : new ArrayList<BatchWorkflow>(processes.values()))
			if (batch.isDone()) {
				processes.remove(batch.getId());

				expunge.add(new BatchWorkflowOverview(batch));
			}

		schedule();

		Collections.sort(expunge, (BatchWorkflowOverview o1, BatchWorkflowOverview o2) -> {
			if (o1.getFinish() == null)
				return 1;
			else if (o2.getFinish() == null)
				return -1;
			else
				return o1.getFinish().compareTo(o2.getFinish());
		});

		return expunge;
	}

}
