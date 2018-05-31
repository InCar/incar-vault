package test;

import com.incarcloud.concurrent.LimitedAsyncArgTask;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitedAsyncArgTaskTest {
    private static final Logger s_logger = LoggerFactory.getLogger(LimitedAsyncArgTaskTest.class);

    @Test
    public void testArg10k(){
        int max = 1000*10;

        LimitedAsyncArgTask<Integer> syncArgTask = new LimitedAsyncArgTask<>((x, onFin)->{
            // s_logger.info("{}", x);
            onFin.run();
        });
        syncArgTask.setMax(2);

        for(int i=1;i<=max;i++){
            syncArgTask.submit(i);
        }

        syncArgTask.stop();

        s_logger.info("\n{}", LimitedAsyncArgTask.printMetric(syncArgTask, 0));
    }
}
