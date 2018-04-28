package test;

import com.incarcloud.concurrent.LimitedSyncTask;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedSyncTaskTest {
    private static Logger s_logger = LoggerFactory.getLogger(LimitedSyncTaskTest.class);

    @Test
    public void test3Seconds() throws Exception{
        LimitedSyncTask syncTask = new LimitedSyncTask();

        AtomicBoolean atomStop = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(()->{
            atomStop.set(true);
        }, 3, TimeUnit.SECONDS);
        scheduler.shutdown();

        AtomicInteger atomCount = new AtomicInteger();
        StringBuilder sbBuf = new StringBuilder();
        while(!atomStop.get()){
            syncTask.submit(()->{
                synchronized (atomCount) {
                    sbBuf.append(String.format("%4d", atomCount.incrementAndGet()));
                    if (atomCount.get() % 8 == 0){
                        s_logger.info(sbBuf.toString());
                        sbBuf.delete(0, sbBuf.length());
                    }
                }
            });
            Thread.sleep(10);
        }

        syncTask.stop();
        s_logger.info(LimitedTaskTest.printMetric(syncTask));
    }

    @Test
    public void testHardInject(){
        /*
         * 测试同步任务中的异常不会导致执行停止
         * 设置最大并发为1，同步过程中抛出异常，如果未能正确处理，将会阻塞后续任务的执行
         */
        LimitedSyncTask syncTask = new LimitedSyncTask();
        syncTask.setMax(1);

        AtomicBoolean atomStop = new AtomicBoolean(false);

        AtomicInteger atomCount = new AtomicInteger();
        while(!atomStop.get()){
            syncTask.submit(()->{
                int n = atomCount.incrementAndGet();

                if(n == 21)
                    throw new RuntimeException("硬注入异常测试");
                else if(n >= 32)
                    atomStop.set(true);

            });
        }

        syncTask.stopASAP();
        Assert.assertTrue(syncTask.queryPerfMetric().getFinishedTask() >= 32);
        s_logger.info(LimitedTaskTest.printMetric(syncTask));
    }
}
