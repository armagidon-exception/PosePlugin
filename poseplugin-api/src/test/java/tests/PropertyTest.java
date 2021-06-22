package tests;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import ru.armagidon.poseplugin.api.utils.misc.MetaDataKeyRings;
import ru.armagidon.poseplugin.api.utils.versions.Version;

public class PropertyTest {


    @Test
    public void testProperties(){
/*        PropertyMap map = new PropertyMap();
        ChangingValueClass clazz = new ChangingValueClass(0, false, "STRING");
        map.registerProperty("integer",new Property<>(clazz::getInteger, clazz::setInteger));
        System.out.println("Constructor value");
        System.out.println("Before changing: "+map.getProperty("integer", Integer.class).getValue());
        map.getProperty("integer", Integer.class).setValue(5);
        System.out.println("After changing: "+map.getProperty("integer", Integer.class).getValue());
        map.registerProperty("integer2", new Property<>(()->5, clazz::setInteger));
        System.out.println("Pre-set value");
        System.out.println("Before changing: "+map.getProperty("integer2", Integer.class).getValue());
        map.getProperty("integer2", Integer.class).setValue(0);
        System.out.println("After changing: "+map.getProperty("integer2", Integer.class).getValue());*/

        MetaDataKeyRings keyRings = new MetaDataKeyRings();
        keyRings.generateKeyRing("POSE", ImmutableMap.<Version, Integer>builder().put(Version.v1_16, 1).build());
        System.out.println(keyRings.getKeyRing("POSE").getKey());

    }

}
