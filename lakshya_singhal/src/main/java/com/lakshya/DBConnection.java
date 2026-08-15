package com.lakshya;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost\\MSSQLSERVER03;"
          + "databaseName=SecureLoginDB;"
          + "encrypt=true;"
          + "trustServerCertificate=true;";

    private static final String USER = "---------";
    private static final String PASSWORD = "----------";

    public static Connection getConnection() {

        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }

    }

}