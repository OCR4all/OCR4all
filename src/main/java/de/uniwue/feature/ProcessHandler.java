package de.uniwue.feature;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
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
    boolean ConsoleOutput = false;

    /**
     * Output  of the console if ConsoleOutput == true 
     */
    private List<InputStream> streams = new ArrayList<InputStream>();

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
        if (ConsoleOutput == true) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            executor.setStreamHandler(new PumpStreamHandler(os));
            executor.execute(cmdLine);
            streams.add(os.toInputStream());
        }
        else 
            executor.execute(cmdLine);
    }

    /**
     * Sets the ConsoleOutput
     */
    public void setConsoleOutput(boolean value) {
        ConsoleOutput = value;
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
     * Returns the InputStreams of the commandLine output
     *
     * @return Returns the InputStreams of the commandLine output
     */
    public List<InputStream> getStreams() {
        return streams;
    }
}
