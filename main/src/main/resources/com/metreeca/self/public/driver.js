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

(function (window) {

	if ( !window.metreeca ) { // guard against multiple inclusions

		var metreeca="metreeca";
		var selector="iframe."+metreeca;


		//// Cross-Frame Messages //////////////////////////////////////////////////////////////////////////////////////

		var identity=metreeca+":identity:";
		var options=metreeca+":options:";
		var escape=metreeca+":escape";


		//// Utilities ///////////////////////////////////////////////////////////////////// ;(ff) before event com.metreeca.next.handlers

		function each(items, handler) { // call a handler on each item in an array-like object

			for (var i=0; i < items.length; ++i) { handler(items[i]); }

		}

		function opts(element, base) { // convert data-* attributes on element to an option object

			var opts={};

			var handle=hash(base); // shared document handle
			var attributes=element.attributes;

			if ( handle ) {
				opts[""]=handle;
			}

			for (var a=0; a < attributes.length; ++a) {

				var attribute=attributes[a];

				if ( attribute.name.indexOf("data-") === 0 ) {
					opts[attribute.name.substr(5).toLowerCase()]=attribute.value;
				}
			}

			return opts;
		}

		function form(opts) { // convert an option object to a urlencoded form

			var form="";

			for (var p in opts) {
				if ( opts.hasOwnProperty(p) ) {

					form+=form ? "&" : "";

					if ( p ) {
						form+=encodeURIComponent(p);
						form+="=";
					}

					form+=encodeURIComponent(opts[p]);
				}
			}

			return form;
		}

		function merge(target, source) { // merge option objects

			for (var p in source) {
				if ( source.hasOwnProperty(p) && !target.hasOwnProperty(p) ) { target[p]=source[p]; }
			}

			return target;
		}

		function hash(url) { // extract hash from url

			return url.match(/(?:#(.*))?$/)[1] || "";

		}

		function load(url, handler) { // retrieve the content of a remote url and process it with handler

			var xhr=new XMLHttpRequest();

			xhr.addEventListener("loadend", function (e) {
				if ( (e.target.status || 0)/100 == 2 ) {
					handler(e.target.responseText || "")
				} else if ( e.target.status ) {
					throw new Error("unable to load configuration from '"+url+"'"
							+" ["+e.target.status+"/"+e.target.statusText+"]");
				}
			});

			xhr.open("GET", url);
			xhr.send();

		}


		//// Embedded Tool Setup ///////////////////////////////////////////////////////////////////////////////////////

		window.addEventListener("message", function (e) {

			if ( typeof e.data === "string" && e.data.indexOf(identity) === 0 ) { // embedded tool signalling readiness

				// cross-origin messages can't be associated to a specific iframe comparing contentWindow and event.source
				// > resend options for all iframes prefixing with the unique activation message generated by the tool

				each(window.document.querySelectorAll("iframe"), function (frame) {
					if ( frame.src.indexOf(e.origin) === 0 ) { // a frame embedding tool

						expand(configure(frame), function (opts) {
							frame.contentWindow.postMessage(e.data+form(opts), "*");
						});

					}
				});
			}

		});

		function configure(frame) {

			if ( !frame.metreeca$opts ) { // to be initialized

				frame.classList.add(metreeca);
				frame.metreeca$opts=opts(frame, frame.src);

				frame.style.border="none"; // disable default iframe border

				if ( frame.name ) {
					each(window.document.querySelectorAll("a[target="+frame.name+"]"), function (link) {

						link.metreeca$opts=opts(link, link.href);

						if ( (link.dataset.default || "").toLowerCase() === "true" ) {
							frame.metreeca$opts=merge(frame.metreeca$opts || {}, link.metreeca$opts);
						}

						link.addEventListener("click", function (e) {
							try {

								expand(e.target.metreeca$opts, function (opts) {
									frame.contentWindow.postMessage(options+form(opts), "*");
								});

							} finally {
								e.preventDefault();
							}
						});

					});
				}
			}

			return frame.metreeca$opts;
		}

		function expand(opts, handler) {
			if ( opts[""] || !opts.document ) {

				handler(opts);

			} else { // load remote content

				load(opts.document, function (content) {

					opts[""]=content;

					handler(opts);

				});

			}
		}


		//// Host Page Events //////////////////////////////////////////////////////////////////////////////////////////

		each(["mousedown", "keydown"], function (event) { // escape overlays on user interaction outside embedding iframe
			window.addEventListener(event, function () {

				each(window.document.querySelectorAll(selector), function (frame) {
					frame.contentWindow.postMessage(escape, "*");
				});

			}, true);
		});


		//// Public API ////////////////////////////////////////////////////////////////////////////////////////////////

		window.metreeca=function (target, opts) {

			var frame=window.document.getElementsByName(target)[0];

			if ( frame && frame.contentWindow ) { // target found and actually an iframe

				if ( typeof opts === "string" ) { // short/document url

					window.metreeca(target, { document: opts });

				} else { // configuration options

					// update attributes to the benefit of uninitialized frames

					for (var p in opts) {
						if ( opts.hasOwnProperty(p) ) {
							frame.setAttribute("data-"+p, opts[p]);
						}
					}

					// send message to the benefit of initialized frames

					expand(opts, function (opts) {
						frame.contentWindow.postMessage(options+form(opts), "*");
					});

				}
			}

		};

	}

})(this);