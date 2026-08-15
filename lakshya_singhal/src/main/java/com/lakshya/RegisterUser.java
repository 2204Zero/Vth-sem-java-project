package com.lakshya;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.mindrot.jbcrypt.BCrypt;

public class RegisterUser {

    public static void register(String username, String password) {

        try {

            Connection conn = DBConnection.getConnection();

            String hash = BCrypt.hashpw(password, BCrypt.gensalt());

            String sql =
                    "INSERT INTO Users(username,password_hash) VALUES(?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, hash);

            int rows = ps.executeUpdate();

            if(rows>0)
                System.out.println("User Registered Successfully!");

            conn.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

    }

}