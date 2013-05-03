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

import static com.google.common.base.Preconditions.checkNotNull;

public class BaseDBTest {
    private static boolean isDBRested;

    @Before
    public void setUp() throws ClassNotFoundException, IOException, SQLException {
        if (!isDBRested) {
            resetDatabase();
            isDBRested = true;
        }

        DB.connection().setAutoCommit(false);
    }

    protected void truncateTable(String tableName) {
        checkNotNull(tableName);
        try {
            DB.connection().createStatement().execute("TRUNCATE TABLE " + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void resetDatabase() throws IOException, SQLException, ClassNotFoundException {
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
    public void tearDown() throws ClassNotFoundException, IOException, SQLException {
        DB.connection().rollback();
    }
}
