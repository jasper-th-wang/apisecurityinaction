package com.manning.apisecurityinaction.token;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Optional;

import javax.crypto.Mac;

import spark.Request;

public class HmacTokenStore implements TokenStore {
    private final TokenStore delegate;
    private final Key macKey;

    public HmacTokenStore(TokenStore delegate, Key macKey) {
        this.delegate = delegate;
        this.macKey = macKey;
    }

    @Override
    public String create(Request request, Token token) {
        // call the real TokenStore to generate token ID
        var tokenId = delegate.create(request, token);
        // then use Hmac to calculate the authentication tag
        var tag = hmac(tokenId);

        return tokenId + '.' + Base64url.encode(tag);
    }

    @Override
    public Optional<Token> read(Request request, String tokenId) {
        // if hmac tag not present -> return null
        var index = tokenId.lastIndexOf('.');
        if (index == -1) {
            return Optional.empty();
        }

        // get real token id by removing the tag
        var realTokenId = tokenId.substring(0, index);

        // compare provided tag in request versus actual tag
        var providedTag = Base64url.decode(tokenId.substring(index + 1));
        var computedTag = hmac(realTokenId);
        if (!MessageDigest.isEqual(providedTag, computedTag)) {
            return Optional.empty();
        }

        // if tag is valid -> call the real token store with the original token id
        return delegate.read(request, realTokenId);
    }

    @Override
    public void revoke(Request request, String tokenId) {
        // TODO Auto-generated method stub

    }

    // get Hmac tag of inputted token
    private byte[] hmac(String tokenId) {
        try {
            var mac = Mac.getInstance(macKey.getAlgorithm());
            mac.init(macKey);
            return mac.doFinal(tokenId.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
