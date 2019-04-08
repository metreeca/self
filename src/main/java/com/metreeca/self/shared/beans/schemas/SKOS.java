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

package com.metreeca.self.shared.beans.schemas;

import com.metreeca.self.shared.beans.Term;

import static com.metreeca.self.shared.beans.Term.named;


public final class SKOS {

	public static final String SKOS="http://www.w3.org/2004/02/skos/core#";


	public static final Term SKOSConcept=named(SKOS, "Concept");
	public static final Term SKOSConceptScheme=named(SKOS, "ConceptScheme");

	public static final Term SKOSInScheme=named(SKOS, "inScheme");
	public static final Term SKOSHasTopConcept=named(SKOS, "hasTopConcept");
	public static final Term SKOSTopConceptOf=named(SKOS, "topConceptOf");

	public static final Term SKOSAltLabel=named(SKOS, "altLabel").setLabel("alternative label");
	public static final Term SKOSHiddenLabel=named(SKOS, "hiddenLabel");
	public static final Term SKOSPrefLabel=named(SKOS, "prefLabel").setLabel("preferred label");

	public static final Term SKOSNotation=named(SKOS, "notation");

	public static final Term SKOSNote=named(SKOS, "note");
	public static final Term SKOSDefinition=named(SKOS, "definition");
	public static final Term SKOSScopeNote=named(SKOS, "scopeNote");
	public static final Term SKOSHistoryNote=named(SKOS, "historyNote");
	public static final Term SKOSEditorialNote=named(SKOS, "editorialNote");
	public static final Term SKOSChangeNote=named(SKOS, "changeNote");
	public static final Term SKOSExample=named(SKOS, "example");

	public static final Term SKOSSemanticRelation=named(SKOS, "semanticRelation");
	public static final Term SKOSBroader=named(SKOS, "broader");
	public static final Term SKOSBroaderTransitive=named(SKOS, "broaderTransitive");
	public static final Term SKOSNarrower=named(SKOS, "narrower");
	public static final Term SKOSNarrowerTransitive=named(SKOS, "narrowerTransitive");
	public static final Term SKOSRelated=named(SKOS, "related");

	public static final Term SKOSMappingRelation=named(SKOS, "mappingRelation");
	public static final Term SKOSExactMatch=named(SKOS, "exactMatch");
	public static final Term SKOSCloseMatch=named(SKOS, "closeMatch");
	public static final Term SKOSRelatedMatch=named(SKOS, "relatedMatch");
	public static final Term SKOSBroadMatch=named(SKOS, "broadMatch");
	public static final Term SKOSNarrowMatch=named(SKOS, "narrowMatch");

	public static final Term SKOSCollection=named(SKOS, "Collection");
	public static final Term SKOSOrderedCollection=named(SKOS, "OrderedCollection");
	public static final Term SKOSMember=named(SKOS, "member");
	public static final Term SKOSMemberList=named(SKOS, "memberList");


	private SKOS() {}

}
