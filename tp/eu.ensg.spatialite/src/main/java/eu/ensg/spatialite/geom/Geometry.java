package eu.ensg.spatialite.geom;

/**
 * Created by cyann on 20/12/15.
 */
public abstract class Geometry implements Marshallable {

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        try {
            marshall(string);
        } catch (BadGeometryException e) {
            e.printStackTrace();
        }
        return string.toString();
    }

    public String toSpatialiteQuery(int srid) throws BadGeometryException {
        StringBuilder string = new StringBuilder();
        string.append("ST_GeomFromText('");
        marshall(string);
        string.append("', " + srid + ")");
        return string.toString();
    }

}
