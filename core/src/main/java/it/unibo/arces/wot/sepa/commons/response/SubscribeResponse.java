/* This class represents the response to a subscribe request
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package it.unibo.arces.wot.sepa.commons.response;

import com.google.gson.JsonPrimitive;

// TODO: Auto-generated Javadoc
/**
 * This class represents the response to a SPARQL 1.1 Subscribe (see SPARQL 1.1
 * Subscription Language)
 *
 * The JSON serialization is the following:
 *
 * {"subscribed" : "SPUID"}
 *
 */

public class SubscribeResponse extends Response {

	/**
	 * Instantiates a new subscribe response.
	 *
	 * @param token
	 *            the token
	 * @param spuid
	 *            the spuid
	 */
	public SubscribeResponse(Integer token, String spuid) {
		super(token);

		if (spuid != null)
			json.add("subscribed", new JsonPrimitive(spuid));
	}

	/**
	 * Instantiates a new subscribe response.
	 *
	 * @param token
	 *            the token
	 * @param spuid
	 *            the spuid
	 * @param alias
	 *            the alias
	 */
	public SubscribeResponse(Integer token, String spuid, String alias) {
		super(token);

		if (spuid != null)
			json.add("subscribed", new JsonPrimitive(spuid));
		if (alias != null)
			json.add("alias", new JsonPrimitive(alias));
	}

	/**
	 * Instantiates a new subscribe response.
	 *
	 * @param spuid
	 *            the spuid
	 */
	public SubscribeResponse(String spuid) {
		super();

		if (spuid != null)
			json.add("subscribed", new JsonPrimitive(spuid));
	}

	/**
	 * Instantiates a new subscribe response.
	 */
	public SubscribeResponse() {
		super();
	}

	/**
	 * Instantiates a new subscribe response.
	 *
	 * @param spuid
	 *            the spuid
	 * @param alias
	 *            the alias
	 */
	public SubscribeResponse(String spuid, String alias) {
		super();

		if (spuid != null)
			json.add("subscribed", new JsonPrimitive(spuid));
		if (alias != null)
			json.add("alias", new JsonPrimitive(alias));
	}

	/**
	 * Gets the spuid.
	 *
	 * @return the spuid
	 */
	public String getSpuid() {
		if (json.get("subscribed") == null)
			return "";
		return json.get("subscribed").getAsString();
	}

	/**
	 * Gets the alias.
	 *
	 * @return the alias
	 */
	public String getAlias() {
		if (json.get("alias") == null)
			return "";
		return json.get("alias").getAsString();
	}
}
