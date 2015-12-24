package eu.ensg.spatialite.geom;

/**
 * Created by cyann on 20/12/15.
 */
public abstract class Geometry implements Marshallable {

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        marshall(string);
        return string.toString();
    }

    public String toSpatialiteQuery(int srid) {
        StringBuilder string = new StringBuilder();
        string.append("GeomFromText('");
        marshall(string);
        string.append("', " + srid + ")");
        return string.toString();
    }

}
