package com.thoughtworks;

import com.thoughtworks.naming.DefaultNameGuesser;
import com.thoughtworks.naming.NameGuesser;
import com.thoughtworks.query.QueryContext;
import com.thoughtworks.query.QueryResult;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;
import com.thoughtworks.util.SqlUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
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

        return (T)this;
    }

    public static <T extends Model> T find(Object primaryKey) throws SQLException {
        String sql = sqlComposer.getSelectSQL(modelName(), primaryKey);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> List<T> find_all() throws SQLException {
        return find_all("");
    }

    public static <T extends Model> List<T> find_all(String criteria) throws SQLException {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        List<T> resultModels = executeObjectListQuery(modelName(), sql);
        if (resultModels.isEmpty()) {
            return resultModels;
        }

        Class resultModelClass = getModelClass();

        String parentIdInCriteria = sqlComposer.getParentIdInCriteria(modelName(), (List<Model>) resultModels);

        Set<Class<? extends Model>> eagerModelSetOf = queryContext.get().getEagerModelSetOf(resultModelClass);
        for (Class<? extends Model> eagerModelClass : eagerModelSetOf) {
            String selectSQL = sqlComposer.getSelectWithWhereSQL(eagerModelClass.getName(), parentIdInCriteria);
            List<? extends Model> eagerLoadedInstances = executeObjectListQuery(eagerModelClass.getName(), selectSQL);

            bindEagerInstances(resultModels, eagerLoadedInstances, resultModelClass, eagerModelClass);
        }

        return resultModels;
    }

    private static <T extends Model> void bindEagerInstances(List<T> resultModels, List<? extends Model> eagerInstances,
                                                             Class resultModelClass, Class<? extends Model> eagerModelClass) {
        String eagerFieldName = guesser.getCollectionFieldName(eagerModelClass.getName());

        Field eagerField = null;
        try {
            eagerField = resultModelClass.getDeclaredField(eagerFieldName);
            eagerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            return;
        }

        if (!isFieldTypeConsistent(eagerModelClass, eagerField)){
            return;
        }

        Map<Object, List<Model>> eagerInstancesMap = buildForeignKeyToEagerInstancesMap(getModelClass(), eagerModelClass, eagerInstances);

        for (T resultModel : resultModels) {
            try {
                List<Model> eagerInstancesOfThisResultModel = eagerInstancesMap.get(resultModel.getId());

                if (eagerField.getType().equals(List.class)) {
                    eagerField.set(resultModel, eagerInstancesOfThisResultModel);
                } else if (eagerField.getType().equals(Set.class)) {
                    eagerField.set(resultModel, newHashSet(eagerInstancesOfThisResultModel));
                } if (eagerField.getType().isArray()) {
                    eagerField.set(resultModel,newArrayList(eagerInstancesOfThisResultModel).toArray());
                }
            } catch (IllegalAccessException e) {
            }
        }
    }

    private static boolean isFieldTypeConsistent(Class<? extends Model> eagerModelClass, Field eagerField) {
        Class elementClass = null;

        if (eagerField.getType().isArray()) {
            elementClass = eagerField.getType().getComponentType();
        } else {
            ParameterizedType listType = (ParameterizedType) eagerField.getGenericType();
            elementClass = (Class) listType.getActualTypeArguments()[0];
        }

        if (!elementClass.equals(eagerModelClass)) {
            return false;
        }

        return true;
    }

    private static Map<Object, List<Model>> buildForeignKeyToEagerInstancesMap(Class modelClass, Class eagerModelClass, List<? extends Model> eagerInstances) {
        String foreignKeyFieldName = guesser.getForeignKeyFieldName(modelName());

        Map<Object, List<Model>> eagerInstancesMap = newHashMap();

        try {
            Field field = eagerModelClass.getField(foreignKeyFieldName);
            field.setAccessible(true);

            for (Model eagerInstance : eagerInstances) {
                Object foreignKeyValue = field.get(eagerInstance);

                List<Model> eagerList = eagerInstancesMap.get(foreignKeyValue);
                if (eagerList == null) {
                    eagerList = newArrayList();
                }
                eagerList.add(eagerInstance);
                eagerInstancesMap.put(foreignKeyValue, eagerList);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return eagerInstancesMap;
    }

    public static <T extends Model> T find_by_sql(String sql) throws SQLException {
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> T where(String criteria) throws SQLException {
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

    public static int delete(Object... primaryKeys) throws SQLException {
        checkNotNull(primaryKeys);
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), primaryKeys);
        return executeUpdate(deleteInSQL);
    }

    private static int executeUpdate(String sql) throws SQLException {
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

    private static <T extends Model> List<T> executeObjectListQuery(String modelClassName, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSets.assembleInstanceListBy(resultSet, modelClassName);
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
        return executeObjectListQuery(theManyClass.getName(), sql);
    }

}
