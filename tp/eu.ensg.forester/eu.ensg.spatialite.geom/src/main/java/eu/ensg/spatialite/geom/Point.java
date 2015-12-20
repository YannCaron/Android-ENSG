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

	public Point(int srid, XY coordinate) {
		super(srid);
		this.coordinate = coordinate;
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
