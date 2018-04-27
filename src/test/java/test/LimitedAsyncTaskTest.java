package test;

import com.incarcloud.concurrent.LimitedAsyncTask;
import com.incarcloud.concurrent.PerfMetric;
import com.incarcloud.concurrent.TaskTracking;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedAsyncTaskTest {
    private static final Logger s_logger = LoggerFactory.getLogger(LimitedAsyncTaskTest.class);

    @Test
    public void test5Seconds() throws Exception{
        ExecutorService pool = Executors.newFixedThreadPool(8);
        LimitedAsyncTask asyncTask = new LimitedAsyncTask(pool);

        AtomicBoolean atomStop = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(()->{
            atomStop.set(true);
            asyncTask.stop();
            pool.shutdown();
        }, 5, TimeUnit.SECONDS);
        scheduler.shutdown();

        int i = 0;
        AtomicInteger atomCount = new AtomicInteger();
        StringBuilder sbBuf = new StringBuilder();
        while(!atomStop.get()){
            i++;
            asyncTask.submit((onFinished)->{
                atomCount.incrementAndGet();

                pool.submit(()->{
                    synchronized (atomCount) {
                        sbBuf.append(String.format("%4s", String.format("%X ", atomCount.get())));
                        if (atomCount.get() % 8 == 0){
                            s_logger.info(sbBuf.toString());
                            sbBuf.delete(0, sbBuf.length());
                        }
                    }

                    if(atomCount.get() == 24){
                        String strInject = "硬注入异常测试-导致长时间任务";
                        s_logger.info(strInject);
                        throw new RuntimeException(strInject);
                    }

                    try {
                        Thread.sleep(20);
                    }catch (Exception e){}

                    onFinished.run();
                });
            });
            Thread.sleep(10);

            if(i % 100 == 0) {
                printMetric(asyncTask);
            }
        }
    }

    @Test
    public void testDuplicateOnFinished() throws Exception{
        /**
         * 导致重复调用onFinished
         */
        Object objQuit  = new Object();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        LimitedAsyncTask asyncTask = new LimitedAsyncTask(pool);

        asyncTask.submit((onFinished)-> {
            pool.submit(() -> {
                onFinished.run();
                synchronized (objQuit){
                    objQuit.notify();
                }
            });

            throw new RuntimeException("硬注入异常测试-导致重复onFinished");
        });

        // 等待可以退出
        synchronized (objQuit){
            objQuit.wait();
        }
        asyncTask.stop();
        pool.shutdown();
    }

    private void printMetric(LimitedAsyncTask asyncTask){
        PerfMetric<Long> metric = asyncTask.queryPerfMetric();
        s_logger.info(LimitedTaskTest.printMetric(asyncTask));

        List<TaskTracking> listLTT = asyncTask.scanForLongTimeTask(1000);
        if (listLTT != null) {
            StringBuilder sbLTT = new StringBuilder();
            sbLTT.append("Long time task:\n");
            long tmNow = System.currentTimeMillis();
            for (TaskTracking tracking : listLTT) {
                float fT = (tmNow - tracking.getExecTM()) / 1000.0f;
                sbLTT.append(String.format("\t%4.2fs %s", fT, tracking.getTask()));
            }
            s_logger.info("{}\n", sbLTT.toString());
        }
    }
}
