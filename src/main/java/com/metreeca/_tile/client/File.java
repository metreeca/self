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

package com.metreeca._tile.client;

import com.google.gwt.core.client.JavaScriptObject;


public final class File extends JavaScriptObject {

	@SuppressWarnings("ProtectedMemberInFinalClass") protected File() {}


	public native String name() /*-{
		return this.name;
	}-*/;

	public native String type() /*-{
		return this.type;
	}-*/;

	public native int size() /*-{ // ;( long not supported by jsni
		return this.size;
	}-*/;


	public File read(final Read handler) {
		return read("UTF-8", handler);
	}

	public native File read(final String encoding, final Read handler) /*-{

		var file=this;
		var reader=new FileReader();

		reader.onprogress=$entry(function (e) {
			handler.@com.metreeca._tile.client.File.Read::reading(Lcom/metreeca/_tile/client/File;II)(
					file, e.loaded, e.total);
		});

		reader.onabort=$entry(function () {
			handler.@com.metreeca._tile.client.File.Read::aborted(Lcom/metreeca/_tile/client/File;)(
					file);
		});

		reader.onerror=$entry(function () {

			function name(code) {

				for (var name in $wnd.FileError) {
					if ( $wnd.FileError.hasOwnProperty(name) && $wnd.FileError[name] === code ) { return name; }
				}

				return "UNKNOWN_ERR";
			}

			handler.@com.metreeca._tile.client.File.Read::failed(Lcom/metreeca/_tile/client/File;Ljava/lang/String;)(
					file, reader.error.name || name(reader.error.code)); // ;(wk) no name on FileError
		});

		reader.onload=$entry(function () {
			handler.@com.metreeca._tile.client.File.Read::read(Lcom/metreeca/_tile/client/File;Ljava/lang/String;)(
					file, reader.result);
		});

		reader.onloadend=$entry(function () {
			handler.@com.metreeca._tile.client.File.Read::handled(Lcom/metreeca/_tile/client/File;)(
					file);
		});

		reader.readAsText(file, encoding);

		return this;
	}-*/;

	public native File load(final String url, final Load handler) /*-{

		var xhr=new XMLHttpRequest();
		xhr.open('POST', url, true);

		xhr.setRequestHeader("Content-Location", this.name);
		xhr.setRequestHeader("Content-Type", this.type);
		xhr.setRequestHeader("Content-Length", this.size);

		xhr.upload.onprogress=$entry(function (e) {
			handler.@com.metreeca._tile.client.File.Load::sending(Lcom/metreeca/_tile/client/File;II)(
					this, e.lengthComputable ? e.loaded : 0, e.lengthComputable ? e.total : 0
			);
		});

		xhr.onabort=$entry(function () {
			handler.@com.metreeca._tile.client.File.Load::aborted(Lcom/metreeca/_tile/client/File;)(this);
		});

		xhr.onerror=$entry(function () {
			handler.@com.metreeca._tile.client.File.Load::aborted(Lcom/metreeca/_tile/client/File;ILjava/lang/String;)(
					this, xhr.status, xhr.statusText
			);
		});

		xhr.onload=$entry(function () {
			if ( xhr.status-xhr.status%100 == 200 ) {
				handler.@com.metreeca._tile.client.File.Load::synched(Lcom/metreeca/_tile/client/File;Ljava/lang/String;)(
						this, xhr.responseText
				);
			} else {
				handler.@com.metreeca._tile.client.File.Load::aborted(Lcom/metreeca/_tile/client/File;ILjava/lang/String;)(
						this, xhr.status, xhr.statusText
				);
			}
		});

		xhr.ontimeout=$entry(function () {
			handler.@com.metreeca._tile.client.File.Load::expired(Lcom/metreeca/_tile/client/File;)(this);
		});

		xhr.onloadend=$entry(function () {
			handler.@com.metreeca._tile.client.File.Load::handled(Lcom/metreeca/_tile/client/File;)(this);
		});

		xhr.send(this);

		return this;

	}-*/;


	public abstract static class Read {

		public void reading(final File file, final int partial, final int total) {}

		public void aborted(final File file) {}

		public void failed(final File file, final String status) {}

		public void read(final File file, final String text) {}

		public void handled(final File file) {}

	}

	public abstract static class Load {

		private final File file;


		protected Load(final File file) {

			if ( file == null ) {
				throw new NullPointerException("null file");
			}

			this.file=file;
		}


		public final File file() {
			return file;
		}


		public void sending(final File file, final int partial, final int total) {}

		public void synched(final File file, final String response) {}

		public void aborted(final File file) {}

		public void aborted(final File file, final int code, final String status) {}

		public void expired(final File file) {}

		public void handled(final File file) {}
	}

}
