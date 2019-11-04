package com.oopsconsultancy.xmltask.test;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apache.tools.ant.FileUtilities.getFileContents;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class TestXmlTask {
    // Default is for running tests from IDE
    public static final String TEST_DIRECTORY = System.getProperty("project.test.workingDirectory",
            "src/test/resources") + "/current/scripts/";
    public static final File TEMP_DIRECTORY = new File(System.getProperty("user.dir"), "temp");

    @Rule
    public BuildFileRule buildRule = new BuildFileRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws IOException {
        File log4jDtd = new File(System.getProperty("user.dir"), "log4j.dtd");
        if (!log4jDtd.exists()) {
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            try {
                sourceChannel = new FileInputStream(new File(TEST_DIRECTORY, "log4j.dtd")).getChannel();
                destChannel = new FileOutputStream(log4jDtd).getChannel();
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } finally {
                sourceChannel.close();
                destChannel.close();
            }
        }
        if (!TEMP_DIRECTORY.exists()) {
            TEMP_DIRECTORY.mkdir();
        }
    }

    @After
    public void cleanUp() {
        for (File f : TEMP_DIRECTORY.listFiles()) {
            f.delete();
        }
    }

    @Test
    @Parameters(method = "parametersToXmlTaskTest")
    public void testXmlTask(String testBuildfile, String out) throws IOException {
        buildRule.configureProject(TEST_DIRECTORY + testBuildfile);
        String message = null;
        switch (extractInt(testBuildfile)) {
            case 88: {
                buildRule.executeTarget("test");
                buildRule.executeTarget("test");
                break;
            }
            case 87: {
                message = "FAIL";
                // fall through
            }
            case 81: {
                if (message == null) {
                    message = "Cannot use multiple buffer nodes as an input source";
                }
                // fall through
            }
            case 62: {
                if (message == null) {
                    message = "<xmltask> subtasks failed to find matches";
                }
                thrown.expect(BuildException.class);
                thrown.expectMessage(message);
                // fall through
            }
            default: {
                buildRule.executeTarget("main");
            }
        }
        if (!out.equals("")) {
            String result = getFileContents(buildRule.getProject(), out);
            String refDir = (JavaEnvUtils.isAtLeastJavaVersion(JavaEnvUtils.JAVA_9)
                    && buildRule.getProject().resolveFile("result9/" + out).canRead())
                    ? "result9/" : "results/";
            String reference = getFileContents(buildRule.getProject(), refDir + out);
            assertEquals(reference, result);
            // required by test 75
            buildRule.getProject().resolveFile(out).delete();
        }
    }

    private List<String[]> parametersToXmlTaskTest() {
        List<String[]> cases = new ArrayList<String[]>();
        List<String> files = Arrays.asList(new File(TEST_DIRECTORY).list());
        Collections.sort(files, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }
        });
        for (final String f : files) {
            if (f.startsWith("build")) {
                String o = f.replace("build-", "").replace(".xml", "-out.xml");
                cases.add(new String[]{f, new File(TEST_DIRECTORY + "results", o).exists() ? o : ""});
            }
        }
        return cases;
    }

    private int extractInt(String s) {
        String num = s.replaceAll("\\D", "");
        // return 0 if no digits found
        return num.equals("") ? 0 : Integer.parseInt(num);
    }

}
