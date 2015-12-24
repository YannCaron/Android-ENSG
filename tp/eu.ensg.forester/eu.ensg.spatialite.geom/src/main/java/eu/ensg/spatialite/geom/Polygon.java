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

	public static Polygon unMarshall(StringBuilder string) {
		Utils.removeBlanks(string);
		if (!Utils.consumeSymbol(string, "POLYGON")) return null;
		Utils.removeBlanks(string);
		if (!Utils.consumeSymbol(string, "(")) return null;

		XYList exterior = XYList.unMarshall(string, true);
		if (exterior == null) return null;
		Polygon polygon = new Polygon(exterior);

		// TODO: Unmarshall interiors

		Utils.removeBlanks(string);
		if (!Utils.consumeSymbol(string, ")")) return null;
		return polygon;
	}
}
