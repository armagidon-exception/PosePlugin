package ru.armagidon.poseplugin.testing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.armagidon.poseplugin.api.utils.property.Property;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {PropertyMap.class, Property.class})
public class PropertyTest {

    @Test
    public void testProperties(){
        PropertyMap map = new PropertyMap();
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
        System.out.println("After changing: "+map.getProperty("integer2", Integer.class).getValue());
    }

}
