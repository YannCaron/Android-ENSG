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
 * The eu.ensg.spatialite.geom.XYList definition.
 */
public class XYList implements Marshallable {

    // attribute
    private final List<XY> coords;
    private final boolean isClosed;

    // constructor and factory
    public XYList(boolean closed) {
        coords = new ArrayList<>();
        this.isClosed = closed;
    }

    public static XYList unMarshall(StringBuilder string, boolean closed) {

        // '('
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, "(")) return null;

        // (<xy> (',' <xy>)*)?
        XYList list = new XYList(closed);
        while (string.length() > 0 && !(Parse.nextSymbol(string, ")") || (Parse.nextSymbol(string, ",")))) {

            // <xy>
            Parse.removeBlanks(string);
            XY xy = XY.unMarshall(string);
            if (xy == null) return null;
            list.add(xy);

            // ','
            Parse.removeBlanks(string);
            if (!Parse.consumeSymbol(string, ",")) break;

        }

        // ')'
        Parse.removeBlanks(string);
        if (!Parse.consumeSymbol(string, ")")) return null;
        return list;
    }

    // accessor
    public List<XY> getCoords() {
        return coords;
    }

    public int size() {
        return coords.size();
    }

    // method
    public boolean add(XY xy) {
        return coords.add(xy);
    }

    public boolean removeAll(Collection<?> c) {
        return coords.removeAll(c);
    }

    // method implement
    @Override
    public void marshall(StringBuilder string) {
        string.append('(');
        boolean tail = false;
        for (XY coord : coords) {
            if (tail) {
                string.append(", ");
            }
            tail = true;

            coord.marshall(string);
        }

        if (isClosed && coords.size() > 0 && !coords.get(0).equals(coords.get(coords.size() - 1))) {
            string.append(", ");
            coords.get(0).marshall(string);
        }
        string.append(')');
    }
}
