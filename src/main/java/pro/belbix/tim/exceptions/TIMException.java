package pro.belbix.tim.exceptions;

public class TIMException extends Exception {
    public TIMException() {
    }

    public TIMException(String message) {
        super(message);
    }

    public TIMException(String message, Throwable cause) {
        super(message, cause);
    }

    public TIMException(Throwable cause) {
        super(cause);
    }

    public TIMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
