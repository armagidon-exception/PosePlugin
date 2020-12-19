import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class FileTest {

    @Test
    public void testStream() throws IOException {
        InputStream io = FileTest.class.getResourceAsStream("/en.yml");
        System.out.println(io.available());
    }

}
