package se.kth.csc.stayawhile.api;

public class APIException extends RuntimeException {
    public APIException(Throwable e) {
        super(e);
    }
}
