package com.github.bogeymanest.twchallenge;

public class ResponsePaymentCreate extends Response {
    public String paymentId;
    public String url;
    public ResponsePaymentCreate(String paymentId, String url) {
        super(true);
        this.paymentId = paymentId;
        this.url = url;
    }
}
