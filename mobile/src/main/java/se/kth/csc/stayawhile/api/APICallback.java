package se.kth.csc.stayawhile.api;

/**
 * Callback for an API task
 */
public interface APICallback {

    /**
     * Called when the API task is performed
     * @param result the result of the task
     */
    void r(String result);
}
