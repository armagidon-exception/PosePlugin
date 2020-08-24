import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.InputStreamReader;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {YamlConfiguration.class, Class.class})
public class FileTest {

    @Test
    public void testStream(){
        InputStream stream = getClass().getResourceAsStream("/locale/en.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
        System.out.println(yamlConfiguration.getKeys(false).size());
    }

}
