package eu.ensg.spatialite.geom; /**
 * Copyright (C) 18/12/15 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

/**
 * The eu.ensg.spatialite.geom.Marshallable definition.
 */
public interface Marshallable {

	public static class Utils {

		public static boolean consumeSymbol(StringBuilder string, String symbol) {
			if (string.indexOf(symbol) != 0) return false;
			string.delete(0, symbol.length());
			return true;
		}

		public static Double consumeDouble(StringBuilder string) {
			StringBuilder numString = new StringBuilder();
			while (string.length() > 0) {
				String chr = String.valueOf(string.charAt(0));
				if (!"0123456789.".contains(chr)) break;
				numString.append(chr);
				string.deleteCharAt(0);
			}
			return Double.valueOf(numString.toString());
		}

		public static void removeBlanks(StringBuilder string) {
			while (string.indexOf(" ") == 0) {
				string.deleteCharAt(0);
			}
		}

	}

	// see at http://www.gaia-gis.it/gaia-sins/spatialite-cookbook/html/wkt-wkb.html
	void marshall(StringBuilder string);
	//boolean unMarshall(String string, int index);

}
