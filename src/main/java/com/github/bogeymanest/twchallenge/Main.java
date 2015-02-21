package com.github.bogeymanest.twchallenge;

import com.google.zxing.WriterException;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import freemarker.template.Configuration;
import spark.ModelAndView;
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
import static spark.SparkBase.externalStaticFileLocation;
import static spark.SparkBase.staticFileLocation;

public class Main {
    private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static JsonTransformer transformer = new JsonTransformer();
    private static String baseUrl= "http://192.168.43.213:4567";
    private static Connection con = null;


    public static void main(String[] args) throws IOException {
        Stripe.apiKey = "sk_test_9Tb1psgAcXkyof9hZAapK5gd";
        String url = "jdbc:mysql://192.168.43.213:3306/twchallenge";
        String user = "twchallenge";
        String password = "twchallenge";

        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        externalStaticFileLocation("public");
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
            if (clientId == null)
                return new ResponseError("Missing clientId");
            ResultSet rs = getStatement().executeQuery(String.format("SELECT * FROM client WHERE client_id='%s'", clientId));
            if (rs.next()) {
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

        jsonGet("/api/payment/status", (request, response) -> {
            String paymentId = request.queryParams("paymentId");

            ResultSet rs = getStatement().executeQuery(String.format("SELECT * FROM payment WHERE payment_id='%s'", paymentId));
            rs.next();
            return new ResponsePaymentStatus(rs.getInt("status"));
        });
        get("/payment/start", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Failed to get message!");
            return new ModelAndView(attributes, "w11.ftl");
        }, engine);
        get("/ajax/qr_code", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            String recipientId = request.queryParams("recipientId");
            double price = Double.valueOf(request.queryParams("price"));
            String paymentId = "p_" + randomString(25);
            try {
                getStatement().executeUpdate(String.format("INSERT INTO payment (recipient_id, price, payment_id) VALUES('%s', %s, '%s')", recipientId, (int)(price*100), paymentId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String link = baseUrl + "/pay/" + paymentId;
            String status = baseUrl + "/status/" + paymentId;
            try {
                attributes.put("image", QRCGenerator.stringToBytes(link));
                attributes.put("paymentId", paymentId);
            } catch (IOException | WriterException e) {
                e.printStackTrace();
            }
            return new ModelAndView(attributes, "ajax_qr.ftl");
        }, engine);
        get("/ajax/payment_complete", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            return new ModelAndView(attributes, "ajax_complete.ftl");
        }, engine);
        get("/pay/:payment_id", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            String paymentId = request.params("payment_id");
            String sql = String.format("SELECT * FROM payment WHERE payment_id='%s'", paymentId);
            ResultSet rs = null;
            try {
                rs = getStatement().executeQuery(sql);
                rs.next();
                attributes.put("price", rs.getInt("price"));
                attributes.put("paymentId", paymentId);
                attributes.put("pprice", rs.getInt("price") / 100);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ModelAndView(attributes, "pay.ftl");
        }, engine);
        post("/payment/process/:paymentId", (request, response) -> {
            String stripeToken = request.queryParams("stripeToken");
            Token token = null;
            try {
                token = Token.retrieve(stripeToken);
            } catch (AuthenticationException | InvalidRequestException | CardException | APIException | APIConnectionException e) {
                e.printStackTrace();
            }
            String paymentId = request.params("paymentId");
            Map<String, Object> attributes = new HashMap<>();
            try {
                ResultSet payset = getStatement().executeQuery(String.format("SELECT * FROM payment WHERE payment_id='%s'", paymentId));
                if (!payset.next())
                    return null;
                getStatement().executeUpdate("UPDATE payment SET `status`=1 WHERE payment_id='" + paymentId + "'");
                Map<String, Object> chargeParams = new HashMap<>();
                int price = payset.getInt("price");
                chargeParams.put("amount", price);
                chargeParams.put("currency", "EUR");
                chargeParams.put("customer", "cl_FDPJB9A42LZMY38I78PNNFYPM");
                Map<String, String> initialMetadata = new HashMap<>();
                initialMetadata.put("payment_id", paymentId);
                chargeParams.put("metadata", initialMetadata);
                attributes.put("price", price);
                attributes.put("pprice", price / 100);
                return new ModelAndView(attributes, "complete.ftl");
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }, engine);
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
