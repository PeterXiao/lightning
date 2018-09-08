package uk.co.automatictester.lightning.core.state.tests;

import uk.co.automatictester.lightning.core.tests.base.AbstractTest;

import java.util.ArrayList;
import java.util.List;

import static uk.co.automatictester.lightning.core.enums.TestResult.*;

public class TestSet {

    private final List<AbstractTest> tests = new ArrayList<>();
    private final TestSetResults results = new TestSetResults();

    public void executeTests() {
        StringBuilder output = new StringBuilder();
        tests.forEach(test -> {
            test.execute();
            String testExecutionReport = test.getTestExecutionReport();
            output.append(testExecutionReport).append(System.lineSeparator());
        });
        String testExecutionReport = output.toString();
        results.setTestExecutionReport(testExecutionReport);
    }

    public String testExecutionReport() {
        return results.testExecutionReport();
    }

    public String testSetExecutionSummaryReport() {
        return results.testSetExecutionSummaryReport();
    }

    public int failCount() {
        return results.failCount();
    }

    public int errorCount() {
        return results.errorCount();
    }

    public boolean hasFailed() {
        return results.hasFailed();
    }

    public void add(AbstractTest test) {
        tests.add(test);
    }

    public void addAll(List<AbstractTest> test) {
        tests.addAll(test);
    }

    public List<AbstractTest> get() {
        return tests;
    }

    public int size() {
        return tests.size();
    }

    private class TestSetResults {
        private String testExecutionReport;

        void setTestExecutionReport(String testExecutionReport) {
            this.testExecutionReport = testExecutionReport;
        }

        String testExecutionReport() {
            return testExecutionReport;
        }

        boolean hasFailed() {
            return failCount() != 0 || errorCount() != 0;
        }

        String testSetExecutionSummaryReport() {
            return String.format("%n============= EXECUTION SUMMARY =============%n"
                            + "Tests executed:    %s%n"
                            + "Tests passed:      %s%n"
                            + "Tests failed:      %s%n"
                            + "Tests errors:      %s%n"
                            + "Test set status:   %s",
                    TestSet.this.size(),
                    passCount(),
                    failCount(),
                    errorCount(),
                    testSetStatus());
        }

        int failCount() {
            return (int) TestSet.this.get().stream()
                    .filter(t -> t.result().equals(FAIL))
                    .count();
        }

        int errorCount() {
            return (int) TestSet.this.get().stream()
                    .filter(t -> t.result().equals(ERROR))
                    .count();
        }

        private int passCount() {
            return (int) TestSet.this.get().stream()
                    .filter(t -> t.result().equals(PASS))
                    .count();
        }

        private String testSetStatus() {
            return hasFailed() ? FAIL.toString() : PASS.toString();
        }
    }
}