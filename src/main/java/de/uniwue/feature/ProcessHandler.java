package de.uniwue.feature;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Class for managing a process
 */
public class ProcessHandler {
    /**
     * Object to execute external commands
     */
    private DefaultExecutor executor; 

    /**
     * Object, which observes the process
     */
    private ExecuteWatchdog watchdog;

    /**
     * Command Line of the process
     */
    CommandLine cmdLine;

    /**
     * Status of the Console output
     */
    boolean consoleOutput = false;

    /**
     * Output  of the console if ConsoleOutput == true 
     */
    private List<InputStream> outStreams = new ArrayList<InputStream>();

    /**
     * Output  of the console if ConsoleOutput == true 
     */
    private List<InputStream> errStreams = new ArrayList<InputStream>();

    /**
     * Err output of the console
     */
    private String errOutput = "";

    /**
     * Constructor
     */
    public ProcessHandler() {
        executor = new DefaultExecutor();
        // Exitcode 143 added to avoid, when the process is canceled that an error is thrown
        executor.setExitValues(new int[] { 0, 1, 143 });
        watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
    }

    /**
     * Sets the Command Line
     */
    public void setCommandLine(String binary, List<String> args) {
        cmdLine = new CommandLine(binary);
        for(String arg : args) {
            cmdLine.addArgument(arg);
        }
    }

    /**
     * Starts the process
     */
    public void start() throws ExecuteException, IOException {
        errOutput = "";
        if (consoleOutput == true) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            executor.setStreamHandler(new PumpStreamHandler(out,error));
            executor.execute(cmdLine);
            outStreams.add(out.toInputStream());
            errStreams.add(error.toInputStream());
        }
        else 
            executor.execute(cmdLine);
    }

    /**
     * Sets the ConsoleOutput
     */
    public void setConsoleOutput(boolean value) {
        consoleOutput = value;
    }

    /**
     * Cancels the process
     */
    public boolean stop() {
        if (watchdog.isWatching()) {
            watchdog.destroyProcess();
           return true;
        }
        return false;
    }

    /**
     * Returns the Error of the commandLine as an InputStream
     *
     * @return Returns the InputStreams of the commandLine output
     */
    public List<InputStream> getErrStreams() {
        return errStreams;
    }

    /**
     * Returns the Output of the commandLine as an InputStream
     *
     * @return Returns the InputStreams of the commandLine output
     */
    public List<InputStream> getOutStreams() {
        return outStreams;
    }

    /**
     * Returns the Output of the commandLine as an InputStream
     *
     * @return Returns the InputStreams of the commandLine output
     * @throws IOException 
     */
    public String getErrString() throws IOException {
        InputStream input = new SequenceInputStream(Collections.enumeration(errStreams));
        errOutput = errOutput + IOUtils.toString(input, "UTF-8");
        return errOutput;
    }
}
