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

package com.metreeca._tile.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

import java.util.HashMap;
import java.util.Map;


public abstract class Plugin extends JavaScriptObject {

	protected Plugin() {}


	public final native <T extends Plugin> T as() /*-{
		return this;
	}-*/;


	//// Text Handling /////////////////////////////////////////////////////////////////////////////////////////////////

	public static native int integer(final String number) /*-{
		return parseInt(number);
	}-*/;

	public static native float real(final String number) /*-{
		return parseFloat(number);
	}-*/;

	public static native String normalize(final String text) /*-{
		return (text || "").replace(/\s+/g, function (match, offset, string) {
			return offset === 0 || offset+match.length === string.length ? "" : /[\r\n]/.test(match) ? "\n" : " ";
		});
	}-*/;

	public static String clip(final String text, final int limit) {
		return text == null ? null : limit <= 0 || text.length() <= limit ? text : text.substring(0, limit)+"…";
	}


	// http://en.wikipedia.org/wiki/Lempel–Ziv–Welch
	// http://www.onicos.com/staff/iz/amuse/javascript/expert/utf.txt
	// http://rosettacode.org/wiki/LZW_compression#JavaScript

	public static native String deflate(final String string) /*-{

		function utf8(utf16) {

			var utf8="";

			for (var i=0; i < utf16.length; i++) {

				var code=utf16.charCodeAt(i);

				if ( (code >= 0x0001) && (code <= 0x007F) ) {

					utf8+=String.fromCharCode(code);

				} else if ( code <= 0x07FF ) {

					utf8+=String.fromCharCode(0xC0|((code>>6)&0x1F));
					utf8+=String.fromCharCode(0x80|((code>>0)&0x3F));

				} else {

					utf8+=String.fromCharCode(0xE0|((code>>12)&0x0F));
					utf8+=String.fromCharCode(0x80|((code>>6)&0x3F));
					utf8+=String.fromCharCode(0x80|((code>>0)&0x3F));

				}
			}

			return utf8;
		}

		var dict={};
		var size=256;

		for (var i=0; i < size; ++i) {
			dict[String.fromCharCode(i)]=i;
		}

		var text=utf8(string || "");
		var data=[];
		var word="";

		for (var i=0; i < text.length; ++i) {

			var code=text.charAt(i);
			var next=word+code;

			if ( dict.hasOwnProperty(next) ) {

				word=next;

			} else {

				data.push(dict[word]);

				if ( size < 0xFFFF ) { dict[next]=size++; }

				word=code;

			}
		}

		if ( word !== "" ) {
			data.push(dict[word]);
		}

		return $wnd.btoa(utf8(String.fromCharCode.apply(null, data)));

	}-*/;

	public static native String inflate(final String string) /*-{

		function utf16(utf8) {

			var utf16="";

			for (var i=0; i < utf8.length; ++i) {

				var code=utf8.charCodeAt(i);

				switch ( code>>4 ) {

					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:

						utf16+=utf8.charAt(i); // 0xxxxxxx

						break;

					case 12:
					case 13:

						utf16+=String.fromCharCode((code&0x1F)<<6 // 110x xxxx
								|utf8.charCodeAt(++i)&0x3F); // 10xx xxxx

						break;

					case 14:

						utf16+=String.fromCharCode((code&0x0F)<<12 // 1110 xxxx
								|(utf8.charCodeAt(++i)&0x3F)<<6 // 10xx xxxx
								|(utf8.charCodeAt(++i)&0x3F)<<0); // 10xx xxxx

						break;
				}
			}

			return utf16;
		}

		var dict=[];
		var size=256;

		for (var i=0; i < size; ++i) {
			dict[i]=String.fromCharCode(i);
		}

		var data=utf16($wnd.atob(string || ""));
		var text="";
		var last="";

		for (var i=0; i < data.length; ++i) {

			var code=data.charCodeAt(i);

			if ( i > 0 && size < 0xFFFF ) { dict[size++]=last+(dict[code] || last).charAt(0); }

			last=dict[code] || "";
			text+=last;
		}

		return text;

	}-*/;


