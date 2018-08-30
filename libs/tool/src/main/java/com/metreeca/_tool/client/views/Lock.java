/*
 * Copyright © 2013-2018 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Metreeca/Self. If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca._tool.client.views;

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;
import com.metreeca._tool.client.Tool.Bus;

import static com.metreeca._tile.client.Tile.$;


public final class Lock extends View {

	private final Action<Event> cancel=new Action<Event>() {
		@Override public void execute(final Event e) {

			// react only to events on body::after below overlays (see visuals.css)
			// e.g. to prevent cancellation when user interacts with PathPicker

			if ( e.targeting() ) {
				try {
					cancel();
				} finally {
					e.cancel();
					e.stop();
				}
			}
		}
	};


	public Lock() {
		root("<script/>")

				.<Bus<?, ?>>as()

				.error(new Action<Throwable>() {
					@Override public void execute(final Throwable throwable) {
						root().<Bus<?, ?>>as().activity((Boolean)null);
					}
				})

				.activity(new Action.Monitor() {
					@Override protected void active(final boolean active) { lock(active); }
				});
	}


	private void lock(final boolean lock) {
		if ( lock ) {
			$("body").is("active", true) // body.active is styled in visuals.css to share defs with overlays // !!! review
					.bind("mousedown", cancel, true);
		} else {
			$("body").is("active", false)
					.drop("mousedown", cancel, true);
		}
	}

	private void cancel() {
		try {
			new Dialog().humanized().message("cancelled").open();
		} finally {
			root().<Bus<?, ?>>as().activity((Boolean)null);
		}
	}

}
