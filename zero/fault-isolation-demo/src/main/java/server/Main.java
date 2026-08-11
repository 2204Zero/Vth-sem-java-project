package server;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        try {

            DBConnectionPool.init();

            try (Connection connection =
                         DBConnectionPool.getDataSource().getConnection()) {

                System.out.println("Database connected successfully!");

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
}