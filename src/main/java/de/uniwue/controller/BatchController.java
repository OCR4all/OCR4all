/**
 * File:     BatchController.java
 * Package:  de.uniwue.controller
 * 
 * Author:   Herbert Baier
 * Date:     18.09.2020
 */
package de.uniwue.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.uniwue.batch.BatchWorkflow;
import de.uniwue.batch.WorkflowConfiguration;
import de.uniwue.batch.report.BatchScheduledOverview;
import de.uniwue.batch.report.BatchWorkflowDetail;
import de.uniwue.batch.report.BatchWorkflowOverview;
import de.uniwue.batch.report.ProcessManagerOverview;
import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessStateCollector;
import de.uniwue.helper.OverviewHelper;
import de.uniwue.helper.RecognitionHelper;

/**
 * Defines batch controllers.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
@Controller
@RequestMapping(value = "batch")
public class BatchController {
	/**
	 * The batch process manager.
	 */
	private final BatchProcessManager batchProcessManager;

	/**
	 * Creates a batch controller.
	 * 
	 * @param batchProcessManager The batch process manager.
	 * @since 1.8
	 */
	public BatchController(BatchProcessManager batchProcessManager) {
		super();

		this.batchProcessManager = batchProcessManager;
	}

	/**
	 * Returns the available project names.
	 * 
	 * @return The available project names.
	 * @since 1.8
	 */
	@RequestMapping(value = "/projects", method = RequestMethod.GET)
	public @ResponseBody List<String> getProjects() {
		List<String> projects = new ArrayList<>(OverviewHelper.listProjects().keySet());

		projects.sort(String::compareToIgnoreCase);

		return projects;
	}

	/**
	 * Returns the project overview.
	 * 
	 * @param name The project name.
	 * @param type The project type. It can be Binary or Gray. The default value is
	 *             Binary.
	 * @return
	 * @since 1.8
	 */
	@RequestMapping(value = "/project", method = RequestMethod.GET)
	public @ResponseBody ProjectOverview getProject(@RequestParam String name,
			@RequestParam(required = false) String type) {
		if (type == null)
			type = WorkflowConfiguration.Type.Binary.name();

		String projectFolder;
		ProcessStateCollector processStateCollector;

		try {
			projectFolder = OverviewHelper.listProjects().get(name);
			if (!projectFolder.endsWith(File.separator))
				projectFolder = projectFolder + File.separator;

			processStateCollector = new ProcessStateCollector(new ProjectConfiguration(projectFolder), type);

		} catch (Exception e) {
			throw new IllegalArgumentException("can not extract pages information - " + e.getMessage());
		}

		File inputFolder = new File(projectFolder, BatchWorkflow.ProjectFolder.input.name());
		if (!inputFolder.exists())
			throw new IllegalStateException("the input folder of project '" + name + "' does not exist.");

		ProjectOverview report = new ProjectOverview(name, type);

		for (final File fileEntry : Objects.requireNonNull(inputFolder.listFiles()))
			if (fileEntry.isFile()
					&& FilenameUtils.getExtension(fileEntry.getName()).equals(BatchWorkflow.sourceImageExtension))
				report.getPages().add(FilenameUtils.removeExtension(fileEntry.getName()));

		report.getPages().sort(String::compareToIgnoreCase);

		for (String page : report.getPages())
			report.getPageStates().add(new PageState(page, processStateCollector.preprocessingState(page),
					processStateCollector.segmentationState(page), processStateCollector.lineSegmentationState(page),
					processStateCollector.recognitionState(page)));

		return report;
	}

	/**
	 * Returns the available models.
	 * 
	 * @return The available models.
	 * @throws IOException Throws if can not recover the models.
	 * @since 1.8
	 */
	@RequestMapping(value = "/models", method = RequestMethod.GET)
	public @ResponseBody List<String> getModels() throws IOException {
		List<String> models = new ArrayList<>(RecognitionHelper.listModels().keySet());

		models.sort(String::compareToIgnoreCase);

		return models;
	}

	/**
	 * Returns a template for a batch workflow configuration.
	 * 
	 * @return A template for a batch workflow configuration
	 * @since 1.8
	 */
	@RequestMapping(value = "/template", method = RequestMethod.GET)
	public @ResponseBody WorkflowConfiguration getTemplate() {
		return WorkflowConfiguration.getTemplate();
	}

	/**
	 * Parses the batch workflow configuration and returns a report.
	 * 
	 * @param configuration The batch workflow configuration to be parsed.
	 * @return The parser report.
	 * @since 1.8
	 */
	@RequestMapping(value = "/parse", method = RequestMethod.POST)
	public @ResponseBody ParserReport parse(@RequestBody WorkflowConfiguration configuration) {
		return new ParserReport(configuration);
	}

	/**
	 * Returns the process manager overview.
	 * 
	 * @return The process manager overview.
	 * @since 1.8
	 */
	@RequestMapping(value = "/manager", method = RequestMethod.GET)
	public @ResponseBody ProcessManagerOverview getManagerOverview() {
		return batchProcessManager.getOverview();
	}

	/**
	 * Runs the process manager and returns its overview.
	 * 
	 * @return The process manager overview.
	 * @since 1.8
	 */
	@RequestMapping(value = "/manager/run", method = RequestMethod.GET)
	public @ResponseBody ProcessManagerOverview runManager() {
		return batchProcessManager.run();
	}

	/**
	 * Pauses the process manager and returns its overview.
	 * 
	 * @return The process manager overview.
	 * @since 1.8
	 */
	@RequestMapping(value = "/manager/pause", method = RequestMethod.GET)
	public @ResponseBody ProcessManagerOverview pauseManager() {
		return batchProcessManager.pause();
	}

	/**
	 * Returns the processes sorted by created time.
	 * 
	 * @return The processes sorted by created time.
	 * @since 1.8
	 */
	@RequestMapping(value = "/processes", method = RequestMethod.GET)
	public @ResponseBody List<BatchWorkflowOverview> getProcesses() {
		return batchProcessManager.getProcesses();
	}

	/**
	 * Returns the running processes sorted by start time.
	 * 
	 * @return The running processes sorted by start time.
	 * @since 1.8
	 */
	@RequestMapping(value = "/processes/running", method = RequestMethod.GET)
	public @ResponseBody List<BatchWorkflowOverview> getRunningProcesses() {
		return batchProcessManager.getRunningProcesses();
	}

	/**
	 * Returns the scheduled processes in the queue order.
	 * 
	 * @return The scheduled processes in the queue order.
	 * @since 1.8
	 */
	@RequestMapping(value = "/processes/scheduled", method = RequestMethod.GET)
	public @ResponseBody List<BatchScheduledOverview> getScheduledProcesses() {
		return batchProcessManager.getScheduledProcesses();
	}

	/**
	 * Returns the done processes sorted by finish time.
	 * 
	 * @return The done processes sorted by finish time.
	 * @since 1.8
	 */
	@RequestMapping(value = "/processes/done", method = RequestMethod.GET)
	public @ResponseBody List<BatchWorkflowOverview> getDoneProcesses() {
		return batchProcessManager.getDoneProcesses();
	}

	/**
	 * Returns the scheduled process.
	 * 
	 * @param id The scheduled process id.
	 * @return The process. Null if it is not current scheduled.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/scheduled/{id}", method = RequestMethod.GET)
	public @ResponseBody BatchScheduledOverview getScheduledProcess(@PathVariable String id) {
		return batchProcessManager.getScheduledProcess(id);
	}

	/**
	 * Schedules the batch process.
	 * 
	 * @param configuration The configuration.
	 * @return The scheduled process.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/schedule", method = RequestMethod.POST)
	public @ResponseBody BatchWorkflowOverview schedule(@RequestBody WorkflowConfiguration configuration) {
		return batchProcessManager.schedule(configuration);
	}

	/**
	 * Schedules the process to the begin of the scheduled queue.
	 * 
	 * @param id The process id.
	 * @return The scheduled processes.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/schedule/first/{id}", method = RequestMethod.GET)
	public @ResponseBody List<BatchScheduledOverview> scheduleFirst(@PathVariable String id) {
		return batchProcessManager.schedule(id, BatchProcessManager.Position.first);
	}

	/**
	 * Schedules the process to the end of the scheduled queue.
	 * 
	 * @param id The process id.
	 * @return The scheduled processes.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/schedule/last/{id}", method = RequestMethod.GET)
	public @ResponseBody List<BatchScheduledOverview> scheduleLast(@PathVariable String id) {
		return batchProcessManager.schedule(id, BatchProcessManager.Position.last);
	}

	/**
	 * Schedules the process to the index of the scheduled queue.
	 * 
	 * @param index The scheduled queue index.
	 * @param id    The process id.
	 * @return The scheduled processes.
	 * @throws IllegalArgumentException Throws if the project is unknown.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/schedule/{index}/{id}", method = RequestMethod.GET)
	public @ResponseBody List<BatchScheduledOverview> scheduleIndex(@PathVariable int index, @PathVariable String id) {
		return batchProcessManager.schedule(id, index);
	}

	/**
	 * Cancels the process.
	 * 
	 * @param id The process id.
	 * @return The process.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/cancel/{id}", method = RequestMethod.GET)
	public @ResponseBody BatchWorkflowOverview cancel(@PathVariable String id) {
		return batchProcessManager.cancel(id);
	}

	/**
	 * Returns the process details.
	 * 
	 * @param id The process id.
	 * @return The process details.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/details/{id}", method = RequestMethod.GET)
	public @ResponseBody BatchWorkflowDetail getDetails(@PathVariable String id) {
		return batchProcessManager.getDetails(id);
	}

	/**
	 * Returns the process configuration.
	 * 
	 * @param id The process id.
	 * @return The process configuration.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/configuration/{id}", method = RequestMethod.GET)
	public @ResponseBody WorkflowConfiguration getConfiguration(@PathVariable String id) {
		return batchProcessManager.getConfiguration(id);
	}

	/**
	 * Returns the process.
	 * 
	 * @param id The process id.
	 * @return The process.
	 * @since 1.8
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.GET)
	public @ResponseBody BatchWorkflowOverview getProcess(@PathVariable String id) {
		return batchProcessManager.getProcess(id);
	}

	/**
	 * Expunges the done processes.
	 * 
	 * @return The expunged process sorted by finish time.
	 * @since 1.8
	 */
	@RequestMapping(value = "/expunge", method = RequestMethod.GET)
	public @ResponseBody List<BatchWorkflowOverview> expunge() {
		return batchProcessManager.expunge();
	}

	/**
	 * Expunges the process if it is done.
	 * 
	 * @param id The process id.
	 * @return The process.
	 * @since 1.8
	 */
	@RequestMapping(value = "/expunge/{id}", method = RequestMethod.GET)
	public @ResponseBody BatchWorkflowOverview expunge(@PathVariable String id) {
		return batchProcessManager.expunge(id);
	}

	/**
	 * Handles client errors.
	 * 
	 * @param exception The exception.
	 * @since 1.8
	 */
	@ExceptionHandler({ HttpMessageNotReadableException.class, IllegalArgumentException.class })
	@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal service request, please verify the arguments")
	public void handleClientErrors(Exception exception) {
	}

	/**
	 * Handles server errors.
	 * 
	 * @param exception The exception.
	 * @since 1.8
	 */
	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal ORC project error")
	public void handleProjectErrors(Exception exception) {
	}

	/**
	 * Handles server errors.
	 * 
	 * @param exception The exception.
	 * @since 1.8
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal server error")
	public void handleServerErrors(Exception exception) {
	}

	/**
	 * Defines project overviews.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	private class ProjectOverview {
		/**
		 * The name.
		 */
		private final String name;

		/**
		 * The type.
		 */
		private final String type;

		/**
		 * The pages.
		 */
		private final List<String> pages = new ArrayList<>();

		/**
		 * The page states.
		 */
		private final List<PageState> pageStates = new ArrayList<>();

		/**
		 * Creates a project overviews.
		 * 
		 * @param name The name.
		 * @param type The type.
		 * @since 1.8
		 */
		public ProjectOverview(String name, String type) {
			super();
			this.name = name;
			this.type = type;
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}

		/**
		 * Returns the type.
		 *
		 * @return The type.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public String getType() {
			return type;
		}

		/**
		 * Returns the pages.
		 *
		 * @return The pages.
		 * @since 1.8
		 */
		public List<String> getPages() {
			return pages;
		}

		/**
		 * Returns the page states.
		 *
		 * @return The page states.
		 * @since 1.8
		 */
		public List<PageState> getPageStates() {
			return pageStates;
		}
	}

	/**
	 * PageState is an immutable class that defines page states.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	private class PageState {
		/**
		 * The identifier.
		 */
		private final String id;

		/**
		 * True if the page was preprocessed.
		 */
		private final boolean isPreprocessing;

		/**
		 * True if the page was segmented.
		 */
		private final boolean isSegmentation;

		/**
		 * True if the lines of the page were extracted.
		 */
		private final boolean isLineSegmentation;

		/**
		 * True if the lines of the page was recognized.
		 */
		private final boolean isRecognition;

		/**
		 * Creates a page state.
		 * 
		 * @param id                 The identifier.
		 * @param isPreprocessing    True if the page was preprocessed.
		 * @param isSegmentation     True if the page was segmented.
		 * @param isLineSegmentation True if the lines of the page were segmented.
		 * @param isRecognition      True if the lines of the page was recognized.
		 * @since 1.8
		 */
		public PageState(String id, boolean isPreprocessing, boolean isSegmentation, boolean isLineSegmentation,
				boolean isRecognition) {
			super();

			this.id = id;
			this.isPreprocessing = isPreprocessing;
			this.isSegmentation = isSegmentation;
			this.isLineSegmentation = isLineSegmentation;
			this.isRecognition = isRecognition;
		}

		/**
		 * Returns the identifier.
		 *
		 * @return The identifier.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public String getId() {
			return id;
		}

		/**
		 * Returns true if the page was preprocessed.
		 *
		 * @return True if the page was preprocessed.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public boolean isPreprocessing() {
			return isPreprocessing;
		}

		/**
		 * Returns true if the page was segmented.
		 *
		 * @return True if the page was segmented.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public boolean isSegmentation() {
			return isSegmentation;
		}

		/**
		 * Returns true if the lines of the page were segmented.
		 *
		 * @return True if the lines of the page were segmented.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public boolean isLineSegmentation() {
			return isLineSegmentation;
		}

		/**
		 * Returns true if the lines of the page was recognized.
		 *
		 * @return True if the lines of the page was recognized.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public boolean isRecognition() {
			return isRecognition;
		}
	}

	/**
	 * ParserReport ins an immutable class that defines parser reports for workflow
	 * configurations.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	private static class ParserReport {
		/**
		 * The status.
		 */
		private final String status;

		/**
		 * The message. Null if
		 */
		private final String message;

		/**
		 * The workflow configuration.
		 */
		private final WorkflowConfiguration configuration;

		/**
		 * Creates a parser reports for workflow configuration.
		 * 
		 * @param configuration The workflow configuration.
		 * @since 1.8
		 */
		public ParserReport(WorkflowConfiguration configuration) {
			super();

			this.configuration = configuration;

			Exception exception = null;
			try {
				WorkflowConfiguration.validate(configuration);
			} catch (Exception e) {
				exception = e;
			}

			if (exception == null) {
				status = "ok";
				message = null;
			} else {
				status = "error";
				message = exception.getMessage();
			}
		}

		/**
		 * Returns the status.
		 *
		 * @return The status.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public String getStatus() {
			return status;
		}

		/**
		 * Returns the message.
		 *
		 * @return The message.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public String getMessage() {
			return message;
		}

		/**
		 * Returns the configuration.
		 *
		 * @return The configuration.
		 * @since 1.8
		 */
		@SuppressWarnings("unused")
		public WorkflowConfiguration getConfiguration() {
			return configuration;
		}
	}

}
