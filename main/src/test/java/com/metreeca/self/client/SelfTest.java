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

package com.metreeca.self.client;

import com.metreeca._bean.server.ServerMeta;
import com.metreeca._bean.server.ServerNode;
import com.metreeca._bean.shared.NodeCodec;
import com.metreeca._tool.shared.Item;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SelfTest.Runner.class)
public final class SelfTest {

	private static final String source="src/test/cases";
	private static final String target="target/test-cases/";

	private final NodeCodec codec=new NodeCodec(ServerMeta.factory(), ServerNode.factory());


	private List<String> tests() {

		final List<String> tests=new ArrayList<>();

		for (final File version : versions(new File(source))) {
			for (final File model : items(version)) {
				tests.add(model.getAbsolutePath());
			}
		}

		return tests;
	}

	private void run(final String test) throws IOException {

		final File item=new File(test);
		final File output=new File(item.getParent().replace(source, target));

		output.mkdirs();

		final String txt=codec.encode(codec.decode(read(new FileReader(item))));

		write(new FileWriter(new File(output, item.getName())), txt);
	}


	private File[] versions(final File archive) {

		final File[] versions=archive.listFiles(file -> {
			try {
				return Item.Build(file.getName()) > 0;
			} catch ( final IllegalArgumentException ignored ) {
				return false;
			}
		});

		Arrays.sort(versions, (x, y) -> {

			final int i=Item.Build(x.getName());
			final int j=Item.Build(y.getName());

			return i < j ? -1 : i > j ? 1 : 0;
		});

		return versions;
	}

	private File[] items(final File version) {
		return version.listFiles(file -> file.getName().endsWith(".xml"));
	}


	private String read(final Reader reader) throws IOException {

		final StringBuilder text=new StringBuilder();
		final char[] buffer=new char[1024];

		for (int n; (n=reader.read(buffer)) > 0; ) {
			text.append(buffer, 0, n);
		}

		return text.toString();
	}

	private void write(final FileWriter writer, final String text) throws IOException {
		writer.write(text);
		writer.close();
	}


	public static final class Runner extends ParentRunner<String> {

		private static final int NameWidth=30;
		private static final Pattern CasePattern=Pattern.compile("(?:/[^/]*)*/(.*)/(.*)\\.xml$");

		private final SelfTest suite;


		public Runner(final Class<SelfTest> type) throws InitializationError {

			super(type);

			try {

				this.suite=type.newInstance();

			} catch ( final InstantiationException e ) {
				throw new InitializationError(e);
			} catch ( final IllegalAccessException e ) {
				throw new InitializationError(e);
			}
		}


		@Override protected List<String> getChildren() {
			return suite.tests();
		}

		@Override protected Description describeChild(final String child) {

			final Matcher matcher=CasePattern.matcher(child);

			if ( !matcher.matches() ) {
				throw new IllegalArgumentException("malformed case path ["+child+"]");
			}

			final String version=matcher.group(1);
			final String name=matcher.group(2);

			return Description.createTestDescription(getTestClass().getJavaClass(),
					version+" / "+(name.length() < NameWidth ? name : name.substring(0, NameWidth)+"…"));
		}

		@Override protected void runChild(final String child, final RunNotifier notifier) {

			final Description description=describeChild(child);

			try {

				notifier.fireTestStarted(description);

				if ( false ) { // !!!
					notifier.fireTestIgnored(description);
				} else {
					suite.run(child);
				}

			} catch ( final Exception t ) {
				notifier.fireTestFailure(new Failure(description, t));
			} finally {
				notifier.fireTestFinished(description);
			}
		}
	}

}
