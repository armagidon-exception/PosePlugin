package ru.armagidon.poseplugin.api.personalListener;

import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
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
        Set<Class<?>> classesToHandle = new HashSet<>();
        Class<?> superclass = listener.getClass();
        while (superclass != null && PersonalListener.class.isAssignableFrom(superclass)) {
            classesToHandle.add(superclass);
            superclass = superclass.getSuperclass();
        }
        Set<Method> methodsToHandle = new HashSet<>();
        classesToHandle.forEach(clazz -> {

            Arrays.stream(clazz.getMethods()).filter(method -> method.isAnnotationPresent(PersonalEventHandler.class)).
            filter(method -> method.getParameterTypes().length == 1).filter(method -> {
                boolean equals = method.getParameterTypes()[0].equals(event.getClass());
                boolean _extends = method.getParameterTypes()[0].isInstance(event);
                return equals || _extends;
            })
            .forEach(methodsToHandle::add);
        });
        methodsToHandle.forEach(method -> {
            try {
                method.invoke(listener, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    public void dispatch(PosePluginPlayer player, Event event){
        if(subscribers.containsKey(player)) {
            subscribers.get(player).forEach(listener -> {
                forEachMethods(listener, event);
            });
        }
    }
}
