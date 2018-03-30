package com.seal.contracts.generator.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Juraj on 22.08.2017.
 */
public class AnnotationUtil {
    public static Set<Field> findFields(Class<?> classs, Class<? extends Annotation> ann) {
        Set<Field> set = new HashSet<>();
        Class<?> c = classs;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(ann)) {
                    set.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return set;
    }
}
