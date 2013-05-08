package com.thoughtworks;

import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;
import com.thoughtworks.util.SqlUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LazyList<T extends Model> implements List<T> {
    private static SqlComposer sqlComposer = new MySQLSqlComposer();
    private final String modelClassName;
    private final String criteria;
    private List<T> records;

    public LazyList(String modelClassName, String criteria) {
        this.modelClassName = modelClassName;
        this.criteria = criteria;
    }

    public LazyList(String modelClassName) {
        this(modelClassName, "");
    }

    private void queryDBIfNeed() {
        if (records == null) {
            String sql = sqlComposer.getSelectWithWhereSQL(modelClassName, criteria);
            records = executeObjectListQuery(modelClassName, sql);
        }
    }

    private List<T> executeObjectListQuery(String modelClassName, String sql) {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSets.assembleInstanceListBy(resultSet, modelClassName);
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        } finally {
            SqlUtil.close(statement);
        }
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
}
