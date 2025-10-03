package javax.security.sasl;

public class AuthenticationException extends SaslException {
    public AuthenticationException() { super(); }
    public AuthenticationException(String message) { super(message); }
    public AuthenticationException(String message, Throwable cause) { super(message, cause); }
}