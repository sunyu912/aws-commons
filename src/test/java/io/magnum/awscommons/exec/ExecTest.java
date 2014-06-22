package io.magnum.awscommons.exec;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.junit.Test;

public class ExecTest {

    @Test
    public void testExecute() throws ExecuteException, IOException {
        new Exec("echo echotest").execute();
    }

    @Test
    public void testNonZeroExitCode() throws ExecuteException, IOException {
        new Exec("false", 1).execute();
    }

    @Test(expected=IOException.class)
    public void textException() throws ExecuteException, IOException {
        new Exec("/this/will/cause/an/exception").execute();
    }
}
