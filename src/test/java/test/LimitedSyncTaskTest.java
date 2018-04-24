package test;

import com.incarcloud.concurrent.LimitedSyncTask;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedSyncTaskTest {
    @Test
    public void test5Seconds() throws Exception{
        ExecutorService pool = Executors.newFixedThreadPool(2);
        LimitedSyncTask syncTask = new LimitedSyncTask(pool);

        AtomicBoolean atomStop = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(()->{
            atomStop.set(true);
            syncTask.stop();
            pool.shutdown();
        }, 5, TimeUnit.SECONDS);
        scheduler.shutdown();

        AtomicInteger atomCount = new AtomicInteger();
        StringBuilder sbBuf = new StringBuilder();
        while(!atomStop.get()){
            syncTask.submit(()->{
                synchronized (atomCount) {
                    sbBuf.append(String.format("%4d", atomCount.incrementAndGet()));
                    if (atomCount.get() % 16 == 0){
                        System.out.println(sbBuf.toString());
                        sbBuf.delete(0, sbBuf.length());
                    }
                }
            });
            Thread.sleep(10);
        }

        System.out.println(sbBuf.toString());
    }
}
