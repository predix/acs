package com.ge.predix.acs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JanusGraphTest {
    public void testRemoteGraphWithFluentAPI() throws Exception {
        // http://tinkerpop.apache.org/docs/3.2.3/reference/#connecting-via-remotegraph:
        // To configure a "remote" traversal, there first needs to be a TraversalSource.
        // A TraversalSource can be generated from any Graph instance with the traversal() method.
        // Any traversals generated from this source using the withRemote() configuration option 
        // will not execute against the local graph. That could be confusing and it maybe be easier
        // to think of the local graph as being "empty". 
        // It is recommended that when using withRemote(), the TraversalSource be generated with EmptyGraph
        // Remote graph could be traversed using remote traversal source, but it could not be manipulated.
        Graph graph = EmptyGraph.instance();
        System.out.println("testRemoteGraph_fluentAPI(): graph = " + graph.toString());

        Cluster cluster = null;
        GraphTraversalSource g = null;
        try {
            cluster = Cluster.build().addContactPoint("127.0.0.1").create();
            System.out.println("testRemoteGraphWithFluentAPI(): cluster = " + cluster);

            g = graph.traversal().withRemote(DriverRemoteConnection.using(cluster, "g"));
            System.out.println("testRemoteGraphWithFluentAPI(): traversal source = " + g.toString());
            System.out.println("testRemoteGraphWithFluentAPI(): vertices count = " + g.V().count().next());

            g.V().toList().forEach(v -> System.out.println(v.toString()));
        } finally {
            if (g != null) {
                g.close();
            }
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    @Test
    public void testJanusGraphWithStringQuery() throws Exception {

        Cluster cluster = null;
        Client client = null;

        try {
            cluster = Cluster.build().addContactPoint("127.0.0.1").create();
            System.out.println("testJanusGraphWithStringQuery(): cluster = " + cluster);

            client = cluster.connect();
            System.out.println("testJanusGraphWithStringQuery(): client = " + client.toString());

            // Get count of vertices and print
            CompletableFuture<List<Result>> results = client.submit("g.V().count()").all();
            int currentCount = results.get().get(0).getInt();
            System.out.println("testJanusGraphWithStringQuery(): current count = " + currentCount);

            // Add new vertex
            results = client.submit("g.addV('name', 'HELLO')").all();

            // Get all the names and print
            results = client.submit("g.V().values('name')").all();
            results.get().forEach(r -> System.out.println(r.getString()));

            // Get new count after adding vertex
            results = client.submit("g.V().count()").all();
            int newCount = results.get().get(0).getInt();
            System.out.println("testJanusGraphWithStringQuery(): new count = " + newCount);

            // Confirm newCount is 1 higher
            Assert.assertEquals(currentCount + 1, newCount);
        } finally {
            if (client != null) {
                client.close();
            }
            if (cluster != null) {
                cluster.close();
            }
        }
    }
}
