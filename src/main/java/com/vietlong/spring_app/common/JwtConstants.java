package com.vietlong.spring_app.common;

public class JwtConstants {

    public static final String ISSUER = "https://vietlong.example.com";
    public static final String AUDIENCE = "https://vietlong.example.com";
    public static final String CLIENT_ID = "vietlong.example";
    public static final String SCOPE_CLAIM = "scope";
    public static final String EMAIL_CLAIM = "email";
    public static final String USER_ID_CLAIM = "userId";

    public static final String ALGORITHM = "HS512";

    private JwtConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}