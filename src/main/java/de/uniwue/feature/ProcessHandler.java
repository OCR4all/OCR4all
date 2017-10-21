package de.uniwue.feature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class for process management
 */
public class ProcessHandler {
    /**
     * Class used to read streams of the process
     */
    public class StreamHandler implements Runnable {
        /**
         * Stream to read from (provided by the process)
         */
        private InputStream inputStream;

        /**
         * Consumer that holds content of the process stream
         */
        private Consumer<String> consumeInputLine;

        /**
         * Constructor
         *
         * @param inputStream Stream to read from
         * @param consumeInputLine Consumer to write to
         */
        public StreamHandler(InputStream inputStream, Consumer<String> consumeInputLine) {
            this.inputStream = inputStream;
            this.consumeInputLine = consumeInputLine;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
        }
    }

    /**
     * Class used to catch when process is completed
     * Used when executing a process in background 
     */
    public class ProcessCompletedNotifier implements Runnable {
        /**
         * Process to watch
         */
        private Process process;

        /**
         * Process handler object which needs to be notified
         */
        private ProcessHandler processHandler;

        /**
         * Constructor
         *
         * @param process Process to watch
         * @param processHandler Process handler object which needs to be notified
         */
        public ProcessCompletedNotifier(Process process, ProcessHandler processHandler) {
            this.process = process;
            this.processHandler = processHandler;
        }

        @Override
        public void run() {
            processHandler.waitForProcessCompletion(process);
        }
    }

    /**
     * Process object of the executed program
     */
    private Process process; 

    /**
     * Determines if the console of the process should be read
     */
    private boolean fetchProcessConsole = false;

    /**
     * Holds the console std.out of the process
     */
    private String consoleOut = "";

    /**
     * Holds the console std.err of the process
     */
    private String consoleErr = "";

    /**
     * Determines if the process is completed or not
     */
    private boolean processCompleted = false;

    /**
     * Constructor
     */
    public ProcessHandler() { }

    /**
     * Sets the state to determine the reading setting of the process std.out/std.err
     *
     * @param fetchProcessConsole Console fetch setting
     */
    public void setFetchProcessConsole(boolean fetchProcessConsole) {
        this.fetchProcessConsole = fetchProcessConsole;
    }

    /**
     * Returns the console std.out of the process
     *
     * @return Console output
     */
    public String getConsoleOut() {
        return consoleOut;
    }

    /**
     * Returns the console std.err of the process
     *
     * @return Console error
     */
    public String getConsoleErr() {
        return consoleErr;
    }

    /**
     * Extends existing console std.out with new content
     * Will be used as consumer by the StreamHandler 
     *
     * @param consoleOut New std.out content of the process
     */
    private void appendConsoleOutput(String consoleOut) {
        this.consoleOut += consoleOut + System.lineSeparator();
    }

    /**
     * Extends existing console std.err with new content
     * Will be used as consumer by the StreamHandler 
     *
     * @param consoleOut New std.err content of the process
     */
    private void appendConsoleError(String consoleErr) {
        this.consoleErr += consoleErr + System.lineSeparator();
    }

    /**
     * Gets if the process is completed or not
     *
     * @return Completed state of the process
     */
    public boolean isProcessCompleted() {
        return processCompleted;
    }

    /**
     * Waits for a completion of a given process
     *
     * @param process Process to wait for
     */
    public void waitForProcessCompletion(Process process) {
        try {
            // Wait till the process is completed
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Distribute complete information
        this.processCompleted = true;
    }

    /**
     * Starts the process
     *
     * @param programPath Path to the program to execute
     * @param cmdArguments Command line arguments for the program
     * @param runInBackground Determines if the process should be ran in background
     * @throws InterruptedException 
     * @throws IOException 
     */
    public void startProcess(String programPath, List<String> cmdArguments, boolean runInBackground)
            throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(programPath);
        command.addAll(cmdArguments);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        process = processBuilder.start();

        if (fetchProcessConsole == true) {
            // Read process streams and pass them to the created consumer functions
            Consumer<String> customOut = (out) -> appendConsoleOutput(out);
            Consumer<String> customErr = (err) -> appendConsoleError(err);
            StreamHandler outStreamHandler = new StreamHandler(process.getInputStream(), customOut);
            StreamHandler errStreamHandler = new StreamHandler(process.getErrorStream(), customErr);
            // Execute stream handlers in new threads to be able to fetch stream contents continuously
            new Thread(outStreamHandler).start();
            new Thread(errStreamHandler).start();
        }

        if (runInBackground) {
            new Thread(new ProcessCompletedNotifier(process, this)).start();
        }
        else {
            waitForProcessCompletion(process);
        }
    }

    /**
     * Stops the process
     */
    public void stopProcess() {
        // TODO: Interrupt threads and close Streams first
        process.destroy();
    }
}
