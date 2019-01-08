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

package com.metreeca.self.shared.beans.schemas;

import com.metreeca.self.shared.beans.Term;


public final class Geo {

	public static final String Geo="http://www.w3.org/2003/01/geo/wgs84_pos#";

	public static final Term GeoLat=Term.named(Geo, "lat");
	public static final Term GeoLong=Term.named(Geo, "long");
	public static final Term GeoAlt=Term.named(Geo, "alt");
	public static final Term GeoLatLong=Term.named(Geo, "lat_long");
	public static final Term GeoLocation=Term.named(Geo, "location");
	public static final Term GeoTime=Term.named(Geo, "time");
	public static final Term GeoGeometry=Term.named(Geo, "geometry"); // as seen on DBpedia… (not part of the specs)


	private Geo() {}

}
