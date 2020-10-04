package pro.belbix.tim.exceptions;

public class TIMRuntimeException extends RuntimeException {
    public TIMRuntimeException() {
    }

    public TIMRuntimeException(String message) {
        super(message);
    }

    public TIMRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TIMRuntimeException(Throwable cause) {
        super(cause);
    }

    public TIMRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
