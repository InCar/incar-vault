package test;

import com.incarcloud.concurrent.LimitedSyncArgTask;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetCapacityTest {
    private static final Logger s_logger = LoggerFactory.getLogger(SetCapacityTest.class);

    @Test
    public void testSetCapacity(){
        LimitedSyncArgTask<Integer> syncArgTask = new LimitedSyncArgTask<>((x)->{
            s_logger.info("running for {}", x);
        });
        syncArgTask.setMax(0);
        syncArgTask.setCapacity(3);

        Thread threadSubmit = new Thread(()->{
            for(int i=0;i<5;i++) {
                syncArgTask.submit(i);
                s_logger.info("submitted for {}", i);
            }
        });
        threadSubmit.start();

        try {
            int j = 0;
            while (syncArgTask.getWaiting() < 3 && j < 1000) {
                j++;
                Thread.sleep(10);
            }
            Thread.sleep(1000);
        } catch (InterruptedException ex) {

        }

        Assert.assertEquals(3, syncArgTask.getWaiting());

        syncArgTask.setMax(1);
        syncArgTask.stop();
    }
}
