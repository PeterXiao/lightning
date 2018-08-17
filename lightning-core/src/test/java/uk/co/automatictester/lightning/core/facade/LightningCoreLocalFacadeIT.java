package uk.co.automatictester.lightning.core.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uk.co.automatictester.lightning.core.enums.Mode;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class LightningCoreLocalFacadeIT extends FileAndOutputComparisonIT {

    private static final Logger log = LoggerFactory.getLogger(LightningCoreLocalFacadeIT.class);

    private LightningCoreLocalFacade core = new LightningCoreLocalFacade();
    private Mode mode;
    private File perfMonCsv;
    private File jmeterCsv;
    private File lightningXml;
    private int exitCode;
    private int expectedExitCode;
    private String expectedJunitReport;
    private String expectedConsoleOutput;

    @BeforeMethod
    public void setup() {
        configureStream();
    }

    @AfterMethod
    public void teardown() {
        revertStream();
    }

    @DataProvider(name = "testData")
    private Object[][] testData() {
        return new Object[][]{
                {Mode.report, null, new File("src/test/resources/csv/jmeter/10_transactions.csv"), null, 0, null, "/results/expected/report.txt"},
                {Mode.report, null, new File("src/test/resources/csv/jmeter/2_transactions.csv"), null, 0, null, null},
                {Mode.report, null, new File("src/test/resources/csv/jmeter/2_transactions_1_failed.csv"), null, 1, null, null},
                {Mode.verify, new File("src/test/resources/xml/1_1_1.xml"), new File("src/test/resources/csv/jmeter/10_transactions.csv"), null, 1, null, "/results/expected/1_1_1.txt"},
                {Mode.verify, new File("src/test/resources/xml/3_0_0.xml"), new File("src/test/resources/csv/jmeter/10_transactions.csv"), null, 0, null, "/results/expected/3_0_0.txt"},
                {Mode.verify, new File("src/test/resources/xml/1_client_2_server.xml"), new File("src/test/resources/csv/jmeter/10_transactions.csv"), new File("src/test/resources/csv/perfmon/2_entries.csv"), 0, null, "/results/expected/1_client_2_server.txt"},
                {Mode.verify, new File("src/test/resources/xml/junit_report.xml"), new File("src/test/resources/csv/jmeter/2_transactions.csv"), new File("src/test/resources/csv/perfmon/junit_report.csv"), 1, "/results/expected/junit/junit_expected.xml", null}
        };
    }

    @Test(dataProvider = "testData")
    public void nothingIT(Mode mode, File lightningXml, File jmeterCsv, File perfMonCsv, int expectedExitCode, String expectedJunitReport, String expectedConsoleOutput) throws IOException {
        this.exitCode = 0;
        this.mode = mode;
        this.lightningXml = lightningXml;
        this.jmeterCsv = jmeterCsv;
        this.perfMonCsv = perfMonCsv;
        this.expectedExitCode = expectedExitCode;
        this.expectedJunitReport = expectedJunitReport;
        this.expectedConsoleOutput = expectedConsoleOutput;

        run();
        assertExitCode();
        assertConsoleOutput();
        assertJunitReport();
    }

    private void run() {
        core.setJmeterCsv(jmeterCsv);
        core.setPerfMonCsv(perfMonCsv);
        core.loadTestData();

        switch (mode) {
            case verify:
                runTests();
                core.saveJunitReport();
                break;
            case report:
                runReport();
                break;
        }
        notifyCIServer();
    }

    private void runTests() {
        long testSetExecStart = System.currentTimeMillis();

        core.setLightningXml(lightningXml);
        core.loadConfig();

        String testExecutionReport = core.executeTests();
        log(testExecutionReport);

        String testSetExecutionSummaryReport = core.getTestSetExecutionSummaryReport();
        log(testSetExecutionSummaryReport);

        long testSetExecEnd = System.currentTimeMillis();
        long testExecTime = testSetExecEnd - testSetExecStart;
        String message = String.format("Execution time:    %dms", testExecTime);
        log(message);

        if (core.hasExecutionFailed()) {
            exitCode = 1;
        }
    }

    private void runReport() {
        String report = core.runReport();
        log(report);
        if (core.hasFailedTransactions()) {
            exitCode = 1;
        }
    }

    private void notifyCIServer() {
        switch (mode) {
            case verify:
                String teamCityVerifyStatistics = core.getTeamCityVerifyStatistics();
                log(teamCityVerifyStatistics);
                core.setJenkinsBuildNameForVerify();
                break;
            case report:
                String teamCityBuildReportSummary = core.getTeamCityBuildReportSummary();
                log(teamCityBuildReportSummary);
                String teamCityReportStatistics = core.getTeamCityReportStatistics();
                log(teamCityReportStatistics);
                core.setJenkinsBuildNameForReport();
                break;
        }
    }

    private void log(String text) {
        for (String line : text.split(System.lineSeparator())) {
            System.out.println(line);
        }
    }

    private void assertExitCode() {
        assertThat(exitCode, is(equalTo((expectedExitCode))));
    }

    private void assertConsoleOutput() {
        if (expectedConsoleOutput != null) {
            assertThat(taskOutputContainsFileContent(expectedConsoleOutput), is(true));
        }
    }

    private void assertJunitReport() throws IOException {
        if (expectedJunitReport != null) {
            assertThat(fileContentIsEqual(expectedJunitReport, "junit.xml"), is(true));
        }
    }
}