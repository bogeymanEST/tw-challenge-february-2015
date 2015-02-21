package com.github.bogeymanest.twchallenge;

public class ResponsePaymentCreate extends Response {
    public String paymentId;
    public String paymentUrl;
    public String statusUrl;
    public ResponsePaymentCreate(String paymentId, String paymentUrl, String statusUrl) {
        super(true);
        this.paymentId = paymentId;
        this.paymentUrl = paymentUrl;
        this.statusUrl = statusUrl;
    }
}
