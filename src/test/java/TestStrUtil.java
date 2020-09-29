import org.junit.Assert;
import org.junit.Test;
import uk.ac.ucl.util.core.StrUtil;

public class TestStrUtil {
    @Test
    public void testSubAfter(){
        String s = StrUtil.subAfter("127.0.0.1:18080/shello.html", "/s");
        Assert.assertEquals( "hello.html", s);
    }
    @Test
    public void testSecondSubAfter(){
        String s = StrUtil.subAfter("http://127.0.0.1:18080/a/index.html",
                "/", true);
        Assert.assertEquals("index.html", s);
    }
}
