package ru.armagidon.poseplugin.api.ticking;

import lombok.Getter;
import org.apache.commons.lang.Validate;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TickingBundle
{
    private final Map<Class<? extends Tickable>, Bundle> tickingBundles = new HashMap<>();

    public void addToTickingBundle(Class<? extends Tickable> clazz, Tickable ticker){
        Validate.notNull(clazz);
        Validate.notNull(ticker);
        tickingBundles.computeIfAbsent(clazz, (c)->{
            Bundle bundle = new Bundle();
            PosePluginAPI.getAPI().getTickManager().registerTickModule(bundle.getTicker(), false);
            return bundle;
        }).getTickables().add(ticker);
    }

    public void removeFromTickingBundle(Class<? extends Tickable> clazz,Tickable ticker){
        Validate.notNull(ticker);
        if(!tickingBundles.containsKey(clazz)) return;
        tickingBundles.get(clazz).getTickables().remove(ticker);
        if(tickingBundles.get(clazz).getTickables().size()==0) {
            PosePluginAPI.getAPI().getTickManager().removeTickModule(tickingBundles.get(clazz).getTicker());
            tickingBundles.remove(clazz);
        }
    }

    private static class Bundle{
        private @Getter final Set<Tickable> tickables;
        private @Getter final Tickable ticker;

        public Bundle() {
            this.tickables = new HashSet<>();
            this.ticker = ()->{
                if(tickables.size()==0) return;
                tickables.forEach(Tickable::tick);
            };
        }
    }
}
