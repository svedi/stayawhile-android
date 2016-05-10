package se.kth.csc.stayawhile.api.http;

public class APIException extends RuntimeException {
    public APIException(Throwable e) {
        super(e);
    }
}
