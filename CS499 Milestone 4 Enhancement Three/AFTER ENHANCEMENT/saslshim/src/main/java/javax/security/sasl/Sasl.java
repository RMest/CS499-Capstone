package javax.security.sasl;

import java.util.Map;

public final class Sasl {
    private Sasl() {}

    public static SaslClient createSaslClient(String[] mechs, String authzid,
                                              String protocol, String serverName, Map<String,?> props, Object callbackHandler) {
        return null;
    }

    public static SaslServer createSaslServer(String mech, String protocol,
                                              String serverName, Map<String,?> props, Object callbackHandler) {
        return null;
    }
}