/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca/Self.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.client.views;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Form;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca.self.client.tiles.Endpoint;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Specs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.metreeca.self.shared.beans.Specs.Collection;
import static com.metreeca.self.shared.beans.Specs.Resource;
import static com.metreeca.self.shared.beans.Specs.Springboard;


public final class EndpointInfo extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("EndpointInfo.xml") TextResource form();

	}


	private final List<String> endpoints=new ArrayList<>();


	public EndpointInfo() {
		root(resources.form().getText())

				.<Form>as().wire()

				.forced(true)

				.submit(new Action<Event>() {

					@Override public void execute(final Event e) { connect(); }

				});
	}

	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public EndpointInfo endpoints(final Collection<String> endpoints) {

		if ( endpoints == null ) {
			throw new NullPointerException("null endpoints");
		}

		this.endpoints.clear();
		this.endpoints.addAll(endpoints);

		return this;
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public EndpointInfo open() {

		final String endpoint=endpoints.isEmpty() ? "" : endpoints.get(0);

		$endpoint()

				.endpoint(endpoint)
				.endpoints(endpoints);

		root().<Overlay>as()

				.modal(true)
				.open()
				.align(Overlay.Align.Center, Overlay.Align.Center);

		return this;
	}

	private void connect() {

		final String endpoint=$endpoint().endpoint();
		final String entity=$entity().value(true);
		final String type=$type().value(true);
		final String label=$label().value(true);

		root().async(new Report()
				.setEndpoint(endpoint)
				.setSpecs(entity.isEmpty() ? new Specs()
						: type.equals("collection") ? Collection(entity, label)
						: type.equals("resource") ? Resource(entity, label)
						: Springboard(label)));
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Endpoint $endpoint() {
		return root().find("[name=endpoint]").as();
	}

	private Tile $type() {
		return root().find("[name=type]");
	}

	private Tile $entity() {
		return root().find("[name=entity]");
	}

	private Tile $label() {
		return root().find("[name=label]");
	}

}
