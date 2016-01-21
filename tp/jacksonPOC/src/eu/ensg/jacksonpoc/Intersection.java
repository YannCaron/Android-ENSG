package eu.ensg.jacksonpoc;/**
 * Copyright (C) 21/01/16 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

/**
 * The Intersection definition.
 */
public class Intersection {
	public double lng, lat;
	public String street1, street2;

	@Override
	public String toString() {
		return "Intersection{" +
				"lng=" + lng +
				", lat=" + lat +
				", street1='" + street1 + '\'' +
				", street2='" + street2 + '\'' +
				'}';
	}
}
