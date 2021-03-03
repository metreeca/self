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

import static com.metreeca.self.shared.beans.Term.named;


public final class VoID {

	public static final String VoID="http://rdfs.org/ns/void#";


	public static final Term VoIDDataset=named(VoID, "Dataset");
	public static final Term VoIDDatasetDescription=named(VoID, "DatasetDescription");
	public static final Term VoIDLinkset=named(VoID, "Linkset");
	public static final Term VoIDTechnicalFeature=named(VoID, "TechnicalFeature");

	public static final Term VoIDClass=named(VoID, "class");
	public static final Term VoIDClassPartition=named(VoID, "classPartition");
	public static final Term VoIDClasses=named(VoID, "classes");
	public static final Term VoIDDataDump=named(VoID, "dataDump");
	public static final Term VoIDDistinctObjects=named(VoID, "distinctObjects");
	public static final Term VoIDDistinctSubjects=named(VoID, "distinctSubjects");
	public static final Term VoIDDocuments=named(VoID, "documents");
	public static final Term VoIDEntities=named(VoID, "entities");
	public static final Term VoIDExampleResource=named(VoID, "exampleResource");
	public static final Term VoIDFeature=named(VoID, "feature");
	public static final Term VoIDInDataset=named(VoID, "inDataset");
	public static final Term VoIDLinkPredicate=named(VoID, "linkPredicate");
	public static final Term VoIDObjectsTarget=named(VoID, "objectsTarget");
	public static final Term VoIDOpenSearchDescription=named(VoID, "openSearchDescription");
	public static final Term VoIDProperties=named(VoID, "properties");
	public static final Term VoIDProperty=named(VoID, "property");
	public static final Term VoIDPropertyPartition=named(VoID, "propertyPartition");
	public static final Term VoIDRootResource=named(VoID, "rootResource");
	public static final Term VoIDSparqlEndpoint=named(VoID, "sparqlEndpoint");
	public static final Term VoIDSubjectsTarget=named(VoID, "subjectsTarget");
	public static final Term VoIDSubset=named(VoID, "subset");
	public static final Term VoIDTarget=named(VoID, "target");
	public static final Term VoIDTriples=named(VoID, "triples");
	public static final Term VoIDUriLookupEndpoint=named(VoID, "uriLookupEndpoint");
	public static final Term VoIDUriRegexPattern=named(VoID, "uriRegexPattern");
	public static final Term VoIDUriSpace=named(VoID, "uriSpace");
	public static final Term VoIDVocabulary=named(VoID, "vocabulary");

	private VoID() {}

}
