package javax.security.sasl;

public interface SaslServer {
    String getMechanismName();
    byte[] evaluateResponse(byte[] response) throws SaslException;
    String getAuthorizationID();
    boolean isComplete();
    byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException;
    byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException;
    Object getNegotiatedProperty(String propName);
    void dispose() throws SaslException;
}