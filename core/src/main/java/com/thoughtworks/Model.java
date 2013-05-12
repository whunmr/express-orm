package com.thoughtworks;

import com.thoughtworks.annotation.Table;
import com.thoughtworks.naming.DefaultNameGuesser;
import com.thoughtworks.naming.NameGuesser;
import com.thoughtworks.query.QueryList;
import com.thoughtworks.query.QueryUtil;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.util.List;

public class Model {
    private static SqlComposer sqlComposer = new MySQLSqlComposer();
    private static NameGuesser guesser = new DefaultNameGuesser();

    private int id;

    public <T extends Model> T save() {
        boolean isNewRecord = this.id == 0;
        if (isNewRecord) {
            QueryUtil.executeUpdate(sqlComposer.getInsertSQL(this));
            this.id = QueryUtil.getIntegerQueryResult(sqlComposer.getLastInsertIdSQL());
        } else {
            QueryUtil.executeUpdate(sqlComposer.getUpdateSQL(this));
        }

        return (T) this;
    }

    public static <T extends Model> T find_by_id(Object primaryKey) {
        String sql = sqlComposer.getSelectSQL(modelName(), primaryKey);
        return QueryUtil.executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> QueryList<T> find_all() {
        return new QueryList<T>(getModelClass());
    }

    public static <T extends Model> QueryList<T> find_all(String criteria) {
        return new QueryList<T>(getModelClass(), criteria);
    }

    public static <T extends Model> T find_by_sql(String sql) {
        return QueryUtil.executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> T find_first(String criteria) {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        return QueryUtil.executeSingleObjectQuery(modelName(), sql);
    }

    public static int count() {
        String countSQL = sqlComposer.getCountSQL(modelName());
        return QueryUtil.getIntegerQueryResult(countSQL);
    }

    public static int delete_all() {
        return delete_all(null);
    }

    public static int delete_all(String criteria) {
        String deleteAllSQL = sqlComposer.getDeleteSQL(modelName(), criteria);
        return QueryUtil.executeUpdate(deleteAllSQL);
    }

    public int delete() {
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), this.getId());
        return QueryUtil.executeUpdate(deleteInSQL);
    }

    public static int delete(Object[] primaryKeys) {
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), primaryKeys);
        return QueryUtil.executeUpdate(deleteInSQL);
    }

    public String getTableName() {
        Table tableAnnotation = this.getClass().getAnnotation(Table.class);
        if (tableAnnotation != null) {
            return tableAnnotation.value();
        }

        return guesser.getTableName(getClass().getSimpleName());
    }

    public int getId() {
        return id;
    }

    public <T extends Model> List<T> find_all(Class<T> theManyClass) {
        String sql = sqlComposer.getTheManysSQLInOne2ManyAssociation(theManyClass, this);
        return QueryList.executeObjectListQuery(theManyClass, sql);
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
}
