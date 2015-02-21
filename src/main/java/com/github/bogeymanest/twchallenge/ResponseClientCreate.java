package com.github.bogeymanest.twchallenge;

public class ResponseClientCreate extends Response {
    public String clientId;
    public String deleteId;
    public String email;

    public ResponseClientCreate(String clientId, String deleteId, String email) {
        super(true);
        this.clientId = clientId;
        this.deleteId = deleteId;
        this.email = email;
    }
}
