package de.uniwue.feature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
         * Consumer for further input handling
         */
        private Consumer<String> consumeInputLine;

        /**
         * Constructor
         *
         * @param inputStream Stream to read from
         * @param consumeInputLine Consumer to pass the stream content to
         */
        public StreamHandler(InputStream inputStream, Consumer<String> consumeInputLine) {
            this.inputStream = inputStream;
            this.consumeInputLine = consumeInputLine;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))){
                String nextLine = null;
                while ((nextLine = reader.readLine()) != null) {
                    consumeInputLine.accept(nextLine);
                }
            } catch (IOException e) {
                // InputStream is closed by terminating the underlying process
                // Terminate this Thread as well to avoid further usage
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Class used to catch when process is completed
     * Used when executing a process in background 
     */
    public class ProcessCompletionNotifier implements Runnable {
        /**
         * Process to watch
         */
        private Process process;

        /**
         * Consumer for further input handling
         */
        private Consumer<Process> consumeProcess;

        /**
         * Constructor
         *
         * @param process Process to watch
         * @param consumeProcess Consumer to pass the process to
         */
        public ProcessCompletionNotifier(Process process, Consumer<Process> consumeProcess) {
            this.process = process;
            this.consumeProcess = consumeProcess;
        }

        @Override
        public void run() {
            consumeProcess.accept(process);
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
    public ProcessHandler() {}

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
     * Extends existing console std.err with new content
     * Will be used as consumer by the StreamHandler 
     *
     * @param content New ontent of the process
     * @param type Type of console logging
     */
    private void filterAndAppendConsoleOutput(String content, String type) {
        // This is a makeshift solution to certain external Python scripts logging stdout / info to stderr in their latest version
        // While this isn't the most elegant solution it will be replaced in the upcoming rewrite of OCR4all anyways
        if(content.startsWith("INFO")){
            content = content.replaceFirst("(?:INFO\\s.*processing/)(.{4})/(.+):\\s(.*)", "Page: $1 | Line: $2 | Prediction: $3");
            content = content.replaceFirst("(?:INFO\\s.*calamari_ocr\\.scripts\\.predict:\\s)(.+)", "$1");
            type = "out";
        }else if(content.startsWith("WARNING:root:Torch version") || content.startsWith("(slice(")){
            type = "skip";
        }
        switch(type){
            case "out":
                this.consoleOut += content + System.lineSeparator();
                break;
            case "err":
                this.consoleErr += content + System.lineSeparator();
                break;
            case "skip":
                break;
        }

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
        // Reset process completion state in case of multiple usages of this instance
        processCompleted = false;

        List<String> command = new ArrayList<>();
        command.add(programPath);
        command.addAll(cmdArguments);
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Set Python specific environment variables
        // Could be done by function parameters as well, but most of the used scripts are written in Python
        // Therefore and due to the fact that these environment variables have no side effects, use it all processes
        Map<String, String> env = processBuilder.environment();
        // Set PYTHONUNBUFFERED environment variable to ensure continuous console output
        env.put("PYTHONUNBUFFERED", "1");
        // Set PYTHONIOENCODING environment variable to ensure successful execution of calamari scripts
        env.put("PYTHONIOENCODING", "utf-8");
        // SET AUTOGRAPH_VERBOSITY for TensorFlow2 execution in calamari.
        env.put("TF_CPP_MIN_LOG_LEVEL", "3");
        // SET PYTHONWARNINGS to ignore to stop e.g. scipy rounding warnings from showing up in the UI.
        env.put("PYTHONWARNINGS", "ignore");

        process = processBuilder.start();

        if (fetchProcessConsole) {
            // Execute stream handlers in new threads to be able to fetch stream contents continuously
            // Use Consumers to redirect stream contents to appropriate appending method
            new Thread(new StreamHandler(process.getInputStream(), (out) -> filterAndAppendConsoleOutput(out, "out"))).start();
            new Thread(new StreamHandler(process.getErrorStream(), (err) -> filterAndAppendConsoleOutput(err, "err"))).start();
        }

        if (runInBackground) {
            // Execute process in a new Thread with the help of a process notifier
            // To be able to get the process completion state the waitForProcessCompletion method is passed as consumer
            new Thread(new ProcessCompletionNotifier(process, this::waitForProcessCompletion)).start();
        }
        else {
            waitForProcessCompletion(process);
        }
    }

    /**
     * Stops the process
     */
    public void stopProcess() {
        process.destroy();
    }
}
