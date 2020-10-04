package pro.belbix.tim.exceptions;

public class TIMRetryException extends TIMException {

    public TIMRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public TIMRetryException(String message) {
        super(message);
    }
}
