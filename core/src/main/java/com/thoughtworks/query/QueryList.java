package com.thoughtworks.query;

import com.thoughtworks.DB;
import com.thoughtworks.Model;
import com.thoughtworks.query.naming.DefaultNameGuesser;
import com.thoughtworks.query.naming.NameGuesser;
import com.thoughtworks.query.sql.MySQLSqlComposer;
import com.thoughtworks.query.sql.SqlComposer;
import com.thoughtworks.queryresult.ResultSets;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class QueryList<T extends Model> implements List<T> {
    private static SqlComposer sqlComposer = new MySQLSqlComposer();
    private static NameGuesser guesser = new DefaultNameGuesser();
    private final Class modelClass;

    private final String modelClassName;

    private String criteria;
    private Set<Class<? extends Model>> eagerLoadClasses = newHashSet();
    private List<T> records;
    private Integer limit;
    private Integer offset;
    public QueryList(Class modelClass, String criteria) {
        this.modelClass = modelClass;
        this.modelClassName = modelClass.getName();
        this.criteria = criteria;
    }

    public QueryList(Class modelClassName) {
        this(modelClassName, "");
    }

    public String getModelClassName() {
        return modelClassName;
    }

    private void queryDBIfNeed() {
        if (records == null) {
            String sql = sqlComposer.getSelectWithWhereSQL(modelClassName, criteria, limit, offset);
            records = QueryUtil.executeObjectListQuery(modelClassName, sql);
            doEagerLoadingIfNeed(records, modelClass);
        }
    }

    public <T extends Model> List<T> doEagerLoadingIfNeed(List<T> resultModels, Class resultModelClass) {
        if (resultModels.isEmpty()) {
            return resultModels;
        }

        String parentIdInCriteria = sqlComposer.getParentIdInCriteria(modelClassName, (List<Model>) resultModels);
            for (Class<? extends Model> eagerClass : eagerLoadClasses) {
                Class<Model> eagerClazz = (Class<Model>) eagerClass;
                String eagerLoadingSQL = sqlComposer.getSelectWithWhereSQL(eagerClass.getName(), parentIdInCriteria);
                List<Model> eagerInstances = executeObjectListQuery(eagerClass, eagerLoadingSQL);

                bindEagerInstances(resultModels, eagerInstances, resultModelClass, eagerClazz);
        }

        return resultModels;
    }

    private <T extends Model> void bindEagerInstances(List<T> resultModels, List<Model> eagerInstances,
                                                      Class resultModelClass, Class<Model> eagerClass) {
        Field eagerField = getEagerField(resultModelClass, eagerClass);
        if (eagerField != null) {
            Map<Object, List<Model>> eagerMap = buildForeignKeyToEagerInstancesMap(eagerClass, eagerInstances);
            for (T resultModel : resultModels) {
                SetEagerFieldFor(resultModel, eagerField, eagerMap);
            }
        }
    }

    private static <T extends Model> void SetEagerFieldFor(T resultModel, Field eagerField, Map<Object, List<Model>> eagerInstancesMap) {
        try {
            List<Model> eagerInstances = eagerInstancesMap.get(resultModel.getId());
            if (eagerInstances == null || eagerInstances.isEmpty()) {
                return;
            }

            Class<?> fieldType = eagerField.getType();
            if (fieldType.equals(List.class)) {
                eagerField.set(resultModel, eagerInstances);
            } else if (fieldType.equals(Set.class)) {
                eagerField.set(resultModel, newHashSet(eagerInstances));
            }
            if (fieldType.isArray()) {
                Class<?> eagerFieldComponentType = getClass(eagerInstances.get(0).getClass());
                Object[] array = (Object[]) Array.newInstance(eagerFieldComponentType, eagerInstances.size());
                eagerInstances.toArray(array);
                eagerField.set(resultModel, array);
            }
        } catch (IllegalAccessException e) {
        }
    }

    public static Class<?> getClass(Type type)
    {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static Field getEagerField(Class resultModelClass, Class<? extends Model> eagerClass) {
        try {
            String eagerFieldName = guesser.getCollectionFieldName(eagerClass.getName());
            Field eagerField = getAccessibleField(resultModelClass, eagerFieldName);
            if (isFieldTypeConsistent(eagerClass, eagerField)) {
                return eagerField;
            }
        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    private static boolean isFieldTypeConsistent(Class<? extends Model> eagerClass, Field eagerField) {
        Class fieldClass;

        if (eagerField.getType().isArray()) {
            fieldClass = eagerField.getType().getComponentType();
        } else {
            ParameterizedType listType = (ParameterizedType) eagerField.getGenericType();
            fieldClass = (Class) listType.getActualTypeArguments()[0];
        }

        return fieldClass.equals(eagerClass);
    }

    private <S extends Model> Map<Object, List<S>> buildForeignKeyToEagerInstancesMap(Class eagerClass, List<S> eagerInstances) {
        String foreignKeyFieldName = guesser.getForeignKeyFieldName(modelClassName);
        Map<Object, List<S>> eagerInstancesMap = newHashMap();

        try {
            Field field = getAccessibleField(eagerClass, foreignKeyFieldName);
            for (S eagerInstance : eagerInstances) {
                putEagerInstanceToMapAccordingForeignKey(eagerInstancesMap, field, eagerInstance);
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }

        return eagerInstancesMap;
    }

    private static <S extends Model> void putEagerInstanceToMapAccordingForeignKey(Map<Object, List<S>> eagerInstancesMap, Field field, S eagerInstance) throws IllegalAccessException {
        Object foreignKeyValue = field.get(eagerInstance);
        List<S> eagerList = eagerInstancesMap.get(foreignKeyValue);
        if (eagerList == null) {
            eagerList = newArrayList();
        }
        eagerList.add(eagerInstance);
        eagerInstancesMap.put(foreignKeyValue, eagerList);
    }

    private static Field getAccessibleField(Class eagerModelClass, String foreignKeyFieldName) throws NoSuchFieldException {
        Field field = eagerModelClass.getField(foreignKeyFieldName);
        field.setAccessible(true);
        return field;
    }

    public static <T extends Model> List<T> executeObjectListQuery(Class modelClass, String sql) {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSets.assembleInstanceListBy(resultSet, modelClass.getName());
        } catch (SQLException e) {
            throw new ORMException(e);
        } finally {
            QueryUtil.close(statement);
        }
    }

    public <E extends Model> QueryList<E> includes(Class<? extends Model> eagerLoadClass) {
        eagerLoadClasses.add(eagerLoadClass);
        return (QueryList<E>) this;
    }

    @Override
    public int size() {
        queryDBIfNeed();
        return records.size();
    }

    @Override
    public boolean isEmpty() {
        queryDBIfNeed();
        return records.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        queryDBIfNeed();
        return records.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        queryDBIfNeed();
        return records.iterator();
    }

    @Override
    public Object[] toArray() {
        queryDBIfNeed();
        return records.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        queryDBIfNeed();
        return records.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        queryDBIfNeed();
        return records.containsAll(c);
    }

    @Override
    public T get(int index) {
        queryDBIfNeed();
        return records.get(index);
    }

    @Override
    public int indexOf(Object o) {
        queryDBIfNeed();
        return records.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        queryDBIfNeed();
        return records.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        queryDBIfNeed();
        return records.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        queryDBIfNeed();
        return records.listIterator(index);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public <E extends Model> QueryList<E> limit(int limit) {
        this.limit = limit;
        return (QueryList<E>) this;
    }

    public <E extends Model> QueryList<E> offset(int offset) {
        this.offset = offset;
        return (QueryList<E>) this;
    }
}
