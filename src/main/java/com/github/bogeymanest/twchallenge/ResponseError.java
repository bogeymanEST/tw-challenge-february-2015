package com.github.bogeymanest.twchallenge;

public class ResponseError extends Response {
    public String error;

    public ResponseError(String error) {
        super(false);
        this.error = error;
    }
}
