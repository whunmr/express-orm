package com.thoughtworks.query;

import com.thoughtworks.Model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class QueryContext {
    private Map<Class<? extends Model>, Set<Class<? extends Model>>> eagerLoadingMap = newHashMap();
    public void addEagerLoadingModels(Class<? extends Model> originalModel, Class<? extends Model> eagerLoadingModel) {
        Set<Class<? extends Model>> eagerLoadingModelSet = eagerLoadingMap.get(originalModel);
        if (eagerLoadingModelSet == null) {
            eagerLoadingModelSet = newHashSet();
        }

        eagerLoadingModelSet.add(eagerLoadingModel);
        eagerLoadingMap.put(originalModel, eagerLoadingModelSet);
    }

    public Set<Class<? extends Model>> getEagerModelSetOf(Class<? extends Model> originalModel) {
        Set<Class<? extends Model>> classes = eagerLoadingMap.get(originalModel);
        return classes != null ? classes : new HashSet<Class<? extends Model>>();
    }
}
