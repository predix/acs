package com.ge.predix.acs.config;

import static com.ge.predix.acs.privilege.management.dao.GraphGenericRepository.PARENT_EDGE_LABEL;
import static com.ge.predix.acs.privilege.management.dao.GraphGenericRepository.SCOPE_PROPERTY_KEY;
import static com.ge.predix.acs.privilege.management.dao.GraphGenericRepository.VERSION_VERTEX_LABEL;
import static com.ge.predix.acs.privilege.management.dao.GraphGenericRepository.ZONE_ID_KEY;
import static com.ge.predix.acs.privilege.management.dao.GraphResourceRepository.RESOURCE_ID_KEY;
import static com.ge.predix.acs.privilege.management.dao.GraphResourceRepository.RESOURCE_LABEL;
import static com.ge.predix.acs.privilege.management.dao.GraphSubjectRepository.SUBJECT_ID_KEY;
import static com.ge.predix.acs.privilege.management.dao.GraphSubjectRepository.SUBJECT_LABEL;

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
@Profile({ "DSE" })
public class DseGraphConfig {

    static final String BY_SCOPE_INDEX_NAME = "byScopeIndex";

    static final String SYSTEM_CREATE_GRAPH = "system.graph('%s').ifNotExists().create()";
    static final String SCHEMA_CREATE_PROPERTY_KEY = "schema.propertyKey('%s').Text().ifNotExists().create()";
    static final String SCHEMA_CREATE_CUSTOM_VERTEX_ID =
            "schema.vertexLabel('%s').partitionKey('%s').clusteringKey" + "('%s').ifNotExists().create()";
    static final String SCHEMA_CREATE_EDGE_INDEX =
            "schema.vertexLabel('%s').index('%s').outE('%s').by" + "('%s').ifNotExists().add()";
    static final String SCHEMA_CREATE_EDGE_LABEL = "schema.edgeLabel('%s').ifNotExists().create()";
    static final String SCHEMA_CREATE_VERTEX_LABEL = "schema.vertexLabel('%s').ifNotExists().create()";

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
        client = client.alias(cassandraKeyspace + ".g");

        createVertexLabel(client, RESOURCE_LABEL);
        createVertexLabel(client, SUBJECT_LABEL);
        createVertexLabel(client, VERSION_VERTEX_LABEL);
        client.submit(String.format(SCHEMA_CREATE_EDGE_LABEL, PARENT_EDGE_LABEL));

        client.submit(String.format(SCHEMA_CREATE_PROPERTY_KEY, ZONE_ID_KEY));
        createCustomVertexIdSchema(client, RESOURCE_ID_KEY, RESOURCE_LABEL);
        createCustomVertexIdSchema(client, SUBJECT_ID_KEY, SUBJECT_LABEL);

        createEdgeIndex(client, BY_SCOPE_INDEX_NAME, PARENT_EDGE_LABEL, SCOPE_PROPERTY_KEY);

        return graph.traversal().withRemote(DriverRemoteConnection.using(cluster, cassandraKeyspace + ".g"));
    }

    private void createCustomVertexIdSchema(final Client client, final String key, final String label)
            throws InterruptedException, ExecutionException {
        client.submit(String.format(SCHEMA_CREATE_PROPERTY_KEY, key));
        client.submit(String.format(SCHEMA_CREATE_CUSTOM_VERTEX_ID, label, ZONE_ID_KEY, key));
    }

    private void createEdgeIndex(final Client client, final String index, final String label,
            final String propertyKey) {
        client.submit(String.format(SCHEMA_CREATE_EDGE_INDEX, RESOURCE_LABEL, index, label, propertyKey));
        client.submit(String.format(SCHEMA_CREATE_EDGE_INDEX, SUBJECT_LABEL, index, label, propertyKey));

    }

    private void createVertexLabel(final Client client, final String label) {
        client.submit(String.format(SCHEMA_CREATE_VERTEX_LABEL, label));
    }

    @Bean
    GraphTraversalSource graphTraversal() {
        return this.graphTraversalSource;
    }
}
