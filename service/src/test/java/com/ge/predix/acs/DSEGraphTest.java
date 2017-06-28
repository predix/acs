package com.ge.predix.acs;

import static com.ge.predix.acs.privilege.management.dao.GraphGenericRepository.ZONE_ID_KEY;
import static com.ge.predix.acs.privilege.management.dao.GraphResourceRepository.RESOURCE_ID_KEY;
import static com.ge.predix.acs.privilege.management.dao.GraphResourceRepository.RESOURCE_LABEL;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DSEGraphTest {
    static final String SYSTEM_CREATE_GRAPH = "system.graph('%s').ifNotExists().create()";
    static final String SYSTEM_DROP_GRAPH = "system.graph('%s').drop()";

    static final String SCHEMA_CREATE_PROPERTY_KEY = "schema.propertyKey('%s').Text().ifNotExists().create()";
    static final String SCHEMA_CREATE_CUSTOM_VERTEX_ID = "schema.vertexLabel('%s').partitionKey('%s')"
            + ".clusteringKey('%s').ifNotExists().create()";
    static final String SCHEMA_DESCRIBE = "schema.describe()";
    static final String SCHEMA_CLEAR = "schema.clear()";

    static final String QUERY_VERTEX_COUNT = "g.V().count()";
    static final String QUERY_ADD_VERTEX = "g.addV('%s').property('%s','%s').property('%s','%s')";
    static final String QUERY_VERTEX = "g.V(['~label':'%s','%s':'%s','%s':'%s'])";

    @Test
    public void testRemoteGraphWithStringQuery() throws Exception {
        final String graphName = "jgraphA";
        final String zoneIdKey = ZONE_ID_KEY;
        final String resourceIdKey = RESOURCE_ID_KEY;
        final String resourceLabel = RESOURCE_LABEL;

        Cluster cluster = null;
        Client client = null;
        try {
            cluster = Cluster.build().addContactPoint("127.0.0.1").create();
            System.out.println("testRemoteGraphWithStringQuery(): cluster = " + cluster);

            client = cluster.connect();
            System.out.println("testRemoteGraphWithStringQuery(): client = " + client.toString());

            // Create an empty DSE graph
            // Cassandra keyspace name is equal to graph name
            List<Result> results = client.submit(String.format(SYSTEM_CREATE_GRAPH, graphName)).all().get();
            System.out.println("testRemoteGraphWithStringQuery(): create graph results = " + results.toString());

            Client aliasClient = null;
            try {
                aliasClient = client.alias(graphName + ".g");
                results = aliasClient.submit("g").all().get();
                System.out.println("testRemoteGraphWithStringQuery(): traversal source = " + results.toString());
                Assert.assertEquals(results.get(0).getString(),
                        "graphtraversalsource[dsegraphimpl[" + graphName + "], standard]");

                // Create graph schema
                // Schema should be created after alias is set
                createCustomVertexIdSchema(aliasClient, zoneIdKey, resourceIdKey, resourceLabel);

                results = aliasClient.submit(QUERY_VERTEX_COUNT).all().get();
                System.out.println("testRemoteGraphWithStringQuery(): vertices count = " + results.toString());
                Assert.assertEquals(0, results.get(0).getLong());

                // Add vertices
                addVertex(aliasClient, resourceLabel, zoneIdKey, "myzone", resourceIdKey, "resourceOne");
                addVertex(aliasClient, resourceLabel, zoneIdKey, "myzone", resourceIdKey, "resourceTwo");
                addVertex(aliasClient, resourceLabel, zoneIdKey, "otherzone", resourceIdKey, "resourceOne");
                addVertex(aliasClient, resourceLabel, zoneIdKey, "otherzone", resourceIdKey, "resourceTwo");
                addVertex(aliasClient, resourceLabel, zoneIdKey, "otherzone", resourceIdKey, "resourceThree");

                results = aliasClient.submit(QUERY_VERTEX_COUNT).all().get();
                System.out.println("testRemoteGraphWithStringQuery(): vertices count = " + results.toString());
                Assert.assertEquals(5, results.get(0).getLong());

                getVertexByCustomId(aliasClient, resourceLabel, zoneIdKey, "otherzone", resourceIdKey, "resourceOne");
            } finally {
                // System commands cannot be executed while alias is set.
                // There is no explicit way to clear aliases but closing the
                // client
                if (aliasClient != null) {
                    results = aliasClient.submit(SCHEMA_CLEAR).all().get();
                    System.out.println(
                            "testRemoteGraphWithStringQuery(): clearing schema results = " + results.toString());
                    aliasClient.close();
                }

                // Will fails if aliases are not cleared.
                results = client.submit(String.format(SYSTEM_DROP_GRAPH, graphName)).all().get();
                System.out.println("testRemoteGraphWithStringQuery(): remove graph results = " + results.toString());
            }
        } finally {
            if (client != null) {
                client.close();
            }
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    private void createCustomVertexIdSchema(final Client client, final String zoneIdKey, final String resourceIdKey,
            final String resourceLabel) throws InterruptedException, ExecutionException {
        List<Result> results = client.submit(String.format(SCHEMA_CREATE_PROPERTY_KEY, zoneIdKey)).all().get();
        System.out.println("createCustomeVertexIdSchema(): create zone id property key result = " + results.toString());
        results = client.submit(String.format(SCHEMA_CREATE_PROPERTY_KEY, resourceIdKey)).all().get();
        System.out.println(
                "createCustomeVertexIdSchema(): create resource id property key result = " + results.toString());
        results = client.submit(String.format(SCHEMA_CREATE_CUSTOM_VERTEX_ID, resourceLabel, zoneIdKey, resourceIdKey))
                .all().get();
        System.out.println("createCustomeVertexIdSchema(): create custom vertex id result = " + results.toString());

        results = client.submit(SCHEMA_DESCRIBE).all().get();
        System.out.println("createCustomeVertexIdSchema(): resulting schema = " + results.toString());
    }

    private void addVertex(final Client client, final String resourceLabel, final String zoneIdKey,
            final String zoneIdValue, final String resourceIdKey, final String resourceIdValue)
            throws InterruptedException, ExecutionException {
        List<Result> results = client.submit(
                String.format(QUERY_ADD_VERTEX, resourceLabel, zoneIdKey, zoneIdValue, resourceIdKey, resourceIdValue))
                .all().get();
        System.out.println("addVertex(): " + results.toString());
        Vertex vertex = results.get(0).getVertex();
        Assert.assertEquals(resourceLabel, vertex.label());
        Assert.assertTrue(vertex.id().toString().contains(zoneIdKey + "=" + zoneIdValue));
        Assert.assertTrue(vertex.id().toString().contains(resourceIdKey + "=" + resourceIdValue));
    }

    private Vertex getVertexByCustomId(final Client client, final String resourceLabel, final String zoneIdKey,
            final String zoneIdValue, final String resourceIdKey, final String resourceIdValue)
            throws InterruptedException, ExecutionException {
        List<Result> results = client.submit(
                String.format(QUERY_VERTEX, resourceLabel, zoneIdKey, zoneIdValue, resourceIdKey, resourceIdValue))
                .all().get();
        System.out.println("getVertexByCustomId(): " + results.toString());
        Assert.assertEquals(1, results.size());
        Vertex vertex = results.get(0).getVertex();
        Assert.assertEquals(resourceLabel, vertex.label());
        Assert.assertTrue(vertex.id().toString().contains(zoneIdKey + "=" + zoneIdValue));
        Assert.assertTrue(vertex.id().toString().contains(resourceIdKey + "=" + resourceIdValue));
        return vertex;
    }

    @Test
    public void testRemoteGraphWithFluentAPI() throws Exception {
        final String graphName = "jgraphB";

        // http://tinkerpop.apache.org/docs/3.2.3/reference/#connecting-via-remotegraph:
        // To configure a "remote" traversal, there first needs to be a
        // TraversalSource.
        // A TraversalSource can be generated from any Graph instance with the
        // traversal() method.
        // Any traversals generated from this source using the withRemote()
        // configuration option
        // will not execute against the local graph. That could be confusing and
        // it maybe be easier
        // to think of the local graph as being "empty".
        // It is recommended that when using withRemote(), the TraversalSource
        // be generated with EmptyGraph
        // Remote graph could be traversed using remote traversal source, but it
        // could not be manipulated.
        Graph graph = EmptyGraph.instance();
        System.out.println("testRemoteGraphWithFluentAPI(): graph = " + graph.toString());

        Cluster cluster = null;
        Client client = null;
        try {
            cluster = Cluster.build().addContactPoint("127.0.0.1").create();
            System.out.println("testRemoteGraphWithFluentAPI(): cluster = " + cluster);

            client = cluster.connect();
            System.out.println("testRemoteGraphWithFluentAPI(): client = " + client.toString());

            // Create an empty DSE graph
            // Cassandra keyspace name is equal to graph name
            List<Result> results = client.submit(String.format(SYSTEM_CREATE_GRAPH, graphName)).all().get();
            System.out.println("testRemoteGraphWithFluentAPI(): create graph results = " + results.toString());

            GraphTraversalSource g = null;
            try {
                g = graph.traversal().withRemote(DriverRemoteConnection.using(cluster, graphName + ".g"));
                System.out.println("testRemoteGraphWithFluentAPI(): traversal source = " + g.toString());

                System.out.println("testRemoteGraphWithFluentAPI(): vertices count before = " + g.V().count().next());
                Assert.assertEquals(0, g.V().count().next().intValue());

                g.addV("resource").property("name", "resourceOne").next();
                g.addV("resource").property("name", "resourceTwo").next();

                System.out.println("testRemoteGraphWithFluentAPI(): vertices count after = " + g.V().count().next());
                Assert.assertEquals(2, g.V().count().next().intValue());

                System.out.println(
                        "testRemoteGraphWithFluentAPI(): vertices = " + g.V().valueMap(true).next(2).toString());
            } finally {
                if (g != null) {
                    g.close();
                }

                results = client.submit(String.format(SYSTEM_DROP_GRAPH, graphName)).all().get();
                System.out.println("testRemoteGraphWithFluentAPI(): remove graph results = " + results.toString());
            }
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