	public static String prefix(final String text) { // insert engine-specific prefixes

		final Map<String, String[]> rules=new HashMap<>();

		rules.put("box-sizing", new String[] {"-moz-"});  /* ;(ff < 29) */

		rules.put("transform*:", new String[] {"-webkit-"});  /* ;(safari) */
		rules.put("animation*:", new String[] {"-webkit-"});  /* ;(safari/chrome) */

		rules.put("display:flex", new String[] {"-webkit-"});
		rules.put("display:inline-flex", new String[] {"-webkit-"});
		rules.put("flex*:", new String[] {"-webkit-"});
		rules.put("align-*:", new String[] {"-webkit-"});
		rules.put("justify-content:", new String[] {"-webkit-"});
		rules.put("order:", new String[] {"-webkit-"});

		rules.put("user-select:", new String[] {"-webkit-", "-moz-", "-ms-"});

		String css=text.replaceAll("(\\s|/\\*.*?\\*/)+", " "); // collapse spaces and remove comments

		for (final Map.Entry<String, String[]> entry : rules.entrySet()) {

			final String pattern=entry.getKey();
			final String[] prefixes=entry.getValue();

			final int colon=pattern.indexOf(':');

			final String property=(colon < 0 ? pattern : pattern

					.substring(0, colon))
					.replace("*", "[-\\w]*");

			final String value=(colon < 0 ? "" : pattern

					.substring(colon+1))
					.replace("*", "[-\\w]*")
					.replaceAll("[.?*\\(){}]", "\\\\$0"); // escape special regex chars

			for (final String prefix : prefixes) {
				css=value.isEmpty()
						? css.replaceAll("\\b("+property+") ?: ?([^;]*);", "$1: $2; "+prefix+"$1: $2;")
						: css.replaceAll("\\b("+property+") ?: ?("+value+"\\b[^;]*);", "$1: $2; $1: "+prefix+"$2;");
			}
		}

		return css;
	}


	//// Error Handling ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Register a global error handler.
	 *
	 * @param handler a global error handler to be chained after existing ones
	 *
	 * @return this Plugin
	 *
	 * @throws IllegalArgumentException if <code>handler</code> is <code>null</code>
	 */
	public final Plugin error(final GWT.UncaughtExceptionHandler handler) {

		if ( handler == null ) {
			throw new NullPointerException("null handler");
		}

		final GWT.UncaughtExceptionHandler current=GWT.getUncaughtExceptionHandler();

		GWT.setUncaughtExceptionHandler(current == null ? handler : new GWT.UncaughtExceptionHandler() {
			@Override public void onUncaughtException(final Throwable throwable) {
				current.onUncaughtException(throwable);
				handler.onUncaughtException(throwable);
			}
		});

		return this;
	}


	//// Logging ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public final Plugin error(final Class<?> clazz, final String message) {
		return error(name(clazz), message);
	}

	public final Plugin error(final Class<?> clazz, final String message, final Throwable throwable) {
		return error(name(clazz), message, throwable);
	}

	public final Plugin error(final String source, final String message) {
		return report("!!! ["+source+"] "+message);
	}

	public final Plugin error(final String source, final String message, final Throwable throwable) {
		return report("!!! ["+source+"] "+message, throwable);
	}


	public final Plugin warning(final Class<?> clazz, final String message) {
		return warning(name(clazz), message);
	}

	public final Plugin warning(final Class<?> clazz, final String message, final Throwable throwable) {
		return warning(name(clazz), message, throwable);
	}

	public final Plugin warning(final String source, final String message) {
		return report(" !! ["+source+"] "+message);
	}

	public final Plugin warning(final String source, final String message, final Throwable throwable) {
		return report("!! ["+source+"] "+message, throwable);
	}


	public final Plugin info(final Class<?> clazz, final String message) {
		return info(name(clazz), message);
	}

	public final Plugin info(final String source, final String message) {
		return report("  ! ["+source+"] "+message);
	}

	public final Plugin info(final Class<?> clazz, final String label, final Runnable task) {
		return info(name(clazz), label, task);
	}

	public final Plugin info(final String source, final String label, final Runnable task) {

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		final long start=System.currentTimeMillis();

		task.run();

		final long stop=System.currentTimeMillis();

		return info(source, label+" in "+(stop-start)+" ms");
	}


	public final Plugin debug(final Class<?> clazz, final String message) {
		return debug(name(clazz), message);
	}

	public final Plugin debug(final String source, final String message) {
		return report("    ["+source+"] "+message);
	}

	public final Plugin debug(final Class<?> clazz, final String label, final Runnable task) {
		return debug(name(clazz), label, task);
	}

	public final Plugin debug(final String source, final String label, final Runnable task) {

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		if ( task == null ) {
			throw new NullPointerException("null task");
		}

		final long start=System.currentTimeMillis();

		task.run();

		final long stop=System.currentTimeMillis();

		return debug(source, label+" in "+(stop-start)+" ms");
	}


	private String name(final Class<?> clazz) {

		final String name=clazz.getName();

		return name.substring(name.lastIndexOf('.')+1);
	}


	public final Plugin report(final String message) {

		if ( message == null ) {
			throw new NullPointerException("null message");
		}

		log(message);

		return this;
	}

	public final Plugin report(final String message, final Throwable throwable) {

		if ( message == null ) {
			throw new NullPointerException("null message");
		}

		if ( throwable == null ) {
			throw new NullPointerException("null throwable");
		}

		log(message+": "+throwable.toString());

		return this;
	}


	public final native Plugin log(final String message) /*-{

		$wnd.console.log(message);

		return this;

	}-*/;

	public final <T> T log(final T object) {

		if ( object instanceof JavaScriptObject ) {
			log((JavaScriptObject)object);
		} else {
			log(String.valueOf(object));
		}

		return object;
	}

	public final native <T extends JavaScriptObject> T log(final T object) /*-{

		$wnd.console.log(object instanceof Error ? object.stack : object);

		return object;

	}-*/;
}
