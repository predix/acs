package com.ge.predix.acs.config;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "titan" })
public class DseGraphConfig {

    static final String SYSTEM_CREATE_GRAPH = "system.graph('%s').ifNotExists().create()";
    static final String SCHEMA_CREATE_PROPERTY_KEY = "schema.propertyKey('%s').Text().ifNotExists().create()";

    @Value("${TITAN_ENABLE_CASSANDRA:false}")
    private boolean cassandraEnabled;
    @Value("${CASSANDRA_KEYSPACE:titan}")
    private String cassandraKeyspace;
    @Value("${TITAN_STORAGE_HOSTNAME:localhost}")
    private String hostname;
    @Value("${TITAN_STORAGE_USERNAME:}")
    private String username;
    @Value("${TITAN_STORAGE_PASSWORD:}")
    private String password;
    @Value("${TITAN_STORAGE_PORT:9160}")
    private int port;

    private GraphTraversalSource graphTraversalSource;

    @PostConstruct
    public void init() throws InterruptedException, ExecutionException {
        this.graphTraversalSource = createGraphTraveralSource();
    }

    public GraphTraversalSource createGraphTraveralSource() throws ExecutionException, InterruptedException {
        Graph graph = EmptyGraph.instance();
        Cluster cluster = Cluster.build().addContactPoint(hostname).create();
        Client client = cluster.connect();
        client.submit(String.format(SYSTEM_CREATE_GRAPH, cassandraKeyspace));
        return graph.traversal().withRemote(DriverRemoteConnection.using(cluster, cassandraKeyspace + ".g"));
    }

    private void createCustomVertexIdSchema(final Client client, final String zoneIdKey, final String resourceIdKey,
            final String resourceLabel) throws InterruptedException, ExecutionException {

        client.submit(String.format(SCHEMA_CREATE_PROPERTY_KEY, zoneIdKey));

        results = client.submit(String.format(SCHEMA_CREATE_PROPERTY_KEY, resourceIdKey)).all().get();

        results = client.submit(String.format(SCHEMA_CREATE_CUSTOM_VERTEX_ID, resourceLabel, zoneIdKey, resourceIdKey))
                .all().get();
        System.out.println("createCustomeVertexIdSchema(): create custom vertex id result = " + results.toString());

        results = client.submit(SCHEMA_DESCRIBE).all().get();
    }

    @Bean
    GraphTraversalSource graphTraversal() {
        return this.graphTraversalSource;
    }
}
