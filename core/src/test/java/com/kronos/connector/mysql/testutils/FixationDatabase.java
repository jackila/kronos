package com.kronos.connector.mysql.testutils;

/** */
public class FixationDatabase extends UniqueDatabase {
    public FixationDatabase(
            MySqlContainer container, String databaseName, String username, String password) {
        super(container, databaseName, username, password, true);
    }
}
