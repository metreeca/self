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
import com.metreeca._tool.client.views.Dialog;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.sparql.JSONTable;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.sparql.Query;
import com.metreeca.self.shared.sparql.Table;

import com.google.gwt.http.client.*;

import java.util.Collection;
import java.util.HashSet;

import static com.metreeca._tile.client.Plugin.clip;


public final class SPARQLPort extends View {

	private static final int Timeout=30*1000; // default query timeout [ms] {0 >> no timeout}


	private Report report; // the last seen report (supports retry options)

	private boolean reporting; // true if already reporting errors to the user (to prevent multiple messages)

	private final Collection<Request> requests=new HashSet<>(); // pending requests


	public SPARQLPort() {
		root("<script/>")

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				}) // !!! migrate to Self.Bus

				.<Bus>as()

				.activity(new Action<Boolean>() {
					@Override public void execute(final Boolean activity) { activity(activity); }
				})

				.query(new Action<Query>() {
					@Override public void execute(final Query query) { query(query); }
				});
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void report(final Report report) {
		this.report=report; // memo the last seen report
	}

	private void activity(final Boolean activity) {
		if ( activity == null ) { cancel(); } // cancel pending requests
	}


	private void query(final Query query) {

		final String server=query.getServer();
		final String source=query.getSource();

		final int timeout=query.getTimeout();

		final RequestBuilder builder=new RequestBuilder(RequestBuilder.POST, server);

		builder.setTimeoutMillis(timeout > 0 ? timeout : Timeout);

		// application/x-www-form-urlencoded is compatible with simple CORS requests
		// ;(sesame) explicitly state charset=UTF-8 to prevent encoding issues (https://openrdf.atlassian.net/browse/SES-2301)

		builder.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); //
		builder.setHeader("Accept", "application/sparql-results+json"); // !!! add tsv/csv/xml with quality

		// setting custom headers (X-*) would turn on pre-flight CORS request with unknown impact on authorization
		// (see https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Simple_requests)

		// Authentication requires full CORS support: wildcard 'Access-Control-Allow-Origin' headers won't work…

		builder.setIncludeCredentials(!server.contains("dbpedia.org/")); // ;( ignore wildcard origin for demos…

		root().info(SPARQLPort.class, "retrieving data from endpoint").report(source);

		final long rstart=System.currentTimeMillis();

		try {

			final String args="query="+URL.encodeQueryString(source);

			insert(builder.sendRequest(args, new RequestCallback() {

				@Override public void onResponseReceived(final Request request, final Response response) {

					final long rstop=System.currentTimeMillis();

					remove(request);

					final int status=response.getStatusCode();
					final String text=response.getStatusText();
					final String body=response.getText();

					switch ( status ) {

						case Response.SC_OK: // !!! switch parser according to response type

							try {

								final long pstart=System.currentTimeMillis();

								final Table table=JSONTable.table(body);

								final long pstop=System.currentTimeMillis();

								root().info(SPARQLPort.class, "retrieved "+table.rows()+" records in "+(rstop-rstart)+" ms");
								root().info(SPARQLPort.class, "parsed "+table.rows()+" records in "+(pstop-pstart)+" ms");

								query.setResults(table).done();

							} catch ( final RuntimeException error ) { // parsing error

								query.error(syntax(error("malformed SPARQL results", error)));

							}

							break;

						case 0: // unknown/not-cors host

							query.error(generic(error("unknown error / not cors-enabled")));

							break;

						case Response.SC_NOT_FOUND:

							query.error(unknown(error(status, text, body)));

							break;

						case Response.SC_BAD_GATEWAY:

							query.error(gateway(error(status, text, body), status, text));

							break;

						case Response.SC_UNAUTHORIZED:

							query.error(unauthorized(error(status, text, body)));

							break;

						case Response.SC_FORBIDDEN:

							query.error(forbidden(error(status, text, body)));

							break;

						default:

							switch ( status/100 ) {

								case 4: // client protocol error

									query.error(client(error(status, text, body), status, text));

									break;

								case 5: // server internal error

									query.error(server(error(status, text, body), status, text));

									break;

								default: // unexpected response code

									query.error(unexpected(error(status, text, body), status, text));

									break;
							}

							break;

					}

				}

				@Override public void onError(final Request request, final Throwable error) { // !!! gracefully (error types?)

					remove(request);

					if ( error instanceof RequestTimeoutException ) {

						query.error(timeout((RequestTimeoutException)error));

					} else {

						query.error(connection(error instanceof Exception ? (Exception)error : new Exception(error)));

					}

				}

			}));

		} catch ( final RequestTimeoutException error ) {

			query.error(timeout(error));

		} catch ( final RequestException error ) { // !!! gracefully (error types?)

			query.error(connection(error));

		}
	}


	//// Requests //////////////////////////////////////////////////////////////////////////////////////////////////////

	private void insert(final Request request) {
		if ( requests.add(request) && requests.size() == 1 ) {
			root().<Bus>as().activity(true);
		}
	}

	private void remove(final Request request) {
		if ( requests.remove(request) && requests.isEmpty() ) {
			root().<Bus>as().activity(false);
		}
	}

	private void cancel() {

		for (final Request request : requests) {
			try {
				request.cancel();
			} catch ( final RuntimeException e ) {
				root().error(SPARQLPort.class, "unable to cancel pending http request", e);
			}
		}

		requests.clear();
	}


	//// Error Logging ///////////////////////////////////////////////////////////////////////////////////////////////

	private Exception error(final String message) {

		root().error(SPARQLPort.class, "failed SPARQL request ["+message+"]");

		return new RequestException(message);
	}

	private Exception error(final String message, final RuntimeException e) {

		root().error(SPARQLPort.class, "failed SPARQL request ["+message+"]").log(e);

		return new RequestException(message, e);
	}

	private Exception error(final int code, final String status, final String text) {

		final String message="HTTP error "+code+" / "+status;

		root().error(SPARQLPort.class, "failed SPARQL request ["+message+"]").report(text);

		return new RequestException(message);
	}


	//// Error Reporting ///////////////////////////////////////////////////////////////////////////////////////////////

	private Exception syntax(final Exception error) {
		return dismiss(error, "Malformed Endpoint Response",
				"Make sure the endpoint URL\nis correct and refers to a SPARQL 1.1 server.");
	}

	private Exception generic(final Exception error) {
		return dismiss(error, "Network Error",
				"Make sure the endpoint\nURL is correct and the server is active."
				//+ " and configured to support\ncross-origin (CORS) requests."
		);
	}

	private Exception unknown(final Exception error) {
		return dismiss(error, "Unknown Endpoint",
				"Make sure the endpoint URL\nis correct and refers to a SPARQL 1.1 server.");
	}

	private Exception gateway(final Exception error, final int code, final String status) {
		return dismiss(error, "Endpoint Error",
				"The SPARQL endpoint\nwasn't able to process\nthe query and says:\n"+clip(status, 40)+".");
	}

	private Exception unauthorized(final Exception error) {
		return dismiss(error, "Authorization Required",
				"Make sure you supplied correct credentials.");
	}

	private Exception forbidden(final Exception error) {
		return dismiss(error, "Access Forbidden",
				"You are not authorized\nto access this endpoint.");
	}

	private Exception client(final Exception error, final int code, final String status) {
		return dismiss(error, "SPARQL Protocol Error",
				"The SPARQL endpoint\ndidn't accept the\nrequest and says:\n"+code+" / "+clip(status, 20)+".");
	}

	private Exception server(final Exception error, final int code, final String status) {
		return dismiss(error, "Proxy Error",
				"The SPARQL proxy\nwasn't able to process\nthe query and says:\n"+code+" / "+clip(status, 20)+".");
	}

	private Exception unexpected(final Exception error, final int code, final String status) {
		return dismiss(error, "Unexpected Endpoint Response",
				"The SPARQL endpoint\nreturned an unexpected response and says:\n"+code+" / "+clip(status, 20)+".");
	}

	private Exception timeout(final RequestTimeoutException error) {
		return retry(error, "Endpoint Connection Timed-Out",
				"The endpoint didn't respond within the allotted time:\nyour query may be too complex or the network connection may be slow.");
	}

	private <T extends Exception> T connection(final T error) {
		return dismiss(error, "Endpoint Connection Failed",
				"Make sure the endpoint URL\nis correct and refers to a SPARQL 1.1 server.");
	}


	private <T extends Exception> T dismiss(final T error, final String message, final String details) {
		return report(error, new Dialog()  // !!! automatic reporting / option to report via email

				.message(message)
				.details(details)

				.action("OK", new Action<Event>() {
					@Override public void execute(final Event e) {}
				}));
	}

	private Exception retry(final Exception error, final String message, final String details) {
		return report(error, new Dialog()

				.message(message)
				.details(details)

				.action("Cancel", new Action<Event>() {
					@Override public void execute(final Event e) {}
				})

				.action("Retry", new Action<Event>() {
					@Override public void execute(final Event e) { root().async(report); }
				}));
	}


	private <T extends Exception> T report(final T error, final Dialog dialog) {

		if ( !reporting ) {
			try {

				dialog.done(new Action<Event>() {
					@Override public void execute(final Event event) { reporting=false; }
				}).open();

			} finally { reporting=true;}
		}

		return error;
	}

}
