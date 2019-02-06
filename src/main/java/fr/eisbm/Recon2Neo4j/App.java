/**

 * Licensed to Neo Technology under one or more contributor

 * license agreements. See the NOTICE file distributed with

 * this work for additional information regarding copyright

 * ownership. Neo Technology licenses this file to you under

 * the Apache License, Version 2.0 (the "License"); you may

 * not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 * http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing,

 * software distributed under the License is distributed on an

 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY

 * KIND, either express or implied. See the License for the

 * specific language governing permissions and limitations

 * under the License.

 */

package fr.eisbm.Recon2Neo4j;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class App

{
	// START SNIPPET: vars
	private static final String DB_PATH = "target/neo4j-db-recon2_network";
	private static GraphDatabaseService graphDb;

	private SBMLReading _smblReading = new SBMLReading();

	private long start = 0, end = 0;

	// END SNIPPET: vars

	public static GraphDatabaseService getGraphInstance() {
		return graphDb;
	}

	// END SNIPPET: createReltype

	public static void main(final String[] args) {
		App metabolicFramework = new App();

		metabolicFramework.createDb();

		// diseaseGraph.removeData();

		metabolicFramework.shutDown();
	}

	@SuppressWarnings("deprecation")
	void createDb() {
		deleteFileOrDirectory(new File(DB_PATH));

		// START SNIPPET: startDb

		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

		registerShutdownHook(graphDb);

		// END SNIPPET: startDb

		// START SNIPPET: transaction

		try (Transaction tx = graphDb.beginTx()) {

			// Database operations go here

			// END SNIPPET: transaction

			// START SNIPPET: addData

			try {
				start = System.currentTimeMillis();
				_smblReading.readXMLFile();
				end = System.currentTimeMillis();
				long diff = end - start;
				System.out.println("Time (in seconds) is : " + diff * 0.001);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			tx.success();

		}

		// END SNIPPET: transaction

	}

	void removeData() {
		try (Transaction tx = graphDb.beginTx()) {
			// START SNIPPET: removingData

			// let's remove the data

			// END SNIPPET: removingData

			tx.success();
		}
	}

	void shutDown() {
		System.out.println();
		System.out.println("Shutting down database ...");

		// START SNIPPET: shutdownServer

		graphDb.shutdown();
		// END SNIPPET: shutdownServer
	}

	// START SNIPPET: shutdownHook

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {

		// Registers a shutdown hook for the Neo4j instance so that it shuts
		// down nicely when the VM exits (even if you "Ctrl-C" the

		// running application).

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});

	}

	// END SNIPPET: shutdownHook

	private static void deleteFileOrDirectory(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					deleteFileOrDirectory(child);
				}
			}
			file.delete();
		}
	}
}
