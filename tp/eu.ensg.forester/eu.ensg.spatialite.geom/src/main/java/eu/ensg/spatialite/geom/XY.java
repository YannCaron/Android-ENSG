package eu.ensg.spatialite.geom; /**
 * Copyright (C) 18/12/15 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

/**
 * The eu.ensg.spatialite.geom.XY definition.
 */
public class XY implements Marshallable {

	private final double x, y;

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public XY(double x, double y) {
		this.x = x;
		this.y = y;
	}

	private void marshallNumber(StringBuilder string, double number) {
		if (number == Math.round(number))
			string.append((int)number);
		else
			string.append(number);
	}

	@Override
	public void marshall(StringBuilder string) {
		marshallNumber(string, x);
		string.append(' ');
		marshallNumber(string, y);
	}

	public static XY unMarshall(StringBuilder string) {
		Utils.removeBlanks(string);
		Double x = Utils.consumeDouble(string);
		if (x == null) return null;
		Utils.removeBlanks(string);
		Double y = Utils.consumeDouble(string);
		if (y == null) return null;
		return new XY(x, y);
	}

}
