package eu.ensg.spatialite.geom; /**
 * Copyright (C) 18/12/15 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

/**
 * The eu.ensg.spatialite.geom.LineString definition.
 */
public class LineString extends Geometry {

    private final XYList coordinates;

    public LineString() {
        this.coordinates = new XYList(false);
    }

    LineString(XYList coordinates) {
        this.coordinates = coordinates;
    }

    protected LineString(boolean closed) {
        this.coordinates = new XYList(closed);
    }

    public static LineString unMarshall(String string) {
        if (string == null) return null;
        return unMarshall(new StringBuilder(string));
    }

    public static LineString unMarshall(StringBuilder string) {

        // 'LINESTRING'
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "LINESTRING")) return null;

        XYList list = XYList.unMarshall(string, false);

        return new LineString(list);
    }

    public XYList getCoordinates() {
        return coordinates;
    }

    public void addCoordinate(XY coordinate) {
        coordinates.add(coordinate);
    }

    public void addCoordinate(float x, float y) {
        coordinates.add(new XY(x, y));
    }

    @Override
    public void marshall(StringBuilder string) throws BadGeometryException {
        string.append("LINESTRING");
        string.append(' ');
        coordinates.marshall(string);
    }

}
