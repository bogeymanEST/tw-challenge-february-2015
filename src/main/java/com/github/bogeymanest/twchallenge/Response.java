package com.github.bogeymanest.twchallenge;

public abstract class Response {
    public boolean success;

    public Response(boolean success) {
        this.success = success;
    }
}
