package com.manning.apisecurityinaction.controller;

import java.time.temporal.ChronoUnit;

import org.json.JSONObject;
import com.manning.apisecurityinaction.token.TokenStore;
import static spark.Spark.*;
import spark.*;

import static java.time.Instant.now;

import java.time.Instant;

public class TokenController {

    private final TokenStore tokenStore;

    public TokenController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public JSONObject login(Request request, Response response) {
        var tokenId = request.headers("Authorization");
        if (tokenId == null || !tokenId.startsWith("Bearer ")) {
            throw new IllegalArgumentException("missing token header");
        }

        tokenId = tokenId.substring(7);

        tokenStore.revoke(request, tokenId);
        response.status(200);
        return new JSONObject();
    }

    public JSONObject logout(Request request, Response response) {
        var tokenId = request.headers("X-CSRF-Token");
        if (tokenId == null) {
            throw new IllegalArgumentException("missing token header");
        }

        tokenStore.revoke(request, tokenId);
        // Success response
        response.status(200);
        return new JSONObject();
    }

    // NOTE: cookie version
    // public JSONObject logout(Request request, Response response) {
    //     var tokenId = request.headers("X-CSRF-Token");
    //     if (tokenId == null) {
    //         throw new IllegalArgumentException("missing token header");
    //     }

    //     tokenStore.revoke(request, tokenId);
    //     // Success response
    //     response.status(200);
    //     return new JSONObject();
    // }


    public void validateToken(Request request, Response response) {
        var tokenId = request.headers("Authorization");
        if (tokenId == null || !tokenId.startsWith("Bearer ")) {
            return;
        }

        tokenId = tokenId.substring(7);

        tokenStore.read(request, tokenId).ifPresent(token -> {
            if (Instant.now().isBefore(token.expiry)) {
                request.attribute("subject", token.username);

                // apply all attribute of this token into the request context
                token.attributes.forEach(request::attribute);
            } else {
                // expired -> respond with standard error response
                response.header("WWW-Authenticate", "Bearer error=\"invalid_token\"," + "error_description=\"Expired\"");
                halt(401);
            }
        });

    }

    // NOTE: cookie version
    // public void validateToken(Request request, Response response) {
    //     var tokenId = request.headers("X-CSRF-Token");
    //     if (tokenId == null) return;

    //     tokenStore.read(request, tokenId).ifPresent(token -> {
    //         if (now().isBefore(token.expiry)) {
    //             request.attribute("subject", token.username);
    //             token.attributes.forEach(request::attribute);
    //         }
    //     });
    //     // WARNING: CSRF attack possible
    //     // tokenStore.read(request, null).ifPresent(token -> {
    //     //     if (now().isBefore(token.expiry)) {
    //     //         request.attribute("subject", token.username);
    //     //         token.attributes.forEach(request::attribute);
    //     //     }
    //     // });
    // }
}
