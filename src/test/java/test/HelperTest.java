package test;

import com.incarcloud.auxiliary.Helper;
import org.junit.Assert;
import org.junit.Test;

public class HelperTest {

    @Test
    public void formatkMGTest(){
        String str5k = Helper.formatkMG(5000.0f, "%1.2f");
        String str236M = Helper.formatkMG(236000000, "%3.0f");

        Assert.assertTrue(str5k.equals("5.00k"));
        Assert.assertTrue(str236M.equals("236M"));
    }
}
