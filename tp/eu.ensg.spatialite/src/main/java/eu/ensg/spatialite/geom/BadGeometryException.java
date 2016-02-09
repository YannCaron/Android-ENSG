package eu.ensg.spatialite.geom;

/**
 * Created by cyann on 09/02/16.
 */
public class BadGeometryException extends Exception {

    public BadGeometryException() {
    }

    public BadGeometryException(String detailMessage) {
        super(detailMessage);
    }

    public BadGeometryException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BadGeometryException(Throwable throwable) {
        super(throwable);
    }

}
