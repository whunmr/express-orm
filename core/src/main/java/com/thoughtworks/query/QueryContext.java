package com.thoughtworks.query;

import com.thoughtworks.Model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class QueryContext {
    private Map<Class, Set<Class<Model>>> eagerLoadingMap = newHashMap();
    public void addEagerLoadingModels(Class originalModel, Class<Model> eagerLoadingModel) {
        Set<Class<Model>> eagerLoadingModelSet = eagerLoadingMap.get(originalModel);
        if (eagerLoadingModelSet == null) {
            eagerLoadingModelSet = newHashSet();
        }

        eagerLoadingModelSet.add(eagerLoadingModel);
        eagerLoadingMap.put(originalModel, eagerLoadingModelSet);
    }

    public Set<Class<Model>> getEagerClassSetOf(Class originalModel) {
        Set<Class<Model>> classes = eagerLoadingMap.get(originalModel);
        return classes != null ? classes : new HashSet<Class<Model>>();
    }

    public void clearEagerLoadingFor(Class resultModelClass) {
        eagerLoadingMap.remove(resultModelClass);
    }
}
