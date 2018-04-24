package test;

import com.incarcloud.concurrent.LimitedAsyncTask;
import com.incarcloud.concurrent.TaskTracking;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedAsyncTaskTest {
    @Test
    public void test5Seconds() throws Exception{
        ExecutorService pool = Executors.newFixedThreadPool(2);
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
                pool.submit(()->{
                    synchronized (atomCount) {
                        sbBuf.append(String.format("%4s", String.format("%X ", atomCount.incrementAndGet())));
                        if (atomCount.get() % 16 == 0){
                            System.out.println(sbBuf.toString());
                            sbBuf.delete(0, sbBuf.length());
                        }
                    }

                    if(atomCount.get() == 24){
                        throw new RuntimeException("硬注入异常测试B");
                    }

                    onFinished.run();
                });
                if(atomCount.get() > 14 & atomCount.get() < 17){
                    throw new RuntimeException("硬注入异常测试");
                }
            });
            Thread.sleep(10);

            if(i % 100 == 0) {
                List<TaskTracking> listLTT = asyncTask.scanForLongTimeTask(1000);
                if (listLTT != null) {
                    StringBuilder sbLTT = new StringBuilder();
                    sbLTT.append("Long time task:\n");
                    long tmNow = System.currentTimeMillis();
                    for (TaskTracking tracking : listLTT) {
                        float fT = (tmNow - tracking.tmBegin) / 1000.0f;
                        sbLTT.append(String.format("\t%4.2fs %s", fT, tracking.task));
                    }
                    System.out.println(sbLTT.toString());
                }
            }
        }

        System.out.println(sbBuf.toString());
    }
}
