package se.kth.csc.stayawhile;

public class APIException extends RuntimeException {
    public APIException(Throwable e) {
        super(e);
    }
}
