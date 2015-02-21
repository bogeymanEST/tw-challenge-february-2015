package com.github.bogeymanest.twchallenge;

public class ResponseRecipientCreate extends Response {
    private String recipientId;
    private String email;

    public ResponseRecipientCreate(String recipientId, String email) {
        super(true);
        this.recipientId = recipientId;
        this.email = email;
    }
}
