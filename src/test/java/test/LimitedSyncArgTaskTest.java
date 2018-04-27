package test;

import com.incarcloud.concurrent.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitedSyncArgTaskTest {
    private static final Logger s_logger = LoggerFactory.getLogger(LimitedSyncArgTaskTest.class);

    @Test
    public void testArg100(){
        int max = 10000;

        LimitedSyncArgTask<Integer> syncArgTask = new LimitedSyncArgTask<>((x)->{
            // s_logger.info("{}", x);
        });
        syncArgTask.setMax(2);

        for(int i=1;i<=max;i++){
            syncArgTask.submit(i);
        }

        syncArgTask.stop();

        s_logger.info(LimitedTaskTest.printMetric(syncArgTask));
    }


}
