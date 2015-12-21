package eu.ensg.spatialite.geom; /**
 * Copyright (C) 18/12/15 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/


/**
 * The eu.ensg.spatialite.geom.Point definition.
 */
public class Point extends Geometry {

	private final XY coordinate;

	public Point(XY coordinate) {
		this.coordinate = coordinate;
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


	public static Point unMarshall(StringBuilder string) {
		Utils.removeBlanks(string);
		if (!Utils.consumeSymbol(string, "POINT")) return null;
		Utils.removeBlanks(string);
		if (!Utils.consumeSymbol(string, "(")) return null;
		XY xy = XY.unMarshall(string);
		if (xy == null) return null;
		Utils.removeBlanks(string);
		if (!Utils.consumeSymbol(string, ")")) return null;
		return new Point(xy);
	}

}
