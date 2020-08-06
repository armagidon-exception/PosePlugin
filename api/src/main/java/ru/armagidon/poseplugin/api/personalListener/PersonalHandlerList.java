package ru.armagidon.poseplugin.api.personalListener;

import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PersonalHandlerList
{
    private final Map<PosePluginPlayer, Set<PersonalListener>> subscribers;

    public PersonalHandlerList() {
        subscribers = new ConcurrentHashMap<>();
    }

    public void subscribe(PosePluginPlayer player, PersonalListener listener){
        Set<PersonalListener> listeners = subscribers.computeIfAbsent(player, (p) -> ConcurrentHashMap.newKeySet());
        Validate.notNull(listener);
        listeners.add(listener);
    }

    public void unsubscribe(PosePluginPlayer player, PersonalListener listener){
            if (subscribers.containsKey(player))
                subscribers.get(player).remove(listener);
    }

    private void forEachMethods(PersonalListener listener, Event event){
        Class<?> superclass = listener.getClass();
        while (superclass!=null) {
            for (Method m : superclass.getDeclaredMethods()) {
                if (!m.isAnnotationPresent(PersonalEventHandler.class)) continue;
                if(!(m.getParameterTypes().length==1&&m.getParameterTypes()[0].getSimpleName().equals(event.getEventName()))) continue;
                try {
                    m.invoke(listener, event);
                } catch (Exception ignore) {}
            }
            superclass = superclass.getSuperclass();
        }
    }

    public void dispatch(PosePluginPlayer player, Event event){
        if(subscribers.containsKey(player)) {
            subscribers.get(player).forEach(listener -> forEachMethods(listener, event));
        }
    }
}
