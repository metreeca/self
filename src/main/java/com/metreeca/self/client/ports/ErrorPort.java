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

package com.metreeca.self.client.ports;

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.views.Dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;


public final class ErrorPort extends View {

	private boolean reporting; // prevent multiple dialogs


	public ErrorPort() {
		root("<script/>")

				.error(new GWT.UncaughtExceptionHandler() {
					@Override public void onUncaughtException(final Throwable throwable) {
						root().<Bus>as().error(throwable);
					}
				})

				.<Bus>as()

				.error(new Action<Throwable>() {
					@Override public void execute(final Throwable throwable) {

						root().error(ErrorPort.class, "internal error", throwable);

						// !!! report to server (and deobfuscate)

						if ( throwable instanceof JavaScriptException ) {

							root().log(((JavaScriptException)throwable).getThrown());

						} else {

							final StringBuilder trace=new StringBuilder(1000);

							for (final StackTraceElement element : throwable.getStackTrace()) {
								trace.append(element).append('\n');
							}

							root().log(trace);

						}

						if ( !reporting ) {
							try {

								new Dialog()

										.message("Internal\u00A0Error\u00A0\u2639")
										.details("It shouldn't happen:\napologies for the\ninconvenience…")

										// !!! automatic reporting / option to report

										.action("Back to Work", new Action<Event>() {
											@Override public void execute(final Event event) { reporting=false; }
										})

										.open();

							} finally {
								reporting=true;
							}
						}

					}
				});
	}

}
