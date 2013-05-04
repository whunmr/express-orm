package com.thoughtworks.sql;

import com.thoughtworks.Model;

public interface SqlComposer {
    String getInsertSQL(Model model);

    String getSelectSQL(String modelClassName, Object primaryKey);

    String getSelectWithWhereSQL(String modelClassName, String criteria);

    String getDeleteSQL(String modelClassName, String criteria);

    String getCountSQL(String modelClassName);

    String getDeleteInSQL(String modelClassName, Object... primaryKeys);

    String getLastInsertIdSQL();

    String getUpdateSQL(Model model);

    String getTheManysSQLInOne2ManyAssociation(Class theManyClass, Model model);
}
