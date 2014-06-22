package io.magnum.awscommons.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * A simple utility class to execute an external process based on
 * Apache Commons Exec {@link http://commons.apache.org/exec/}
 *
 * @author Yu Sun
 */
public class Exec {

    private static Logger log = Logger.getLogger(Exec.class.getName());

    /** The default success code of executing a command */
    private static final int DEFAULT_SUCCESS_CDOE = 0;
    /** The default timeout of executing a command */
    private static final int DEFAULT_TIMEOUT = 1 * 60 * 1000; // 3 min
    /** The command line to execute, consisting of a base command and a set of arguments */
    private final String command;
    /** The expected success code of executing a command */
    private final int successCode;
    /** The timeout of executing a command */
    private final int timeout;

    public Exec(String command) {
        this(command, DEFAULT_SUCCESS_CDOE, DEFAULT_TIMEOUT);
    }

    public Exec(String command, int successCode) {
        this(command, successCode, DEFAULT_TIMEOUT);
    }

    public Exec(String command, int successCode, int timeout) {
        this.command = command;
        this.successCode = successCode;
        this.timeout = timeout;
    }

    /**
     * Execute the command using Apache Commons Exec.
     */
    public void execute() throws ExecuteException, IOException {
        CommandLine line = CommandLine.parse(command);
        DefaultExecutor runner = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        runner.setWatchdog(watchdog);
        runner.setExitValue(successCode);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        runner.setStreamHandler(new PumpStreamHandler(out, err));
        try {
            runner.execute(line);
        } catch (ExecuteException e) {
            log.warning("Failed to run " + command);
            throw e;
        } catch (IOException e) {
            log.warning("Failed to run " + command);
            throw e;
        } finally {
            final String output = new String(out.toByteArray(), "UTF-8");
            if (!output.isEmpty()) {
                log.info("STDOUT: " + output);
            }
            final String error = new String(err.toByteArray(), "UTF-8");
            if (!error.isEmpty()) {
                log.warning("STDERR: " + error);
            }
        }
    }
}
