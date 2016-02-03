package eu.ensg.jacksonpoc;/**
 * Copyright (C) 21/01/16 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

/**
 * The IntersectionContainer definition.
 */
public class IntersectionContainer {

	//@JsonProperty("credits")
	public double version;
	public Intersection intersection;

	@Override
	public String toString() {
		return "IntersectionContainer{" +
				"version=" + version +
				", intersection=" + intersection +
				'}';
	}
}
