package com.thoughtworks;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDBTest {
    private static boolean schemaLoaded;

    @Before
    public void setUp() throws ClassNotFoundException, IOException, SQLException {
        if (!schemaLoaded) {
            loadSchema();
            schemaLoaded = true;
        }

        DB.connection().setAutoCommit(false);
    }

    private void loadSchema() throws IOException, SQLException, ClassNotFoundException {
        URL url = Resources.getResource("dbschema.sql");
        String sqls = Resources.toString(url, Charsets.UTF_8);

        for (String sql : sqls.split(";")) {
            if (Strings.isNullOrEmpty(sql.trim())) {
                continue;
            }

            Statement statement = DB.connection().createStatement();
            statement.execute(sql);
            statement.close();
        }
    }

    @After
    public void after() throws ClassNotFoundException, IOException, SQLException {
        DB.connection().rollback();
    }
}
