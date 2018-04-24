package test;

import com.incarcloud.concurrent.LimitedAsyncTask;
import org.junit.Test;

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

        AtomicInteger atomCount = new AtomicInteger();
        StringBuilder sbBuf = new StringBuilder();
        while(!atomStop.get()){
            asyncTask.submit((onFinished)->{
                pool.submit(()->{
                    synchronized (atomCount) {
                        sbBuf.append(String.format("%4s", String.format("%X ", atomCount.incrementAndGet())));
                        if (atomCount.get() % 16 == 0){
                            System.out.println(sbBuf.toString());
                            sbBuf.delete(0, sbBuf.length());
                        }
                    }
                    onFinished.run();
                });
            });
            Thread.sleep(10);
        }

        System.out.println(sbBuf.toString());
    }
}
