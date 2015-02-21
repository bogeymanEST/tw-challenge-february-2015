package com.github.bogeymanest.twchallenge;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import freemarker.template.Configuration;
import spark.Route;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static spark.Spark.get;
import static spark.Spark.post;

public class Main {
    private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static JsonTransformer transformer = new JsonTransformer();
    private static String baseUrl= "http://localhost:4567";
    private static Connection con = null;


    public static void main(String[] args) throws IOException {
        Stripe.apiKey = "sk_test_9Tb1psgAcXkyof9hZAapK5gd";
        String url = "jdbc:mysql://10.1.0.184:3306/twchallenge";
        String user = "twchallenge";
        String password = "twchallenge";

        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        FreeMarkerEngine engine = new FreeMarkerEngine();
        Configuration config = new Configuration();
        config.setDirectoryForTemplateLoading(new File("template"));
        engine.setConfiguration(config);
        jsonPost("/api/client/create", (request, response) -> {
            String stripeToken = request.queryParams("stripeToken");
            String stripeEmail = request.queryParams("stripeEmail");
            if (stripeEmail == null || stripeToken == null || stripeToken.isEmpty() || stripeEmail.isEmpty()) {
                return new ResponseError("Missing stripeToken or stripeEmail");
            }
            String clientId = "cl_" + randomString(25);
            String deleteId = "del_" + randomString(25);

            getStatement().executeUpdate(String.format("INSERT INTO client (client_id, delete_id, stripe_id, email) VALUES('%s', '%s', '%s', '%s')", clientId, deleteId, stripeToken, stripeEmail));
            return new ResponseClientCreate(clientId, deleteId, stripeEmail);
        });
        jsonGet("/api/client/get", (request, response) -> {
            String clientId = request.queryParams("clientId");
            if(clientId == null)
                return new ResponseError("Missing clientId");
            ResultSet rs = getStatement().executeQuery(String.format("SELECT * FROM client WHERE client_id='%s'", clientId));
            if(rs.next()) {
                return new ResponseClientGet(true);
            }
            return new ResponseClientGet(false);
        });
        jsonPost("/api/recipient/create", (request, response) -> {
            String stripeToken = request.queryParams("stripeToken");
            String stripeEmail = request.queryParams("stripeEmail");
            if (stripeEmail == null || stripeToken == null || stripeToken.isEmpty() || stripeEmail.isEmpty()) {
                return new ResponseError("Missing stripeToken or stripeEmail");
            }
            String clientId = "r_" + randomString(25);

            getStatement().executeUpdate(String.format("INSERT INTO recipient (recipient_id, email, stripe_id) VALUES('%s', '%s', '%s')", clientId, stripeToken, stripeEmail));
            return new ResponseRecipientCreate(clientId, stripeEmail);
        });
        jsonPost("/api/payment/create", (request, response) -> {
            String recipientId = request.queryParams("recipientId");
            double price = Double.valueOf(request.queryParams("price"));
            String paymentId = "p_" + randomString(25);
            getStatement().executeUpdate(String.format("INSERT INTO payment (recipient_id, price, payment_id) VALUES('%s', %s, '%s')", recipientId, price, paymentId));
            String link = baseUrl + "/pay/" + paymentId;
            String status = baseUrl + "/status/" + paymentId;
            return new ResponsePaymentCreate(paymentId, link, status);
        });
        jsonGet("/pay/:payment_id", (request, response) -> {
            String paymentId = request.params("payment_id");
            ResultSet rs = getStatement().executeQuery(String.format("SELECT * FROM payment WHERE payment_id='%s", paymentId));
            return "NEED TEMPLATE";
        });
        final Connection finalCon = con;
        jsonPost("/api/pay/process", (request, response) -> {
            String paymentId = request.queryParams("paymentId");
            String clientId = request.queryParams("clientId");
            ResultSet payset = getStatement().executeQuery(String.format("SELECT * FROM payment WHERE payment_id='%s'", paymentId));
            ResultSet clset = getStatement().executeQuery(String.format("SELECT * FROM client WHERE client_id='%s'", clientId));
            if(!payset.next())
                return new ResponseError("Couldn't find payment!");
            if(!clset.next())
                return new ResponseError("Couldn't find client!");
            Map<String, Object> chargeParams = new HashMap<>();
            int price = payset.getInt("price");
            chargeParams.put("amount", price);
            chargeParams.put("currency", "EUR");
            chargeParams.put("customer", clset.getString("stripe_id"));
            Map<String, String> initialMetadata = new HashMap<>();
            initialMetadata.put("payment_id", paymentId);
            chargeParams.put("metadata", initialMetadata);

            try {
                Charge charge = Charge.create(chargeParams);
                getStatement().executeUpdate(String.format("UPDATE payment WHERE payment_id='%s' SET processed=1", paymentId));
            }
            catch (Exception e) {
                return new ResponseError(e.getMessage());
            }
            return new ResponsePayProcess();

        });
        jsonGet("/api/payment/status", (request, response) -> {
            String paymentId = request.queryParams("paymentId");

            ResultSet rs = getStatement().executeQuery(String.format("SELECT * FROM payment WHERE payment_id='%s'", paymentId));
            rs.next();
            return new ResponsePaymentStatus(rs.getInt("status"));
        });
    }

    private static Statement getStatement() {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
