package test;

import com.incarcloud.concurrent.LimitedTask;
import com.incarcloud.concurrent.PerfMetric;

class LimitedTaskTest {
    static String printMetric(LimitedTask task){
        PerfMetric<Long> metric = task.queryPerfMetric();
        return String.format("\n-----perf-----\n" +
                        "Perf: %6.2f Hz | Running: %d | Waiting: %d | Finished: %d\n" +
                        "Running avg:%6.3fs | min:%4dms | max:%4dms\n" +
                        "Waiting avg:%6.3fs | min:%4dms | max:%4dms\n" +
                        "-----++++-----",
                task.queryPerf(),
                task.getRunning(),
                task.getWaiting(),
                metric.getFinishedTask(),

                metric.getPerfRunning().getAvg() / 1000.0f,
                metric.getPerfRunning().getMin(),
                metric.getPerfRunning().getMax(),

                metric.getPerfWaiting().getAvg() / 1000.0f,
                metric.getPerfWaiting().getMin(),
                metric.getPerfWaiting().getMax()
        );
    }
}
