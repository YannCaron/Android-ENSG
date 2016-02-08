package eu.ensg.spatialite.geom; /**
 * Copyright (C) 18/12/15 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/


import com.google.android.gms.maps.model.LatLng;

/**
 * The eu.ensg.spatialite.geom.Point definition.
 */
public class Point extends Geometry {

    private final XY coordinate;

    public Point(XY coordinate) {
        this.coordinate = coordinate;
    }

    public Point(double x, double y) {
        this.coordinate = new XY(x, y);
    }

    public LatLng toLatLng() {
        return new LatLng(coordinate.getY(), coordinate.getX());
    }

    public static Point unMarshall(String string) {
        if (string == null) return null;
        return unMarshall(new StringBuilder(string));
    }

    public static Point unMarshall(StringBuilder string) {
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "POINT")) return null;
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "(")) return null;
        XY xy = XY.unMarshall(string);
        if (xy == null) return null;
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, ")")) return null;
        return new Point(xy);
    }

    public XY getCoordinate() {
        return coordinate;
    }

    @Override
    public void marshall(StringBuilder string) {
        string.append("POINT");
        string.append(' ');
        string.append('(');
        coordinate.marshall(string);
        string.append(')');
    }

}
