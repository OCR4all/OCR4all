/**
 * File:     BatchWorkflow.java
 * Package:  de.uniwue.batch
 * 
 * Author:   Herbert Baier
 * Date:     21.09.2020
 */
package de.uniwue.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.batch.process.AdjustmentType;
import de.uniwue.batch.process.ResultGenerationStrategy;
import de.uniwue.batch.process.ResultGenerationType;
import de.uniwue.batch.report.BatchProcessOverview;
import de.uniwue.batch.report.BatchWorkflowDetail;
import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;
import de.uniwue.helper.LineSegmentationHelper;
import de.uniwue.helper.OverviewHelper;
import de.uniwue.helper.PreprocessingHelper;
import de.uniwue.helper.RecognitionHelper;
import de.uniwue.helper.ResultGenerationHelper;
import de.uniwue.helper.SegmentationDummyHelper;

/**
 * Defines batch workflows.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class BatchWorkflow {
	/**
	 * The source image extension.
	 */
	public static final String sourceImageExtension = "png";

	/**
	 * The source image extension.
	 */
	private static final String segmentationImageType = "Binary";

	/**
	 * The preserve empty lines argument for result generation.
	 */
	public static final String resultGenerationPreserveEmptyLinesArgument = "--preserve-empty-lines";

	/**
	 * The backup argument for adjustment.
	 */
	public static final String adjustmentBackupArgument = "--backup";

	/**
	 * The ignore blank pages for adjustment.
	 */
	public static final String adjustmentIgnoreBlankPagesArgument = "--ignore-blank-pages";

	/**
	 * The dpi argument for adjustment.
	 */
	public static final String adjustmentDpiArgument = "--dpi";

	/**
	 * Defines workflow states.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public enum State {
		scheduled, running, completed, canceled, interrupted
	}

	/**
	 * Defines project folders.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public enum ProjectFolder {
		input, processing, results
	}

	/**
	 * The id.
	 */
	private final String id = UUID.randomUUID().toString();

	/**
	 * The state. The initial state is scheduled.
	 */
	private State state = State.scheduled;

	/**
	 * The configuration.
	 */
	private final WorkflowConfiguration configuration;

	/**
	 * The workflow process configurations. The key is the process identifier.
	 */
	private final Hashtable<String, WorkflowConfiguration.ProcessConfiguration> processConfigurations = new Hashtable<>();

	/**
	 * The project folder.
	 */
	private final String projectFolder;

	/**
	 * The page ids.
	 */
	private final List<String> pageIds = new ArrayList<String>();

	/**
	 * The collector for current state of processes.
	 */
	private final ProcessStateCollector processStateCollector;

	/**
	 * The process workers.
	 */
	private final LinkedList<ProcessWorker> processWorkers = new LinkedList<>();

	/**
	 * True if the workflow was canceled.
	 */
	private boolean isCanceled = false;

	/**
	 * The created time.
	 */
	private final Date created = new Date();

	/**
	 * The start time.
	 */
	private Date start = null;

	/**
	 * The finish time.
	 */
	private Date finish = null;

	/**
	 * Creates a batch workflow.
	 * 
	 * @param configuration The configuration.
	 * @throws IllegalArgumentException Throws on workflow configuration troubles.
	 * @throws IllegalStateException    Throws if the input folder of project does
	 *                                  not exist.
	 * @since 1.8
	 */
	public BatchWorkflow(WorkflowConfiguration configuration) throws IllegalArgumentException, IllegalStateException {
		super();

		WorkflowConfiguration.validate(configuration);

		String projectFolder = OverviewHelper.listProjects().get(configuration.getProject());
		this.projectFolder = projectFolder + (projectFolder.endsWith(File.separator) ? "" : File.separator);

		this.configuration = configuration;
		for (WorkflowConfiguration.ProcessConfiguration processConfiguration : this.configuration.getProcesses())
			processConfigurations.put(processConfiguration.getId(), processConfiguration);

		processStateCollector = new ProcessStateCollector(new ProjectConfiguration(this.projectFolder),
				this.configuration.getType().name());

		initialize(new File(this.projectFolder));
	}

	/**
	 * Initializes the batch workflow.
	 * 
	 * @param projectFolder The project folder.
	 * @throws IllegalStateException Throws if the input folder of project does not
	 *                               exist.
	 * @since 1.8
	 */
	private void initialize(File projectFolder) throws IllegalStateException {
		loadPageIds(projectFolder);

		// create processing folder if required
		File processingFolder = new File(projectFolder, ProjectFolder.processing.name());

		if (!processingFolder.exists())
			processingFolder.mkdir();
	}

	/**
	 * Loads the page ids from project input folder if batch configuration requires
	 * all pages, this means, its pages field is null. Furthermore, cross check if
	 * the project input folder is available. The pages ids will be sorted
	 * lexicographically, ignoring case differences.
	 * 
	 * @param projectFolder The project folder.
	 * @throws IllegalStateException Throws if the input folder of project does not
	 *                               exist.
	 * @since 1.8
	 */
	private void loadPageIds(File projectFolder) throws IllegalStateException {
		File inputFolder = new File(projectFolder, ProjectFolder.input.name());

		if (inputFolder.exists()) {
			pageIds.clear();

			if (configuration.getPages() == null) {
				for (File fileEntry : inputFolder.listFiles())
					if (fileEntry.isFile()
							&& FilenameUtils.getExtension(fileEntry.getName()).equals(sourceImageExtension))
						pageIds.add(FilenameUtils.removeExtension(fileEntry.getName()));
			} else
				for (String page : configuration.getPages())
					pageIds.add(page);
		} else
			throw new IllegalStateException(
					"the input folder of project '" + configuration.getProject() + "' does not exist.");

		Collections.sort(pageIds, String::compareToIgnoreCase);
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
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns true if the process is done.
	 * 
	 * @return True if the process is done.
	 * @since 1.8
	 */
	public boolean isDone() {
		switch (state) {
		case canceled:
		case completed:
		case interrupted:
			return true;

		case running:
		case scheduled:
		default:
			return false;
		}
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
	public Date getCreated() {
		return created;
	}

	/**
	 * Returns the start time.
	 *
	 * @return The start time.
	 * @since 1.8
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Returns the finish time.
	 *
	 * @return The finish time.
	 * @since 1.8
	 */
	public Date getFinish() {
		return finish;
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public WorkflowConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the page ids.
	 *
	 * @return The page ids.
	 * @since 1.8
	 */
	public List<String> getPageIds() {
		return pageIds;
	}

	/**
	 * Returns the running step. 0 if not started.
	 *
	 * @return The running step.
	 * @since 1.8
	 */
	public int getStep() {
		return processWorkers.size();
	}

	/**
	 * Returns the number of steps.
	 *
	 * @return The number of steps.
	 * @since 1.8
	 */
	public int getStepNumber() {
		return configuration.getWorkflow().size();
	}

	/**
	 * Returns the batch workflow details.
	 * 
	 * @return The batch workflow details.
	 * @since 1.8
	 */
	public BatchWorkflowDetail getDetails() {
		BatchWorkflowDetail details = new BatchWorkflowDetail(this);

		synchronized (processWorkers) {
			int step = 0;
			for (ProcessWorker processWorker : processWorkers)
				details.getSteps().add(new BatchProcessOverview(++step, processWorker));
		}

		return details;
	}

	/**
	 * Starts the workflow in a new thread if it is in scheduled state.
	 * 
	 * @param callback The callback method when the batch workflow finishes. If
	 *                 null, no callback is performed.
	 * @since 1.8
	 */
	public synchronized void start(Callback callback) {
		if (State.scheduled.equals(state)) {
			state = State.running;
			start = new Date();

			new Thread(new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					// Perform workflow
					for (String workflow : configuration.getWorkflow()) {
						WorkflowConfiguration.ProcessConfiguration processConfiguration = processConfigurations
								.get(workflow);
						switch (processConfiguration.getProcess()) {
						case adjustment:
							adjustment(processConfiguration);
							break;

						case preprocessing:
							preprocessing(processConfiguration);
							break;

						case segmentationDummy:
							segmentationDummy(processConfiguration);
							break;

						case lineSegmentation:
							lineSegmentation(processConfiguration);
							break;

						case recognition:
							recognition(processConfiguration);
							break;

						case resultGeneration:
							resultGeneration(processConfiguration);
							break;
						}

						if (isCanceled || !State.running.equals(state))
							break;
					}

					if (State.running.equals(state)) {
						if (isCanceled)
							state = State.canceled;
						else
							state = State.completed;
					}

					finish = new Date();

					if (callback != null)
						callback.finish(BatchWorkflow.this);
				}
			}).start();
		}
	}

	/**
	 * Cancels the workflow if it is in scheduled or running state.
	 * 
	 * @since 1.8
	 */
	public synchronized void cancel() {
		if (!isCanceled && !isDone()) {
			isCanceled = true;

			switch (state) {
			case scheduled:
				state = State.canceled;
				finish = new Date();

				break;
			case running:
				synchronized (processWorkers) {
					if (!processWorkers.isEmpty())
						processWorkers.getLast().getWrapper().cancelProcess();
				}

				break;
			default:
				break;
			}
		}
	}

	/**
	 * Performs the workflow adjustment.
	 * 
	 * @param processConfiguration The process configuration.
	 * @since 1.8
	 */
	private void adjustment(WorkflowConfiguration.ProcessConfiguration processConfiguration) {
		final OverviewHelper helper;
		final ProcessWorker worker;

		synchronized (processWorkers) {
			if (isCanceled)
				return;

			helper = new OverviewHelper(projectFolder, configuration.getType().name());

			// there are no page restrictions
			worker = new ProcessWorker(processConfiguration, (id) -> true, new ProcessWrapper() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getHandler()
				 */
				@Override
				public ProcessHandler getHandler() {
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getProgress()
				 */
				@Override
				public float getProgress() {
					try {
						int progress = helper.getProgress();
						return progress < 0 ? 0 : progress / 100F;
					} catch (Exception e) {
						return 0;
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#cancelProcess()
				 */
				@Override
				public void cancelProcess() {
					try {
						helper.cancelProcess();
					} catch (Exception e) {
						// Nothing to do
					}
				}
			});

			processWorkers.add(worker);
		}

		execute(worker, () -> {
			List<String> arguments = processConfiguration.getArguments();

			AdjustmentType type;
			int index = arguments.indexOf(AdjustmentType.argument);
			if (index >= 0 && index < arguments.size() - 1)
				try {
					type = AdjustmentType.valueOf(arguments.get(index + 1));
				} catch (Exception e) {
					throw new IllegalArgumentException("unknown adjustment type \"" + arguments.get(index + 1) + "\"");
				}
			else
				throw new IllegalArgumentException("missed adjustment type");

			boolean isFlag = false;
			switch (type) {
			case pdf:
				isFlag = arguments.contains(adjustmentIgnoreBlankPagesArgument);

				index = arguments.indexOf(adjustmentDpiArgument);
				if (index >= 0 && index < arguments.size() - 1)
					try {
						helper.setDPI(Integer.parseInt(arguments.get(index + 1)));
					} catch (Exception e) {
						throw new IllegalArgumentException("invalid adjustment dpi value \"" + arguments.get(index + 1)
								+ "\" - " + e.getMessage());
					}
				else
					throw new IllegalArgumentException("missed adjustment dpi");

				break;
			case image:
				isFlag = arguments.contains(adjustmentBackupArgument);

				break;
			}

			helper.execute(isFlag, AdjustmentType.pdf.equals(type));

			// update the page ids
			loadPageIds(new File(projectFolder));
		});
	}

	/**
	 * Performs the workflow preprocessing.
	 * 
	 * @param processConfiguration The process configuration.
	 * @since 1.8
	 */
	private void preprocessing(WorkflowConfiguration.ProcessConfiguration processConfiguration) {
		final PreprocessingHelper helper;
		final ProcessWorker worker;

		synchronized (processWorkers) {
			if (isCanceled)
				return;

			helper = new PreprocessingHelper(projectFolder, configuration.getType().name());

			worker = new ProcessWorker(processConfiguration, null, new ProcessWrapper() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getHandler()
				 */
				@Override
				public ProcessHandler getHandler() {
					return helper.getProcessHandler();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getProgress()
				 */
				@Override
				public float getProgress() {
					return helper.getProgress() / 100F;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#cancelProcess()
				 */
				@Override
				public void cancelProcess() {
					try {
						helper.cancelProcess();
					} catch (Exception e) {
						// Nothing to do
					}
				}
			});

			processWorkers.add(worker);
		}

		execute(worker, () -> helper.execute(worker.getAvailablePageIds(), processConfiguration.getArguments()));
	}

	/**
	 * Performs the workflow dummy segmentation.
	 * 
	 * @param processConfiguration The process configuration.
	 * @since 1.8
	 */
	private void segmentationDummy(WorkflowConfiguration.ProcessConfiguration processConfiguration) {
		final SegmentationDummyHelper helper;
		final ProcessWorker worker;

		synchronized (processWorkers) {
			if (isCanceled)
				return;

			helper = new SegmentationDummyHelper(projectFolder, configuration.getType().name());

			worker = new ProcessWorker(processConfiguration, (id) -> processStateCollector.preprocessingState(id),
					new ProcessWrapper() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getHandler()
						 */
						@Override
						public ProcessHandler getHandler() {
							return null;
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getProgress()
						 */
						@Override
						public float getProgress() {
							return helper.getProgress() / 100F;
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#cancelProcess()
						 */
						@Override
						public void cancelProcess() {
							try {
								helper.cancelProcess();
							} catch (Exception e) {
								// Nothing to do
							}
						}
					});

			processWorkers.add(worker);
		}

		execute(worker, () -> helper.execute(worker.getAvailablePageIds(), segmentationImageType));
	}

	/**
	 * Performs the workflow line segmentation.
	 * 
	 * @param processConfiguration The process configuration.
	 * @since 1.8
	 */
	private void lineSegmentation(WorkflowConfiguration.ProcessConfiguration processConfiguration) {
		final LineSegmentationHelper helper;
		final ProcessWorker worker;

		synchronized (processWorkers) {
			if (isCanceled)
				return;

			helper = new LineSegmentationHelper(projectFolder, configuration.getType().name());

			worker = new ProcessWorker(processConfiguration, (id) -> processStateCollector.segmentationState(id),
					new ProcessWrapper() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getHandler()
						 */
						@Override
						public ProcessHandler getHandler() {
							return helper.getProcessHandler();
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getProgress()
						 */
						@Override
						public float getProgress() {
							try {
								int progress = helper.getProgress();
								return progress < 0 ? 0 : progress / 100F;
							} catch (Exception e) {
								return 0;
							}
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#cancelProcess()
						 */
						@Override
						public void cancelProcess() {
							try {
								helper.cancelProcess();
							} catch (Exception e) {
								// Nothing to do
							}
						}
					});

			processWorkers.add(worker);
		}

		execute(worker, () -> helper.execute(worker.getAvailablePageIds(), processConfiguration.getArguments()));
	}

	/**
	 * Performs the workflow recognition.
	 * 
	 * @param processConfiguration The process configuration.
	 * @since 1.8
	 */
	private void recognition(WorkflowConfiguration.ProcessConfiguration processConfiguration) {
		final RecognitionHelper helper;
		final ProcessWorker worker;

		synchronized (processWorkers) {
			if (isCanceled)
				return;

			helper = new RecognitionHelper(projectFolder, configuration.getType().name());

			worker = new ProcessWorker(processConfiguration, (id) -> processStateCollector.lineSegmentationState(id),
					new ProcessWrapper() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getHandler()
						 */
						@Override
						public ProcessHandler getHandler() {
							return helper.getProcessHandler();
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getProgress()
						 */
						@Override
						public float getProgress() {
							try {
								int progress = helper.getProgress();
								return progress < 0 ? 0 : progress / 100F;
							} catch (Exception e) {
								return 0;
							}
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#cancelProcess()
						 */
						@Override
						public void cancelProcess() {
							try {
								helper.cancelProcess();
							} catch (Exception e) {
								// Nothing to do
							}
						}
					});

			processWorkers.add(worker);
		}

		execute(worker, () -> {
			TreeMap<String, String> models = RecognitionHelper.listModels();

			/*
			 * Set the models in format required by the recognition helper
			 */
			List<String> arguments = new ArrayList<>();
			StringBuffer buffer = null;
			for (String argument : processConfiguration.getArguments()) {
				/*
				 * If the buffer is non null, then we are currently scanning models.
				 */
				if (buffer != null) {
					// Ends model list
					if (argument.startsWith("--")) {
						if (buffer.length() == 0)
							throw new IllegalArgumentException("the argmuent --checkpoint requires at least one model");

						arguments.add(buffer.toString());

						buffer = null;
					} else {
						String model = models.get(argument);
						if (model == null)
							throw new IllegalArgumentException("unknown model \"" + argument + "\"");

						if (buffer.length() > 0)
							buffer.append(" ");

						buffer.append(model);
					}
				}

				if (buffer == null) {
					arguments.add(argument);

					if ("--checkpoint".equals(argument))
						buffer = new StringBuffer();
				}
			}

			if (buffer != null) {
				if (buffer.length() == 0)
					throw new IllegalArgumentException("the argmuent --checkpoint requires at least one model");

				arguments.add(buffer.toString());
			}

			helper.execute(worker.getAvailablePageIds(), arguments);
		});
	}

	/**
	 * Performs the workflow result generation.
	 * 
	 * @param processConfiguration The process configuration.
	 * @since 1.8
	 */
	private void resultGeneration(WorkflowConfiguration.ProcessConfiguration processConfiguration) {
		final ResultGenerationHelper helper;
		final ProcessWorker worker;

		synchronized (processWorkers) {
			if (isCanceled)
				return;

			helper = new ResultGenerationHelper(projectFolder, configuration.getType().name());

			// only the txt type uses a validor, the xml take all pages with extension xml
			worker = new ProcessWorker(processConfiguration, (id) -> processStateCollector.recognitionState(id),
					new ProcessWrapper() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getHandler()
						 */
						@Override
						public ProcessHandler getHandler() {
							return null;
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#getProgress()
						 */
						@Override
						public float getProgress() {
							try {
								int progress = helper.getProgress();
								return progress < 0 ? 0 : progress / 100F;
							} catch (Exception e) {
								return 0;
							}
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwue.batch.BatchWorkflow.ProcessWrapper#cancelProcess()
						 */
						@Override
						public void cancelProcess() {
							try {
								helper.cancelProcess();
							} catch (Exception e) {
								// Nothing to do
							}
						}
					});

			processWorkers.add(worker);
		}

		execute(worker, () -> {
			List<String> arguments = processConfiguration.getArguments();

			ResultGenerationType type;
			int index = arguments.indexOf(ResultGenerationType.argument);
			if (index >= 0 && index < arguments.size() - 1)
				try {
					type = ResultGenerationType.valueOf(arguments.get(index + 1));
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"unknown result generation type \"" + arguments.get(index + 1) + "\"");
				}
			else
				throw new IllegalArgumentException("missed result generation type");

			// strategy and preserve empty lines are only required for txt type
			ResultGenerationStrategy strategy = ResultGenerationStrategy.defaultStrategy;
			boolean isPreserveEmptyLines = false;
			if (ResultGenerationType.txt.equals(type)) {
				index = arguments.indexOf(ResultGenerationStrategy.argument);
				if (index >= 0) {
					if (index < arguments.size() - 1)
						try {
							strategy = ResultGenerationStrategy.valueOf(arguments.get(index + 1));
						} catch (Exception e) {
							throw new IllegalArgumentException(
									"unknown result generation strategy \"" + arguments.get(index + 1) + "\"");
						}
					else
						throw new IllegalArgumentException("missed result generation strategy");
				}

				isPreserveEmptyLines = arguments.contains(resultGenerationPreserveEmptyLinesArgument);
			}

			helper.executeProcess(worker.getAvailablePageIds(), type.name(), strategy.getProcessName(),
					isPreserveEmptyLines);
		});
	}

	/**
	 * Executes the process.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	private interface ExecuteProcess {
		/**
		 * Executes the process.
		 * 
		 * @throws Exception Throws on execution troubles.
		 * @since 1.8
		 */
		public void execute() throws Exception;
	}

	/**
	 * Executes the process for given worker.
	 * 
	 * @param worker         The process worker.
	 * @param executeProcess The process to execute.
	 * @since 1.8
	 */
	private void execute(ProcessWorker worker, ExecuteProcess executeProcess) {

		try {
			executeProcess.execute();

			if (isCanceled)
				worker.cancel();
			else if (worker.getWrapper().getProgress() < 1F) {
				worker.finish(true);
				worker.setMessage("only " + (int) (worker.getWrapper().getProgress() * 100F)
						+ "% of the complete process was performed");
				state = State.interrupted;
			} else
				worker.finish();
		} catch (Exception e) {
			state = State.interrupted;

			worker.setMessage(e.getMessage());
			worker.finish(true);

			e.printStackTrace();
		}

	}

	/**
	 * Defines callback.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface Callback {
		/**
		 * Callback method when the batch workflow finishes.
		 * 
		 * @param batchWorkflow The batch workflow that finish.
		 * @since 1.8
		 */
		public void finish(BatchWorkflow batchWorkflow);
	}

	/**
	 * Defines process page validators
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	private interface ProcessPageValidator {
		/**
		 * Returns true if the page is available for the process.
		 * 
		 * @param id The page id.
		 * @return True if the page is available for the process.
		 * @since 1.8
		 */
		public boolean isAvailable(String id);
	}

	/**
	 * Defines process wrappers.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public interface ProcessWrapper {
		/**
		 * Returns the handler. Null if not available.
		 * 
		 * @return The handler.
		 * @since 1.8
		 */
		public ProcessHandler getHandler();

		/**
		 * Returns the progress. This is a value between 0 and 1 inclusive.
		 * 
		 * @return The progress.
		 * @since 1.8
		 */
		public float getProgress();

		/**
		 * Cancels the process.
		 * 
		 * @since 1.8
		 */
		public void cancelProcess();
	}

	/**
	 * Defines process workers.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public class ProcessWorker {
		/**
		 * The configuration.
		 */
		private final WorkflowConfiguration.ProcessConfiguration configuration;

		/**
		 * The available page ids.
		 */
		private final List<String> availablePageIds = new ArrayList<>();

		/**
		 * The wrapper.
		 */
		private final ProcessWrapper wrapper;

		/**
		 * The process begin time.
		 */
		private final Date begin = new Date();

		/**
		 * The process end time. Null if running.
		 */
		private Date end = null;

		/**
		 * The message.
		 */
		private String message = null;

		/**
		 * True if process worker was canceled.
		 */
		private boolean isCanceled = false;

		/**
		 * True if there exist troubles performing process worker.
		 */
		private boolean isTrouble = false;

		/**
		 * Creates a process worker.
		 * 
		 * @param configuration The configuration.
		 * @param validator     The page validator.
		 * @param wrapper       The wrapper.
		 * @since 1.8
		 */
		public ProcessWorker(WorkflowConfiguration.ProcessConfiguration configuration, ProcessPageValidator validator,
				ProcessWrapper wrapper) {
			super();

			this.configuration = configuration;
			this.wrapper = wrapper;

			for (String pageId : BatchWorkflow.this.getPageIds())
				if (validator == null || validator.isAvailable(pageId))
					availablePageIds.add(pageId);
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
		 * Returns the wrapper.
		 *
		 * @return The wrapper.
		 * @since 1.8
		 */
		public ProcessWrapper getWrapper() {
			return wrapper;
		}

		/**
		 * Returns the process begin time.
		 *
		 * @return The process begin time.
		 * @since 1.8
		 */
		public Date getBegin() {
			return begin;
		}

		/**
		 * Returns the process end time. Null if running.
		 *
		 * @return The process end time.
		 * @since 1.8
		 */
		public Date getEnd() {
			return end;
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
		 * Cancels the process worker.
		 *
		 * @since 1.8
		 */
		public void cancel() {
			isCanceled = true;

			finish();
		}

		/**
		 * Finishes the process worker without troubles.
		 *
		 * @since 1.8
		 */
		public void finish() {
			finish(false);
		}

		/**
		 * Finishes the process worker.
		 *
		 * @param isTrouble True if there exist troubles performing this worker.
		 * @since 1.8
		 */
		public void finish(boolean isTrouble) {
			if (isRunning()) {
				end = new Date();

				this.isTrouble = isTrouble;
			}
		}

		/**
		 * Returns true if the process worker is running.
		 * 
		 * @return True if the process worker is running.
		 * @since 1.8
		 */
		public boolean isRunning() {
			return end == null;
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
		 * Set the message.
		 *
		 * @param message The message to set.
		 * @since 1.8
		 */
		public void setMessage(String message) {
			this.message = message;
		}

	}
}
