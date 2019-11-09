package test;

import com.incarcloud.concurrent.LimitedSyncArgTask;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetMaxTest {

    private static final Logger s_logger = LoggerFactory.getLogger(SetMaxTest.class);

    @Test
    public void testSetMax4(){
        // 动态修改最大并发数测试
        int max = 100;

        LimitedSyncArgTask<Integer> syncArgTask = new LimitedSyncArgTask<>((x)->{
            try {
                Thread.sleep(5);
            }catch (InterruptedException ex){

            }
        });
        syncArgTask.setMax(1);

        for(int i=1;i<=max;i++){
            syncArgTask.submit(i);
        }
        int nRunning = syncArgTask.getRunning();
        s_logger.info("Running ====> {}", nRunning);

        syncArgTask.setMax(4);
        nRunning = syncArgTask.getRunning();
        s_logger.info("Running ====> {}", nRunning);

        syncArgTask.stop();
        s_logger.info("\n{}", LimitedSyncArgTask.printMetric(syncArgTask, 0));
    }
}
