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

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.beans.Specs;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import java.io.IOException;

import static com.metreeca.self.shared.forms.TuplesTest.assertEquals;


public final class TuplesTestStarting extends ShapeTest {

	//// RDFS/OWL Classes //////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testClasses() throws RepositoryException, RDFParseException, IOException {
		try (final Store store=new Store()) {

			assertEquals("<Class>",

					"prefix : <urn:example:> select ?class {} values ?class { :a :b }",

					new Tuples().setSpecs(Specs.Classes()),

					store.open("@prefix : <urn:example:>.\n"
							+"\n"
							+":a a rdfs:Class.\n"
							+":b a owl:Class.\n"
							+"rdfs:Resource a rdfs:Class. # excluded: well-known namespace\n"
							+"\n"
							+":x a :a.\n"
							+":y a :b.\n"
							+":r a rdfs:Resource.\n"));

		}
	}

	//// Introspected Types ////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testTypes() throws RepositoryException, RDFParseException, IOException {
		try (final Store store=new Store()) {

			assertEquals("<Type>",

					"prefix : <urn:example:> select ?type {} values ?type { :A :B }",

					new Tuples().setSpecs(Specs.Types()),

					store.open("@prefix : <urn:example:>.\n"
							+"\n"
							+":w a :A.\n"
							+":x a :A.\n"
							+":y a :B.\n"
							+":r a rdfs:Resource. # excluded: well-known namespace\n"));

		}
	}

	//// Resources /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testResources() throws Exception {
		try (final Store store=new Store()) {

			assertEquals("<Resource>",

					"select distinct ?s { ?s ?p ?o }",

					new Tuples().setSpecs(Specs.Resources()),

					store.open("@prefix : <urn:example:>. :x :p :a, :b. :y :p :a."));

		}
	}

	@Test public void testResourcesFromEmptyStore() throws RepositoryException {
		try (final Store store=new Store()) {

			assertEquals("<Resource>",

					"select distinct ?s { ?s ?p ?o }",

					new Tuples().setSpecs(Specs.Resources()),

					store.open());

		}
	}

}
