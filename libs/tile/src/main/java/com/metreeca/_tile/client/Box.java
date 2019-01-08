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

package com.metreeca._tile.client;

import com.google.gwt.core.client.JavaScriptObject;


public final class Box extends JavaScriptObject {

	@SuppressWarnings("ProtectedMemberInFinalClass") protected Box() {}


	public native float x() /*-{ return this.x || this.left || 0; }-*/;

	public native float y() /*-{ return this.y || this.top || 0; }-*/;


	public native float left() /*-{ return this.left || 0; }-*/;

	public native float right() /*-{ return this.right || 0; }-*/;

	public native float top() /*-{ return this.top || 0; }-*/;

	public native float bottom() /*-{ return this.bottom || 0; }-*/;


	public native float width() /*-{ return this.width || 0; }-*/;

	public native float height() /*-{ return this.height || 0; }-*/;

}
