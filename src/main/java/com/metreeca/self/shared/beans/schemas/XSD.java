/*
 * Copyright © 2013-2021 Metreeca srl. All rights reserved.
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

package com.metreeca.self.shared.beans.schemas;

import com.metreeca.self.shared.beans.Term;


public final class XSD {

	public static final String XSD="http://www.w3.org/2001/XMLSchema#";

	public static final String XSDBoolean=XSD+"boolean";

	public static final String XSDByte=XSD+"byte";
	public static final String XSDShort=XSD+"short";
	public static final String XSDInt=XSD+"int";
	public static final String XSDLong=XSD+"long";
	public static final String XSDFloat=XSD+"float";
	public static final String XSDDouble=XSD+"double";
	public static final String XSDInteger=XSD+"integer";
	public static final String XSDDecimal=XSD+"decimal";

	public static final String XSDDateTime=XSD+"dateTime";
	public static final String XSDDate=XSD+"date";
	public static final String XSDTime=XSD+"time";
	public static final String XSDGYear=XSD+"gYear";
	public static final String XSDGMonth=XSD+"gMonth";
	public static final String XSDGDay=XSD+"gDay";

	public static final String XSDAnyURI=XSD+"anyURI";
	public static final String XSDString=XSD+"string";

	public static final Term XSDFalse=Term.typed("false", XSDBoolean);
	public static final Term XSDTrue=Term.typed("true", XSDBoolean);

	private XSD() {}

}
