package io.elves.core;

public class ElvesConstants {
    public static final int DEFAULT_PORT = 10000;
    public static final String FAVICON_PATH = "/favicon.ico";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String SERVER_IP_KEY = "serverIp";
    public static final byte[] DELIMITER = "$".getBytes();
    public static final String PROFILES_ACTIVE = "elves.profiles.active";
    public static final String TEXT_PLAIN_CODER = "text/plain";
    public static final String JSON_CODER = "application/json";
    public static final String FORM_DATA_CODER = "multipart/form-data";
    public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private ElvesConstants() {

    }
}
