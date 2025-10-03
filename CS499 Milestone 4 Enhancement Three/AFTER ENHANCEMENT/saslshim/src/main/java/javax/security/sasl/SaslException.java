package javax.security.sasl;

public class SaslException extends Exception {
    public SaslException() {}
    public SaslException(String msg) { super(msg); }
    public SaslException(String msg, Throwable cause) { super(msg, cause); }
}