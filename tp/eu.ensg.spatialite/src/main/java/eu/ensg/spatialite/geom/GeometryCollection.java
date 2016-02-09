package eu.ensg.spatialite.geom;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cyann on 29/01/16.
 */
public class GeometryCollection extends Geometry {

    private final List<Geometry> geometries;

    public GeometryCollection() {
        this.geometries = new LinkedList<>();
    }

    public void addGeometry(Geometry geometry) {
        this.geometries.add(geometry);
    }

    @Override
    public void marshall(StringBuilder string) throws BadGeometryException {
        string.append("GEOMETRYCOLLECTION ");
        string.append("(");

        boolean tail = false;
        for (Geometry geometry : geometries) {
            if (tail) string.append(", ");
            geometry.marshall(string);
            tail = true;
        }

        string.append(")");
    }

    public static GeometryCollection unMarshall(String string) {
        if (string == null) return null;
        return unMarshall(new StringBuilder(string));
    }

    public static GeometryCollection unMarshall(StringBuilder string) {

        // 'GEOMETRYCOLLECTION'
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "GEOMETRYCOLLECTION")) return null;

        GeometryCollection geometryCollection = new GeometryCollection();

        // '('
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "(")) return null;

        // <geometry>*
        while (string.length() > 0) {

            // <geometry>
            Parse.removeBlanks(string);
            Geometry geometry;
            geometry = GeometryCollection.unMarshall(string);
            if (geometry == null)
                geometry = Point.unMarshall(string);
            if (geometry == null)
                geometry = LineString.unMarshall(string);
            if (geometry == null)
                geometry = Polygon.unMarshall(string);
            if (geometry == null) return null;
            geometryCollection.addGeometry(geometry);

            if (!Parse.nextSymbol(string, ",")) break;

            // ','
            Parse.removeBlanks(string);
            Parse.consumeSymbol(string, ",");

        }

        // ')'
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, ")")) return null;
        return geometryCollection;
    }
}
