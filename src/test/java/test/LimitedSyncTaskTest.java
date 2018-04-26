package test;

import com.incarcloud.concurrent.LimitedSyncTask;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedSyncTaskTest {
    @Test
    public void test3Seconds() throws Exception{
        LimitedSyncTask syncTask = new LimitedSyncTask();

        AtomicBoolean atomStop = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(()->{
            atomStop.set(true);
            syncTask.stop();
        }, 3, TimeUnit.SECONDS);
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

                    if(atomCount.get() == 122){
                        throw new RuntimeException("硬注入异常测试");
                    }
                }
            });
            Thread.sleep(10);

        }

        System.out.println(sbBuf.toString());
    }
}
