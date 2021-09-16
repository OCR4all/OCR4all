/**
 * File:     WorkflowConfiguration.java
 * Package:  de.uniwue.batch
 *
 * Author:   Herbert Baier
 * Date:     22.09.2020
 */
package de.uniwue.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniwue.batch.process.AdjustmentType;
import de.uniwue.batch.process.Process;
import de.uniwue.batch.process.ResultGenerationStrategy;
import de.uniwue.batch.process.ResultGenerationType;
import de.uniwue.helper.OverviewHelper;
import de.uniwue.helper.RecognitionHelper;

/**
 * Defines batch workflow configurations.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class WorkflowConfiguration {
	/**
	 * Defines processing modes.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Processing {
		sequential, parallel
	}

	/**
	 * Defines project types.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Type {
		Binary, Gray
	}

	/**
	 * The project.
	 */
	private String project;

	/**
	 * The project type.
	 */
	private Type type;

	/**
	 * The processing mode.
	 */
	private Processing processing;

	/**
	 * The workflow. This means, the identifiers of the process configurations in
	 * the order in which they are to be processed.
	 */
	private List<String> workflow = new ArrayList<>();

	/**
	 * The project pages. Null if process on all pages.
	 */
	private List<String> pages = null;

	/**
	 * The process configurations.
	 */
	private List<ProcessConfiguration> processes = new ArrayList<>();

	/**
	 * Default constructor for a batch workflow configuration.
	 *
	 * @since 1.8
	 */
	public WorkflowConfiguration() {
		super();
	}

	/**
	 * Creates a batch workflow configuration.
	 *
	 * @param project    The project.
	 * @param type       The project type.
	 * @param processing The processing mode.
	 * @param workflow   The workflow, this means, the identifiers of the process
	 *                   configurations in the order in which they are to be
	 *                   processed.
	 * @since 1.8
	 */
	public WorkflowConfiguration(String project, Type type, Processing processing, String... workflow) {
		super();
		this.project = project;
		this.type = type;
		this.processing = processing;

		for (String step : workflow)
			this.workflow.add(step);
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
	 * Set the project.
	 *
	 * @param project The project to set.
	 * @since 1.8
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * Returns the project type.
	 *
	 * @return The project type.
	 * @since 1.8
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the project type.
	 *
	 * @param type The type to set.
	 * @since 1.8
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Returns the processing mode.
	 *
	 * @return The processing mode.
	 * @since 1.8
	 */
	public Processing getProcessing() {
		return processing;
	}

	/**
	 * Set the processing mode.
	 *
	 * @param processing The processing to set.
	 * @since 1.8
	 */
	public void setProcessing(Processing processing) {
		this.processing = processing;
	}

	/**
	 * Returns the workflow. This means, the identifiers of the process
	 * configurations in the order in which they are to be processed.
	 *
	 * @return The workflow.
	 * @since 1.8
	 */
	public List<String> getWorkflow() {
		return workflow;
	}

	/**
	 * Set the workflow. This means, the identifiers of the process configurations
	 * in the order in which they are to be processed.
	 *
	 * @param workflow The workflow to set.
	 * @since 1.8
	 */
	public void setWorkflow(List<String> workflow) {
		this.workflow = workflow;
	}

	/**
	 * Returns the project pages. Null if process on all pages.
	 *
	 * @return The project pages.
	 * @since 1.8
	 */
	public List<String> getPages() {
		return pages;
	}

	/**
	 * Set the project pages. Null if process on all pages.
	 *
	 * @param pages The pages to set.
	 * @since 1.8
	 */
	public void setPages(List<String> pages) {
		this.pages = pages;
	}

	/**
	 * Returns the process configurations.
	 *
	 * @return The process configurations.
	 * @since 1.8
	 */
	public List<ProcessConfiguration> getProcesses() {
		return processes;
	}

	/**
	 * Set the process configurations.
	 *
	 * @param processes The process configurations to set.
	 * @since 1.8
	 */
	public void setProcesses(List<ProcessConfiguration> processes) {
		this.processes = processes;
	}

	/**
	 * Returns the names of the enumeration values in csv format.
	 *
	 * @param <T>    The enumeration type.
	 * @param values The enumeration values.
	 * @return The names of the enumeration values in csv format.
	 * @since 1.8
	 */
	private static <T extends Enum<?>> String getCSV(T[] values) {
		StringBuffer buffer = new StringBuffer();
		for (T value : values) {
			if (buffer.length() > 0)
				buffer.append(", ");

			buffer.append(value.name());
		}

		return buffer.toString();
	}

	/**
	 * Validates the workflow configuration.
	 *
	 * @param configuration The workflow configuration.
	 * @throws IllegalArgumentException Throws on workflow configuration troubles.
	 * @since 1.8
	 */
	public static void validate(WorkflowConfiguration configuration) throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException("the batch workflow configuration has to be set.");

		if (configuration.getProject() == null)
			throw new IllegalArgumentException("the project is not set.");

		if (!OverviewHelper.listProjects().containsKey(configuration.getProject()))
			throw new IllegalArgumentException("the project '" + configuration.getProject() + "' is unknown.");

		if (configuration.getType() == null)
			throw new IllegalArgumentException("the project type is not set. Allowed types: " + getCSV(Type.values()));

		if (configuration.getProcessing() == null)
			throw new IllegalArgumentException(
					"the processing mode is not set. Allowed modes: " + getCSV(Processing.values()));

		if (configuration.getWorkflow() == null)
			throw new IllegalArgumentException("the workflow is not set.");

		if (configuration.getPages() != null)
			for (String page : configuration.getPages())
				if (page == null)
					throw new IllegalArgumentException("a page is not defined.");

		if (configuration.getProcesses() == null)
			throw new IllegalArgumentException("the process configurations are not set.");

		Set<String> processIds = new HashSet<>();
		Set<String> models;
		try {
			models = RecognitionHelper.listModels().keySet();
		} catch (IOException e) {
			throw new IllegalArgumentException("the models can not be read.");
		}

		for (ProcessConfiguration process : configuration.getProcesses())
			if (process == null)
				throw new IllegalArgumentException("a process configuration is not defined.");
			else if (process.getId() == null || process.getId().trim().length() == 0)
				throw new IllegalArgumentException("the identifier of a process configuration is not set.");
			else if (!processIds.add(process.getId()))
				throw new IllegalArgumentException(
						"duplicated process configuration for identifier '" + process.getId() + "'.");
			else {
				boolean isModelArgument = false;
				for (String argument : process.getArguments()) {
					if (argument == null)
						throw new IllegalArgumentException(
								"an argument of the process configurations '" + process.getId() + "' is not defined.");
					else if (Process.recognition.equals(process.getProcess()) && "--checkpoint".equals(argument))
						isModelArgument = true;
					else if (isModelArgument) {
						if (argument.startsWith("--"))
							isModelArgument = false;
						else if (!models.contains(argument))
							throw new IllegalArgumentException("the model '" + argument + "' is unknown.");
					}

				}
			}

		for (String processId : configuration.getWorkflow())
			if (processId == null)
				throw new IllegalArgumentException("a workflow identifier is not set.");
			else if (!processIds.contains(processId))
				throw new IllegalArgumentException(
						"no process configuration matches the workflow identifier '" + processId + "'.");

	}

	/**
	 * Returns a template for a batch workflow configuration.
	 *
	 * @return A template for a batch workflow configuration.
	 * @throws InternalError Throws on workflow configuration troubles.
	 * @since 1.8
	 */
	public static WorkflowConfiguration getTemplate() throws InternalError {
		WorkflowConfiguration configuration = new WorkflowConfiguration("<project>", Type.Binary, Processing.parallel,
				"step1", "step2", "step3", "step4", "step5", "step6");

		configuration.getProcesses().add(new ProcessConfiguration("step0/1", Process.adjustment,
				AdjustmentType.argument, AdjustmentType.image.name(), BatchWorkflow.adjustmentBackupArgument));

		configuration.getProcesses()
				.add(new ProcessConfiguration("step0/2", Process.adjustment, AdjustmentType.argument,
						AdjustmentType.pdf.name(), BatchWorkflow.adjustmentDpiArgument, "300",
						BatchWorkflow.adjustmentIgnoreBlankPagesArgument));

		configuration.getProcesses().add(new ProcessConfiguration("step1", Process.preprocessing, "--nocheck",
				"--maxskew", "0", "--parallel", "12"));

		configuration.getProcesses().add(new ProcessConfiguration("step2", Process.segmentationDummy));

		// TODO: line segmentation argument troubles
		// "--scale", "-1" OR "--scale", "0"
		// UNKNOWN "--tolerance", "1.0"
		//		configuration.getProcesses()
		//				.add(new ProcessConfiguration("step3", Process.lineSegmentation, "--usegauss", "--remove_images",
		//						"--maxcolseps", "-1", "--scale", "-1", "--tolerance", "1.0", "--parallel", "12", "--minscale",
		//						"12", "--maxlines", "300", "--scale", "0", "--hscale", "1", "--vscale", "1",
		//						"--filter_strength", "1", "--maxskew", "2.0", "--skewsteps", "8", "--threshold", "0.2",
		//						"--maxseps", "0", "--sepwiden", "10", "--csminheight", "10"));

		configuration.getProcesses().add(new ProcessConfiguration("step3", Process.lineSegmentation, "--usegauss",
				"--remove-images", "--maxcolseps", "-1", "--parallel", "12", "--minscale", "12", "--maxlines", "300",
				"--scale", "0", "--hscale", "1", "--vscale", "1", "--filter-strength", "1", "--maxskew", "2.0",
				"--skewsteps", "8", "--threshold", "0.2", "--maxseps", "0"));

		configuration.getProcesses()
				.add(new ProcessConfiguration("step4", Process.recognition, "--verbose", "--pagexml_word_level",
						"--estimate_skew", "--processes", "12", "--batch_size", "5", "--maxskew", "2.0", "--skewsteps",
						"8", "--checkpoint", "default/antiqua_historical", "--voter", "confidence_voter_default_ctc"));

		configuration.getProcesses().add(new ProcessConfiguration("step5", Process.resultGeneration,
				ResultGenerationType.argument, ResultGenerationType.txt.name(), ResultGenerationStrategy.argument,
				ResultGenerationStrategy.combine.name(), BatchWorkflow.resultGenerationPreserveEmptyLinesArgument));

		configuration.getProcesses().add(new ProcessConfiguration("step6", Process.resultGeneration,
				ResultGenerationType.argument, ResultGenerationType.xml.name()));

		return configuration;
	}

	/**
	 * Defines process configurations for batch workflows.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ProcessConfiguration {
		/**
		 * The process configuration identifier.
		 */
		private String id;

		/**
		 * The process.
		 */
		private Process process;

		/**
		 * The process arguments.
		 */
		private List<String> arguments = new ArrayList<>();

		/**
		 * Default constructor for a process configuration for batch workflow.
		 *
		 * @since 1.8
		 */
		public ProcessConfiguration() {
			super();
		}

		/**
		 * Creates a process configuration for batch workflow.
		 *
		 * @param id        The process configuration identifier.
		 * @param process   The process.
		 * @param arguments The process arguments.
		 * @since 1.8
		 */
		public ProcessConfiguration(String id, Process process, String... arguments) {
			super();
			this.id = id;
			this.process = process;

			for (String argument : arguments)
				if (argument != null)
					this.arguments.add(argument);
		}

		/**
		 * Returns the process configuration identifier.
		 *
		 * @return The process configuration identifier.
		 * @since 1.8
		 */
		public String getId() {
			return id;
		}

		/**
		 * Set the process configuration identifier.
		 *
		 * @param id The identifier to set.
		 * @since 1.8
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * Returns the process.
		 *
		 * @return The process.
		 * @since 1.8
		 */
		public Process getProcess() {
			return process;
		}

		/**
		 * Set the process.
		 *
		 * @param process The process to set.
		 * @since 1.8
		 */
		public void setProcess(Process process) {
			this.process = process;
		}

		/**
		 * Returns the process arguments.
		 *
		 * @return The process arguments.
		 * @since 1.8
		 */
		public List<String> getArguments() {
			return arguments;
		}

		/**
		 * Set the process arguments.
		 *
		 * @param arguments The arguments to set.
		 * @since 1.8
		 */
		public void setArguments(List<String> arguments) {
			if (arguments == null)
				this.arguments.clear();
			else
				this.arguments = arguments;
		}
	}
}
