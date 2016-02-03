package eu.ensg.jacksonpoc;/**
 * Copyright (C) 21/01/16 Yann Caron aka cyann
 * <p/>
 * Cette œuvre est mise à disposition sous licence Attribution -
 * Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France.
 * Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/
 * ou écrivez à Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 **/

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * The Main definition.
 */
public class Main {

	public static final String JSON_DATA = "{\"credits\":\"1.0\", \"intersection\": {\"lng\":\"-122.180842\", \"lat\":\"37.450649\", \"street1\":\"Roble Ave\", \"street2\":\"Curtis St\"}}";

	abstract class MixIn {
		@JsonProperty("credits") double version;
	}

	public static void main(String[] args) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixInAnnotations(IntersectionContainer.class, MixIn.class);
		IntersectionContainer ic = mapper.readValue(JSON_DATA, IntersectionContainer.class);

		System.out.println(ic.toString());
	}

}
