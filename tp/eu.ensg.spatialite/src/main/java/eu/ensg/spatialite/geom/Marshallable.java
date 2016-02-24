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

	/**
	 * Abstract class Parse that contains necessarily method for geometry parsing (unMarshalling)
	 */
	class Parse {

		/**
		 * Recognize symbol and consume it.
		 * @param string the input string (string builder) to parse.
		 * @param symbol the symbol to recognize
		 * @return true if parsing is ok.
		 */
		public static boolean consumeSymbol(StringBuilder string, String symbol) {
			if (string.indexOf(symbol) != 0) return false;
			string.delete(0, symbol.length());
			return true;
		}

		/**
		 * Recognize double and consume it.
		 * @param string the input string (string builder) to parse.
		 * @return true if parsing is ok.
		 */
		public static Double consumeDouble(StringBuilder string) {
			StringBuilder numString = new StringBuilder();
			while (string.length() > 0) {
				String chr = String.valueOf(string.charAt(0));

				// thanks alkra for finding the bug
				if (!"-0123456789.".contains(chr)) break;
				numString.append(chr);
				string.deleteCharAt(0);
			}
			return Double.valueOf(numString.toString());
		}

		/**
		 * Remove unnecessary blanks in the input string.
		 * @param string the input string (string builder) to parse.
		 */
		public static void removeBlanks(StringBuilder string) {
			while (string.indexOf(" ") == 0) {
				string.deleteCharAt(0);
			}
		}

		/**
		 * Look at the next symbol and recognize it without consuming it. LL(1) parser, first symbol is deterministic for the choice.
		 *
		 * @param string the input string (string builder) to parse.
		 * @param symbol the symbol to recognize
		 * @return true if next symbol corresponding.
		 */
		public static boolean nextSymbol(StringBuilder string, String symbol) {
			return string.indexOf(symbol) == 0;
		}

	}

	/**
	 * The abstract method that transform Geometry structure into string understandable by GeomFromText() spatialite function.
	 * see at http://www.gaia-gis.it/gaia-sins/spatialite-cookbook/html/wkt-wkb.html
	 * @param string
	 */
	void marshall(StringBuilder string) throws BadGeometryException;

}
