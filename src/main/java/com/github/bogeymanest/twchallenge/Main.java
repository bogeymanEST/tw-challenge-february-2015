package com.github.bogeymanest.twchallenge;

import com.google.gson.Gson;
import freemarker.template.Configuration;
import spark.Route;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Random;

import static spark.Spark.get;
import static spark.Spark.post;

public class Main {
    static Statement st;
    private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static JsonTransformer transformer = new JsonTransformer();


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
        jsonPost("/client/create", (request, response) -> {
            String stripeToken = request.queryParams("stripeToken");
            String stripeEmail = request.queryParams("stripeEmail");
            if (stripeEmail == null || stripeToken == null || stripeToken.isEmpty() || stripeEmail.isEmpty()) {
                return new ResponseError("Missing stripeToken or stripeEmail");
            }
            String clientId = "cl_" + randomString(25);
            String deleteId = "del_" + randomString(25);

            st.executeUpdate(String.format("INSERT INTO client (client_id, delete_id, email, stripe_id) VALUES('%s', '%s', '%s', '%s')", clientId, deleteId, stripeToken, stripeEmail));
            return new ResponseClientCreate(clientId, deleteId, stripeEmail);
        });
        jsonGet("/client/get", (request, response) -> {
            String clientId = request.queryParams("clientId");
            if(clientId == null)
                return new ResponseError("Missing clientId");
            ResultSet rs = st.executeQuery(String.format("SELECT * FROM client WHERE client_id='%s'", clientId));
            if(rs.next()) {
                return new ResponseClientGet(true);
            }
            return new ResponseClientGet(false);
        });
        jsonPost("/recipient/create", (request, response) -> {
            String stripeToken = request.queryParams("stripeToken");
            String stripeEmail = request.queryParams("stripeEmail");
            if (stripeEmail == null || stripeToken == null || stripeToken.isEmpty() || stripeEmail.isEmpty()) {
                return new ResponseError("Missing stripeToken or stripeEmail");
            }
            String clientId = "r_" + randomString(25);

            st.executeUpdate(String.format("INSERT INTO recipient (recipient_id, email, stripe_id) VALUES('%s', '%s', '%s')", clientId, stripeToken, stripeEmail));
            return new ResponseRecipientCreate(clientId, stripeEmail);
        });
    }
    private static String randomString(int len) {
        StringBuilder sb = new StringBuilder();
        while(sb.length() < len) {
            sb.append(chars.charAt(new Random().nextInt(chars.length())));
        }
        return sb.toString();
    }
    public static void jsonGet(String path, Route route) {
        get(path, "application/json", route, transformer);
    }

    public static void jsonPost(String path, Route route) {
        post(path, "application/json", route, transformer);
    }

}
