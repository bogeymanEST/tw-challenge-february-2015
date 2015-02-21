package com.github.bogeymanest.twchallenge;

import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class Main {
    static Statement st;

    public static void main(String[] args) throws IOException {
        Connection con;

        String url = "jdbc:mysql://10.1.0.184:3306/twchallenge";
        String user = "twchallenge";
        String password = "twchallenge";

        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        FreeMarkerEngine engine = new FreeMarkerEngine();
        Configuration config = new Configuration();
        config.setDirectoryForTemplateLoading(new File(""));
        engine.setConfiguration(config);
        get("/hello", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Failed to get message!");
            try {
                ResultSet rs = null;
                rs = st.executeQuery("SELECT * FROM hello");
                rs.next();
                attributes.put("message", rs.getString("message"));

                // The hello.ftl file is located in directory:
                // src/test/resources/spark/template/freemarker
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ModelAndView(attributes, "hello.ftl");
        }, engine);
    }
}
