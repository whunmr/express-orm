package com.thoughtworks;

import com.thoughtworks.naming.DefaultNameGuesser;
import com.thoughtworks.naming.NameGuesser;
import com.thoughtworks.query.QueryContext;
import com.thoughtworks.query.QueryResult;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;
import com.thoughtworks.util.SqlUtil;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class Model {
    private static NameGuesser guesser = new DefaultNameGuesser();
    private static SqlComposer sqlComposer = new MySQLSqlComposer();

    private int id;

    private static ThreadLocal<QueryContext> queryContext = new ThreadLocal<QueryContext>() {
        @Override
        protected QueryContext initialValue() {
            return new QueryContext();
        }
    };

    public <T extends Model> T save() throws SQLException {
        boolean isNewRecord = this.id == 0;
        if (isNewRecord) {
            executeUpdate(sqlComposer.getInsertSQL(this));
            this.id = getIntegerQueryResult(sqlComposer.getLastInsertIdSQL());
        } else {
            executeUpdate(sqlComposer.getUpdateSQL(this));
        }

        return (T) this;
    }

    public static <T extends Model> T find_by_id(Object primaryKey) throws SQLException {
        String sql = sqlComposer.getSelectSQL(modelName(), primaryKey);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> List<T> find_all() throws SQLException {
        return new LazyList<T>(modelName());
    }

    public static <T extends Model> List<T> find_all(String criteria) throws SQLException {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        List<T> resultModels = executeObjectListQuery(getModelClass(), sql);
        if (resultModels.isEmpty()) {
            return resultModels;
        }

        return doEagerLoadingIfNeed(resultModels, getModelClass());
    }

    private static <T extends Model> List<T> doEagerLoadingIfNeed(List<T> resultModels, Class resultModelClass) throws SQLException {
        String parentIdInCriteria = sqlComposer.getParentIdInCriteria(modelName(), (List<Model>) resultModels);

        Set<Class<Model>> eagerClasses = queryContext.get().getEagerClassSetOf(resultModelClass);
        try {
            for (Class<Model> eagerClass : eagerClasses) {
                String eagerLoadingSQL = sqlComposer.getSelectWithWhereSQL(eagerClass.getName(), parentIdInCriteria);
                List<Model> eagerInstances = executeObjectListQuery(eagerClass, eagerLoadingSQL);

                bindEagerInstances(resultModels, eagerInstances, resultModelClass, eagerClass);
            }
        } finally {
            queryContext.get().clearEagerLoadingFor(resultModelClass);
        }

        return resultModels;
    }

    private static <T extends Model> void bindEagerInstances(List<T> resultModels, List<Model> eagerInstances,
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

    private static <S extends Model> Map<Object, List<S>> buildForeignKeyToEagerInstancesMap(Class eagerClass, List<S> eagerInstances) {
        String foreignKeyFieldName = guesser.getForeignKeyFieldName(modelName());
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

    public static <T extends Model> T find_by_sql(String sql) throws SQLException {
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> T find_first(String criteria) throws SQLException {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static int count() throws SQLException {
        String countSQL = sqlComposer.getCountSQL(modelName());
        return getIntegerQueryResult(countSQL);
    }

    private static int getIntegerQueryResult(String countSQL) throws SQLException {
        QueryResult queryResult = getQueryResult(countSQL);

        try {
            ResultSet resultSet = queryResult.getResultSet();
            resultSet.next();
            return Integer.valueOf(resultSet.getObject(1).toString());
        } finally {
            queryResult.close();
        }
    }

    public static int delete_all() throws SQLException {
        return delete_all(null);
    }

    public static int delete_all(String criteria) throws SQLException {
        String deleteAllSQL = sqlComposer.getDeleteSQL(modelName(), criteria);
        return executeUpdate(deleteAllSQL);
    }

    public int delete() throws SQLException {
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), this.getId());
        return executeUpdate(deleteInSQL);
    }

    public static int delete(Object[] primaryKeys) throws SQLException {
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), primaryKeys);
        return executeUpdate(deleteInSQL);
    }

    private static int executeUpdate(String sql) throws SQLException {
        System.out.println(sql);
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            return statement.executeUpdate(sql);
        } finally {
            SqlUtil.close(statement);
        }
    }

    private static <T extends Model> T executeSingleObjectQuery(String modelClassName, String querySQL) throws SQLException {
        QueryResult queryResult = getQueryResult(querySQL);
        T instance = ResultSets.assembleInstanceBy(queryResult.getResultSet(), modelClassName);
        queryResult.close();
        return instance;
    }

    private static QueryResult getQueryResult(String querySQL) throws SQLException {
        Statement statement = DB.connection().createStatement();
        ResultSet resultSet = statement.executeQuery(querySQL);
        return new QueryResult(resultSet, statement);
    }

    private static <T extends Model> List<T> executeObjectListQuery(Class modelClass, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSets.assembleInstanceListBy(resultSet, modelClass.getName());
        } finally {
            SqlUtil.close(statement);
        }
    }

    public String getTableName() {
        return guesser.getTableName(getClass().getSimpleName());
    }

    public static void includes(Class eagerLoadClass) {
        queryContext.get().addEagerLoadingModels(getModelClass(), eagerLoadClass);
    }

    private static Class getModelClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            String className = stackTraceElement.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                boolean isModelClass = clazz != null && !clazz.equals(Model.class) && Model.class.isAssignableFrom(clazz);
                if (isModelClass) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        throw new IllegalStateException("Please make sure using the instrument.jar as -javaagent of the running JVM. " +
                "refer https://github.com/whunmr/express-orm/blob/master/README.md for details");
    }

    private static String modelName() {
        return getModelClass().getName();
    }

    public int getId() {
        return id;
    }

    public <T extends Model> List<T> find_all(Class<T> theManyClass) throws SQLException {
        String sql = sqlComposer.getTheManysSQLInOne2ManyAssociation(theManyClass, this);
        return executeObjectListQuery(theManyClass, sql);
    }

}
