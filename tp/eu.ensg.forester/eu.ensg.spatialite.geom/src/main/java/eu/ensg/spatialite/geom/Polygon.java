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
public class Polygon implements Marshallable {

	private final XYList exterior;
	private final List<XYList> interiors;

	public Polygon() {
		this.exterior = new XYList();
		this.interiors = new ArrayList<>();
	}

	public XYList getExterior() {
		return exterior;
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
		exterior.marshall(string);

		for (XYList interior : interiors) {
			string.append(", ");
			interior.marshall(string);
		}

		string.append(')');
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		marshall(string);
		return string.toString();
	}
}
