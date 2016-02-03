package eu.ensg.spatialite.geom; /**
 * Copyright (C) 18/12/15 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The eu.ensg.spatialite.geom.LineString definition.
 */
public class Polygon extends LineString {

    private final List<XYList> interiors;

    public Polygon() {
        super(true);
        this.interiors = new ArrayList<>();
    }

    Polygon(XYList exterior) {
        super(exterior);
        this.interiors = new ArrayList<>();
    }

    public static Polygon unMarshall(StringBuilder string) {

        // 'POLYGON'
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "POLYGON")) return null;

        // '('
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "(")) return null;

        // <exterior>
        XYList exterior = XYList.unMarshall(string, true);
        if (exterior == null) return null;
        Polygon polygon = new Polygon(exterior);

        // <interior>*
        while (string.length() > 0 && Parse.nextSymbol(string, ",")) {

            // ','
            Parse.removeBlanks(string);
            Parse.consumeSymbol(string, ",");

            // <interior>
            Parse.removeBlanks(string);
            XYList interior = XYList.unMarshall(string, true);
            if (interior == null) return null;
            polygon.addInterior(interior);

        }

        // ')'
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, ")")) return null;
        return polygon;
    }

    public int interiorsSize() {
        return interiors.size();
    }

    public boolean addInterior(XYList xyList) {
        return interiors.add(xyList);
    }

    public boolean removeAllInteriors(Collection<?> c) {
        return interiors.removeAll(c);
    }

    @Override
    public void marshall(StringBuilder string) {
        string.append("POLYGON");
        string.append(' ');
        string.append('(');
        getCoordinates().marshall(string);

        for (XYList interior : interiors) {
            string.append(", ");
            interior.marshall(string);
        }

        string.append(')');
    }
}
