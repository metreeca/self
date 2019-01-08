/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.client.tiles;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Combo;
import com.metreeca._tile.client.plugins.Menu;

import java.util.List;

import static com.metreeca._tile.client.Tile.$;


public final class Endpoint extends Plugin {

	private static final String CaptiveKey="captive";
	private static final String EndpointKey="endpoint";
	private static final String EndpointsKey="endpoints";

	private static final String HTTPPrefix="http://";
	private static final String HTTPSPrefix="https://";


	private static String expand(final String endpoint) {

		if ( endpoint == null ) {
			throw new NullPointerException("null endpoint");
		}

		return endpoint.startsWith(HTTPPrefix) ? endpoint
				: endpoint.startsWith(HTTPSPrefix) ? endpoint
				: endpoint.isEmpty() ? ""
				: HTTPPrefix+endpoint;
	}

	private static String compact(final String endpoint) {

		if ( endpoint == null ) {
			throw new NullPointerException("null endpoint");
		}

		return endpoint.startsWith(HTTPPrefix) ? endpoint.substring(HTTPPrefix.length())
				: endpoint.startsWith(HTTPSPrefix) ? endpoint.substring(HTTPSPrefix.length())
				: endpoint;
	}


	protected Endpoint() {}


	public String endpoint() {

		final String title=this.<Tile>as().title();

		return expand(title != null ? title.trim() : "");
	}

	public Endpoint endpoint(final String endpoint) {
		return this.<Tile>as()

				.value(compact(endpoint))
				.title(endpoint)

				.<Endpoint>as().wire();
	}

	public Endpoint endpoints(final List<String> endpoints) {
		return this.<Tile>as().set(EndpointsKey, endpoints).<Endpoint>as().wire();
	}


	private Endpoint wire() {
		return this.<Tile>as().each(new Lambda<Tile>() {
			@Override public Tile apply(final Tile tile) {

				return tile.get(EndpointKey, false) ? tile : tile.set(EndpointKey, true)

						.attribute("pattern", "(\\s*)(https?://)?(\\S+)(\\s*)") // (very) rough URL validation

						.focus(new Action<Event>() {
							@Override public void execute(final Event e) {
								e.current().value(e.current().title());
							}
						})

						.blur(new Action<Event>() {
							@Override public void execute(final Event e) {

								final String endpoint=expand(e.current().value().trim());

								e.current()
										.title(endpoint) // complete url
										.value(compact(endpoint)); // prefix-stripped url
							}
						})

						.with(new Lambda<Tile>() {
							@Override public Tile apply(final Tile tile) {

								tile.<Combo>as()

										.wire($("<menu/>")

												.click(new Action<Event>() {
													@Override public void execute(final Event e) {

														final String endpoint=e.target().title();

														tile
																.title(endpoint)
																.value(compact(endpoint))
																.change(); // signal user change
													}
												})

												.bind("open", new Action<Event>() {
													@Override public void execute(final Event e) {

														e.current().clear();

														final String current=tile.title();

														for (final String endpoint : tile.<Iterable<String>>get(EndpointsKey)) {
															e.current().append($("<command/>")
																	.title(endpoint)
																	.text(compact(endpoint))
																	.selected(current.equals(endpoint)));
														}
													}
												})

												.<Menu>as());

								return tile;
							}
						});

			}
		}).as();
	}

}
