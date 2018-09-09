package uk.co.automatictester.lightning.core.reporters.ci;

import uk.co.automatictester.lightning.core.exceptions.JenkinsReportGenerationException;
import uk.co.automatictester.lightning.core.state.data.JmeterTransactions;
import uk.co.automatictester.lightning.core.state.tests.TestSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class JenkinsReporter {

    private TestSet testSet;
    private JmeterTransactions jmeterTransactions;

    private JenkinsReporter(TestSet testSet) {
        this.testSet = testSet;
    }

    private JenkinsReporter(JmeterTransactions jmeterTransactions) {
        this.jmeterTransactions = jmeterTransactions;
    }

    public static JenkinsReporter fromTestSet(TestSet testSet) {
        return new JenkinsReporter(testSet);
    }

    public static JenkinsReporter fromJmeterTransactions(JmeterTransactions jmeterTransactions) {
        return new JenkinsReporter(jmeterTransactions);
    }

    public void setJenkinsBuildName() {
        String fileContent = null;
        if (testSet != null) {
            fileContent = testSet.jenkinsSummaryReport();
        } else if (jmeterTransactions != null) {
            fileContent = jmeterTransactions.summaryReport();
        }
        writeJenkinsBuildNameSetterFile(fileContent);
    }

    private void writeJenkinsBuildNameSetterFile(String summary) {
        try (FileOutputStream fos = new FileOutputStream("lightning-jenkins.properties")) {
            Properties props = new Properties();
            props.setProperty("result.string", summary);
            OutputStreamWriter out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            props.store(out, "In Jenkins Build Name Setter Plugin, define build name as: ${BUILD_NUMBER} - ${PROPFILE,file=\"lightning-jenkins.properties\",property=\"result.string\"}");
        } catch (IOException e) {
            throw new JenkinsReportGenerationException(e);
        }
    }
}
