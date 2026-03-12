package com.edacourse.api.di;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Container {
    private final Map<Class<?>, Registration> registry = new HashMap<>();

    private final Map<Class<?>, Object> singletons = new HashMap<>();

    public <T> void register(Class<T> type, Class<? extends T> impl) {
        register(type, impl, Lifecycle.SINGLETON);
    }

    public <T> void register(Class<T> type, Class<? extends T> impl, Lifecycle lifecycle) {
        registry.put(type, new Registration(impl, lifecycle));
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        return (T) resolveInternal(type, new LinkedHashSet<>());
    }

    private Object resolveInternal(Class<?> type, Set<Class<?>> resolving) {
        if (!resolving.add(type)) {
            throw new RuntimeException("Dependencia circular detectada: " + type.getSimpleName());
        }

        Registration reg = registry.get(type);
        if (reg == null) {
            throw new RuntimeException("No se ha registrado la clase: " + type.getSimpleName());
        }

        if (reg.lifecycle() == Lifecycle.SINGLETON && singletons.containsKey(type)) {
            resolving.remove(type);
            return singletons.get(type);
        }

        try {
            Constructor<?>[] constructors = reg.impl().getConstructors();
            if (constructors.length == 0) {
                throw new RuntimeException("La clase " + reg.impl().getSimpleName() + " no tiene constructor");
            }

            Constructor<?> constructor = constructors[0];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = resolveInternal(paramTypes[i], resolving);
            }

            Object instance = constructor.newInstance(params);
            if (reg.lifecycle() == Lifecycle.SINGLETON) {
                singletons.put(type, instance);
            }
            resolving.remove(type);
            return instance;
            
        } catch (ContainerException e) {
            throw e;
        } catch (Exception e) {
            throw new ContainerException("Error al resolver la clase " + type.getSimpleName(), e);
        }
        
    }
}
