package ru.armagidon.poseplugin.api.utils.misc;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.Nullable;
import ru.armagidon.poseplugin.api.utils.versions.Version;

import java.util.HashMap;
import java.util.Map;

public class MetaDataKeyRings
{

    private final Map<String, KeyRing> keyRings = new HashMap<>();

    @Nullable
    public KeyRing getKeyRing(String name) {
        if (!keyRings.containsKey(name)) return null;
        return keyRings.get(name);
    }

    public void generateKeyRing(String name, ImmutableMap<Version, Integer> keys) {
        Validate.notEmpty(name);
        Validate.notEmpty(keys);
        if (keyRings.containsKey(name))
            throw new IllegalArgumentException("This keyring is already exists");
        keyRings.put(name, new KeyRing(keys));
    }


    @AllArgsConstructor
    public static class KeyRing {
        private final ImmutableMap<Version, Integer> keys;

        public int getKey() {
            if (!keys.containsKey(Version.getVersion())) return -1;
            return keys.get(Version.getVersion());
        }
    }
}
