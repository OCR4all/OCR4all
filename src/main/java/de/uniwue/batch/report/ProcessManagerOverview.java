/**
 * File:     ProcessManagerOverview.java
 * Package:  de.uniwue.batch.report
 * 
 * Author:   Herbert Baier
 * Date:     25.09.2020
 */
package de.uniwue.batch.report;

import java.util.Date;

/**
 * ProcessManagerOverview is an immutable class that defines process manager
 * overviews.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class ProcessManagerOverview {
	/**
	 * The created time.
	 */
	private final Date created;

	/**
	 * The state update time.
	 */
	private final Date stateUpdated;

	/**
	 * True if the schedule is running. Otherwise it is paused.
	 */
	private final boolean isScheduleRunning;

	/**
	 * The process overview.
	 */
	private final ProcessOverview process;

	/**
	 * Creates a process manager overview.
	 * 
	 * @param created      The created time.
	 * @param stateUpdated The state update time.
	 * @param isRunning    True if the schedule is running. Otherwise it is paused.
	 * @param process
	 * @since 1.8
	 */
	public ProcessManagerOverview(Date created, Date stateUpdated, boolean isRunning, ProcessOverview process) {
		super();

		this.created = created;
		this.stateUpdated = stateUpdated;
		this.isScheduleRunning = isRunning;
		this.process = process;
	}

	/**
	 * Returns the created time.
	 *
	 * @return The created time.
	 * @since 1.8
	 */
	public String getCreated() {
		return created.toString();
	}

	/**
	 * Returns the state update time.
	 *
	 * @return The state update time.
	 * @since 1.8
	 */
	public String getStateUpdated() {
		return stateUpdated == null ? null : stateUpdated.toString();
	}

	/**
	 * Returns true if the schedule is running. Otherwise it is paused.
	 *
	 * @return True if the schedule is running. Otherwise it is paused.
	 * @since 1.8
	 */
	public boolean isScheduleRunning() {
		return isScheduleRunning;
	}

	/**
	 * Returns the process overview.
	 *
	 * @return The process overview.
	 * @since 1.8
	 */
	public ProcessOverview getProcess() {
		return process;
	}

	/**
	 * Defines process overviews.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ProcessOverview {
		/**
		 * The number of running processes.
		 */
		private int running = 0;

		/**
		 * The number of scheduled processes.
		 */
		private int scheduled = 0;

		/**
		 * The number of completed processes.
		 */
		private int completed = 0;

		/**
		 * The number of canceled processes.
		 */
		private int canceled = 0;

		/**
		 * The number of interrupted processes.
		 */
		private int interrupted = 0;

		public ProcessOverview() {
			super();
		}

		/**
		 * Returns the number of running processes.
		 *
		 * @return The number of running processes.
		 * @since 1.8
		 */
		public int getRunning() {
			return running;
		}

		/**
		 * Increment by one the number of running processes.
		 * 
		 * @since 1.8
		 */
		public void incrementRunning() {
			running++;
		}

		/**
		 * Returns the number of scheduled processes.
		 *
		 * @return The number of scheduled processes.
		 * @since 1.8
		 */
		public int getScheduled() {
			return scheduled;
		}

		/**
		 * Increment by one the number of scheduled processes.
		 * 
		 * @since 1.8
		 */
		public void incrementScheduled() {
			scheduled++;
		}

		/**
		 * Returns the number of completed processes.
		 *
		 * @return The number of completed processes.
		 * @since 1.8
		 */
		public int getCompleted() {
			return completed;
		}

		/**
		 * Increment by one the number of completed processes.
		 * 
		 * @since 1.8
		 */
		public void incrementCompleted() {
			completed++;
		}

		/**
		 * Returns the number of canceled processes.
		 *
		 * @return The number of canceled processes.
		 * @since 1.8
		 */
		public int getCanceled() {
			return canceled;
		}

		/**
		 * Increment by one the number of canceled processes.
		 * 
		 * @since 1.8
		 */
		public void incrementCanceled() {
			canceled++;
		}

		/**
		 * Returns the number of interrupted processes.
		 *
		 * @return The number of interrupted processes.
		 * @since 1.8
		 */
		public int getInterrupted() {
			return interrupted;
		}

		/**
		 * Increment by one the number of interrupted processes.
		 * 
		 * @since 1.8
		 */
		public void incrementInterrupted() {
			interrupted++;
		}
	}
}
