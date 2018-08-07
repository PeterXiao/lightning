package uk.co.automatictester.lightning.ci;

import uk.co.automatictester.lightning.TestSet;
import uk.co.automatictester.lightning.data.JMeterTransactions;

public abstract class CIReporter {

    protected TestSet testSet;
    protected JMeterTransactions jmeterTransactions;

    protected CIReporter(TestSet testSet) {
        this.testSet = testSet;
    }

    protected CIReporter(JMeterTransactions jmeterTransactions) {
        this.jmeterTransactions = jmeterTransactions;
    }

    public String getReportSummary() {
        int executed = jmeterTransactions.size();
        int failed = jmeterTransactions.getFailCount();
        return String.format("Transactions executed: %s, failed: %s", executed, failed);
    }

}