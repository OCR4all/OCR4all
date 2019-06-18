package de.uniwue.feature;

import java.util.List;

/**
 * Class to determine if two processes stand in conflict with each other
 * This helps to decide if they can be executed in parallel without any issues
 */
public class ProcessConflictDetector {
    /**
     * Different types of conflicts that can occur
     */
    public static final int NO_CONFLICT       = 0;
    public static final int SAME_PROCESS      = 1;
    public static final int SAME_PROCESS_TYPE = 2;
    public static final int PREV_PROCESS      = 3;
    public static final int PROCESS_FLOW      = 4;

    /**
     * Determines conflicts with any Segmentation process
     *
     * @param currentProcesses Processes that are currently running
     * @param segmentationProcess Indicates if the check is performed for a segmentation process
     * @return Type of process conflict
     */
    private static int segmentationConflict(List<String> currentProcesses, boolean segmentationProcess) {
        for (String currentProcess : currentProcesses) {
            // Return different types of conflict for segmentation process and following processes
            if (currentProcess.startsWith("segmentation"))
                return (segmentationProcess ? SAME_PROCESS_TYPE : PREV_PROCESS);
        }

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the Preprocessing process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int preprocessingConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("preprocessing"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        if (currentProcesses.contains("despeckling"))
            return PREV_PROCESS;

        int segmentationConflictType = segmentationConflict(currentProcesses, true);
        if (segmentationConflictType != NO_CONFLICT)
            return segmentationConflictType;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the Despeckling process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int despecklingConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("despeckling"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        int segmentationConflictType = segmentationConflict(currentProcesses, true);
        if (segmentationConflictType != NO_CONFLICT)
            return segmentationConflictType;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the SegmentationLarex process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public static int larexConflict(List<String> currentProcesses) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("larex"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow"))
            return PROCESS_FLOW;

        int segmentationConflictType = segmentationConflict(currentProcesses, true);
        if (segmentationConflictType != NO_CONFLICT)
            return segmentationConflictType;

        if (currentProcesses.contains("regionExtraction")
                || currentProcesses.contains("lineSegmentation")
                || currentProcesses.contains("recognition")
                || currentProcesses.contains("evaluation")
                || currentProcesses.contains("result"))
            return PREV_PROCESS;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the SegmentationDummy process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int segmentationDummyConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("segmentationDummy"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        int segmentationConflictType = segmentationConflict(currentProcesses, true);
        if (segmentationConflictType != NO_CONFLICT)
            return segmentationConflictType;

        if (currentProcesses.contains("regionExtraction")
                || currentProcesses.contains("lineSegmentation")
                || currentProcesses.contains("recognition")
                || currentProcesses.contains("evaluation")
                || currentProcesses.contains("result"))
            return PREV_PROCESS;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the SegmentationPixelClassifier process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int segmentationPixelClassifierConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("segmentationPixelClassifier"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        int segmentationConflictType = segmentationConflict(currentProcesses, true);
        if (segmentationConflictType != NO_CONFLICT)
            return segmentationConflictType;

        if (currentProcesses.contains("regionExtraction")
                || currentProcesses.contains("lineSegmentation")
                || currentProcesses.contains("recognition")
                || currentProcesses.contains("evaluation")
                || currentProcesses.contains("result"))
            return PREV_PROCESS;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the RegionExtraction process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int regionExtractionConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("regionExtraction"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        if (currentProcesses.contains("lineSegmentation")
                || currentProcesses.contains("recognition")
                || currentProcesses.contains("evaluation")
                || currentProcesses.contains("result"))
            return PREV_PROCESS;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the LineSegmentation process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int lineSegmentationConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("lineSegmentation"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        if (currentProcesses.contains("recognition")
                || currentProcesses.contains("evaluation")
                || currentProcesses.contains("result"))
            return PREV_PROCESS;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the Recognition process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public static int recognitionConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("recognition"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow") && !inProcessFlow)
            return PROCESS_FLOW;

        if (currentProcesses.contains("evaluation")
                || currentProcesses.contains("result"))
            return PREV_PROCESS;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the Evaluation process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public static int evaluationConflict(List<String> currentProcesses) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("evaluation"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow"))
            return PROCESS_FLOW;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the Evaluation process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public static int resultConflict(List<String> currentProcesses) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("result"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow"))
            return PROCESS_FLOW;

        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the Training process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
	public static int trainingConflict(List<String> currentProcesses, boolean inProcessFlow) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("training"))
            return SAME_PROCESS;

        if (currentProcesses.contains("processFlow"))
            return PROCESS_FLOW;
        return NO_CONFLICT;
    }

    /**
     * Determines conflicts with the ProcessFlow process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public static int processFlowConflict(List<String> currentProcesses) {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;

        if (currentProcesses.contains("processFlow"))
            return SAME_PROCESS;

        // No other process should be executed
        return PREV_PROCESS;
    }
}
