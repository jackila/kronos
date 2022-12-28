package com.kronos.connector.mysql.testutils;

/**
 * @Author: jackila
 * @Date: 20:30 2022/12/20
 */
public class FixationDatabase extends UniqueDatabase {
    public FixationDatabase(MySqlContainer container,
                            String databaseName,
                            String username,
                            String password) {
        super(container, databaseName, username, password, true);
    }
}
