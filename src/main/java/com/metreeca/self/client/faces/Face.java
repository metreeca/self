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

package com.metreeca.self.client.faces;

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.View;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.shared.beans.Term;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;


public abstract class Face<T extends Face<T>> extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Face.css") TextResource skin();

	}


	protected Face() {
		root("<div/>").hide()

				.skin(resources.skin().getText())

				.<Bus>as()

				.resize(new Action<Void>() {
					@Override public void execute(final Void v) { render(); }
				});
	}


	protected abstract T self();


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public T locked(final boolean locked) { // !!! review read-only support
		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	protected boolean open(final Term term) {
		return root().fire(term);
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected T render() {
		return self();
	}

}
