package com.github.bogeymanest.twchallenge;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;

public class StripeApi extends DefaultApi20 {
    private static final String AUTHORIZE_URL = "https://connect.stripe.com/oauth/authorize?client_id=%s&response_type=code";

    @Override
    public String getAccessTokenEndpoint() {
        return "https://connect.stripe.com/oauth/token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZE_URL, config.getApiKey());
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
}
