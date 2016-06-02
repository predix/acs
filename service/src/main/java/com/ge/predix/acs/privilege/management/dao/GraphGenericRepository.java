package com.ge.predix.acs.privilege.management.dao;

import static com.ge.predix.acs.privilege.management.dao.AttributePredicate.elementOf;
import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
import static org.apache.tinkerpop.gremlin.process.traversal.P.test;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;

import com.ge.predix.acs.model.Attribute;
import com.ge.predix.acs.rest.Parent;
import com.ge.predix.acs.utils.JsonUtils;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import com.thinkaurelius.titan.core.SchemaViolationException;

public abstract class GraphGenericRepository<E extends ZonableEntity> implements JpaRepository<E, Long> {
    private static final JsonUtils JSON_UTILS = new JsonUtils();

    public static final String ATTRIBUTES_PROPERTY_KEY = "attributes";
    public static final String PARENT_EDGE_LABEL = "parent";
    public static final String SCOPE_PROPERTY_KEY = "scope";
    public static final String ZONE_NAME_PROPERTY_KEY = "zoneName";
    public static final String ZONE_ID_KEY = "zoneId";

    @Autowired
    private Graph graph;

    @Value("${TITAN_TRAVERSAL_LIMIT:256}")
    private long traversalLimit = 256;

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public void deleteInBatch(final Iterable<E> entities) {
        delete(entities);
    }

    @Override
    public List<E> findAll() {
        try {
            return this.graph.traversal().V().has(ZONE_ID_KEY).has(getEntityIdKey()).toList().stream()
                    .map(item -> vertexToEntity(item)).collect(Collectors.toList());
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public List<E> findAll(final Sort arg0) {
        throw new NotImplementedException("This repository does not support sortable find all queries.");
    }

    @Override
    public List<E> findAll(final Iterable<Long> ids) {
        try {
            return this.graph.traversal().V(iterableToArray(ids)).toList().stream().map(item -> vertexToEntity(item))
                    .collect(Collectors.toList());
        } finally {
            this.graph.tx().commit();
        }
    }

    private static Object[] iterableToArray(final Iterable<?> iterable) {
        List<Object> idList = new ArrayList<>();
        iterable.forEach(item -> idList.add(item));
        return idList.toArray();
    }

    @Override
    public void flush() {
        // Nothing to do.
    }

    @Override
    public E getOne(final Long id) {
        return findOne(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends E> List<S> save(final Iterable<S> entities) {
        try {
            List<E> savedEntities = new ArrayList<>();
            entities.forEach(item -> savedEntities.add(saveCommon(item)));
            return (List<S>) savedEntities;
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public <S extends E> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public Page<E> findAll(final Pageable pageable) {
        throw new NotImplementedException("This repository does not support pageable find all queries.");
    }

    @Override
    public long count() {
        try {
            return this.graph.traversal().V().has(getEntityIdKey()).count().next();
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public void delete(final Long id) {
        try {
            this.graph.traversal().V(id).drop().iterate();
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public void delete(final E entity) {
        try {
            this.graph.traversal().V(entity.getId()).drop().iterate();
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public void delete(final Iterable<? extends E> entities) {
        try {
            // Assemble list of entity ids.
            List<Long> ids = new ArrayList<>();
            entities.forEach(item -> ids.add(item.getId()));
            // Delete all entities in one fell swoop.
            this.graph.traversal().V(ids.toArray()).drop().iterate();
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public void deleteAll() {
        try {
            this.graph.traversal().V().has(getEntityIdKey()).drop().iterate();
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public boolean exists(final Long id) {
        try {
            GraphTraversal<Vertex, Vertex> traversal = this.graph.traversal().V(id);
            if (traversal.hasNext()) {
                return true;
            }
            return false;
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public E findOne(final Long id) {
        try {
            GraphTraversal<Vertex, Vertex> traversal = this.graph.traversal().V(id);
            if (traversal.hasNext()) {
                Vertex vertex = traversal.next();
                E entity = vertexToEntity(vertex);
                return entity;
            }
            return null;
        } finally {
            this.graph.tx().commit();
        }
    }

    @Override
    public <S extends E> S save(final S entity) {
        try {
            return saveCommon(entity);
        } finally {
            this.graph.tx().commit();
        }
    }

    private <S extends E> S saveCommon(final S entity) {
        // Create the entity if the id is null otherwise update an existing entity.
        if ((null == entity.getId()) || (0 == entity.getId())) {
            verifyEntityNotSelfReferencing(entity);
            String entityId = getEntityId(entity);
            String zoneId = entity.getZone().getName();
            Vertex entityVertex = this.graph.addVertex(T.label, getEntityLabel(), ZONE_ID_KEY, zoneId, getEntityIdKey(),
                    entityId);
            updateVertexProperties(entity, entityVertex);
            saveParentRelationships(entity, entityVertex, false);
            entity.setId((long) entityVertex.id());
        } else {
            verifyEntityReferencesNotCyclic(entity);
            GraphTraversal<Vertex, Vertex> traversal = this.graph.traversal().V(entity.getId());
            Vertex entityVertex = traversal.next();
            updateVertexProperties(entity, entityVertex);
            saveParentRelationships(entity, entityVertex, true);
        }
        return entity;
    }

    private void verifyEntityNotSelfReferencing(final E entity) {
        if (entity.getParents().contains(new Parent(getEntityId(entity)))) {
            throw new SchemaViolationException(
                    String.format("The entity '%s' references itself as a parent.", getEntityId(entity)));
        }
    }

    private void verifyEntityReferencesNotCyclic(final E entity) {
        // First verify the entity does not reference itself as a parent.
        verifyEntityNotSelfReferencing(entity);

        // Now check for potential cyclic references.
        getGraph().traversal().V(entity.getId()).has(getEntityIdKey()).emit().repeat(in().has(getEntityIdKey()))
                .until(eq(null)).values(getEntityIdKey()).toStream().forEach(id -> {
                    if (entity.getParents().contains(new Parent((String) id))) {
                        throw new SchemaViolationException(
                                String.format("Updating entity '%s' with parent '%s' introduces a cyclic reference.",
                                        getEntityId(entity), id));
                    }
                });
    }

    void saveParentRelationships(final E entity, final Vertex vertex, final boolean update) {
        if (update) { // If this is an update remove all existing edges.
            vertex.edges(Direction.OUT, PARENT_EDGE_LABEL).forEachRemaining(edge -> {
                edge.remove();
            });
        }
        entity.getParents().stream().forEach(parent -> {
            saveParentRelationship(entity, vertex, parent);
        });
    }

    private void saveParentRelationship(final E entity, final Vertex vertex, final Parent parent) {
        GraphTraversal<Vertex, Vertex> traversal = getGraph().traversal().V()
                .has(ZONE_ID_KEY, entity.getZone().getName()).has(getEntityIdKey(), parent.getIdentifier());
        if (!traversal.hasNext()) {
            throw new IllegalStateException(String.format("No parent exists in zone '%s' with '%s' value of '%s'.",
                    entity.getZone().getName(), getEntityIdKey(), parent.getIdentifier()));
        }
        if (parent.getScopes().isEmpty()) {
            vertex.addEdge(PARENT_EDGE_LABEL, traversal.next());
            return;
        }
        for (Attribute scope : parent.getScopes()) {
            vertex.addEdge(PARENT_EDGE_LABEL, traversal.next()).property(SCOPE_PROPERTY_KEY,
                    JSON_UTILS.serialize(scope));
        }
    }

    Set<ParentEntity> getParentEntities(final E entity) {
        Set<ParentEntity> parents = new HashSet<>();
        entity.getParents().stream().forEach(parent -> {
            GraphTraversal<Vertex, Vertex> traversal = getGraph().traversal().V()
                    .has(ZONE_ID_KEY, entity.getZone().getName()).has(getEntityIdKey(), parent.getIdentifier());
            if (!traversal.hasNext()) {
                throw new IllegalStateException(String.format("No parent exists in zone '%s' with '%s' value of '%s'.",
                        entity.getZone().getName(), getEntityIdKey(), parent.getIdentifier()));
            }
            parents.add(new ParentEntity(vertexToEntity(traversal.next()), parent.getScopes()));
        });
        return parents;
    }

    public List<E> findByZone(final ZoneEntity zoneEntity) {
        try {
            String zoneName = zoneEntity.getName();
            GraphTraversal<Vertex, Vertex> traversal = this.graph.traversal().V()
                    .has(getEntityLabel(), ZONE_ID_KEY, zoneName).has(getEntityIdKey());
            List<E> entities = new ArrayList<>();
            while (traversal.hasNext()) {
                entities.add(vertexToEntity(traversal.next()));
            }
            return entities;
        } finally {
            this.graph.tx().commit();
        }
    }

    public static String getVertexStringPropertyOrEmpty(final Vertex vertex, final String propertyKey) {
        VertexProperty<String> property = vertex.property(propertyKey);
        if (property.isPresent()) {
            return property.value();
        }
        return "";
    }

    public static String getVertexStringPropertyOrFail(final Vertex vertex, final String propertyKey) {
        VertexProperty<String> property = vertex.property(propertyKey);
        if (property.isPresent()) {
            return property.value();
        }
        throw new IllegalStateException(String.format(
                "The vertex with id '%d' does not conatin the expected property '%s'.", vertex.id(), propertyKey));
    }

    public static String getVertexStringPropertyOrNull(final Vertex vertex, final String propertyKey) {
        VertexProperty<String> property = vertex.property(propertyKey);
        if (property.isPresent()) {
            return property.value();
        }
        return null;
    }

    public E getByZoneAndIdentifier(final ZoneEntity zone, final String identifier) {
        try {
            GraphTraversal<Vertex, Vertex> traversal = getGraph().traversal().V().has(ZONE_ID_KEY, zone.getName())
                    .has(getEntityIdKey(), identifier);
            if (!traversal.hasNext()) {
                return null;
            }
            Vertex vertex = traversal.next();
            E entity = vertexToEntity(vertex);
            searchAttributes(entity, vertex);

            // There should be only one entity with a given entity id.
            Assert.isTrue(!traversal.hasNext(),
                    String.format("There are two entities with the same %s.", getEntityIdKey()));
            return entity;
        } finally {
            getGraph().tx().commit();
        }
    }

    public E getByZoneAndIdentifierAndScopes(final ZoneEntity zone, final String identifier,
            final Set<Attribute> scopes) {
        try {
            GraphTraversal<Vertex, Vertex> traversal = getGraph().traversal().V().has(ZONE_ID_KEY, zone.getName())
                    .has(getEntityIdKey(), identifier);
            if (!traversal.hasNext()) {
                return null;
            }
            Vertex vertex = traversal.next();
            E entity = vertexToEntity(vertex);
            searchAttributesWithScopes(entity, vertex, scopes);

            // There should be only one entity with a given entity id.
            Assert.isTrue(!traversal.hasNext(),
                    String.format("There are two entities with the same %s.", getEntityIdKey()));
            return entity;
        } finally {
            getGraph().tx().commit();
        }
    }

    @SuppressWarnings("unchecked")
    public void searchAttributes(final E entity, final Vertex vertex) {
        Set<Attribute> attributes = new HashSet<>();
        getGraph().traversal().V(vertex.id()).has(ATTRIBUTES_PROPERTY_KEY).emit()
                .repeat(out().simplePath().has(ATTRIBUTES_PROPERTY_KEY)).until(eq(null)).limit(this.traversalLimit)
                .values(ATTRIBUTES_PROPERTY_KEY).toStream().forEach(it -> {
                    attributes.addAll(JSON_UTILS.deserialize((String) it, Set.class, Attribute.class));
                });
        entity.setAttributes(attributes);
        entity.setAttributesAsJson(JSON_UTILS.serialize(attributes));
    }

    @SuppressWarnings("unchecked")
    public void searchAttributesWithScopes(final E entity, final Vertex vertex, final Set<Attribute> scopes) {
        Set<Attribute> attributes = new HashSet<>();

        // First add all attributes inherited from non-scoped relationships.
        getGraph().traversal().V(vertex.id()).has(ATTRIBUTES_PROPERTY_KEY).emit()
                .repeat(outE().hasNot(SCOPE_PROPERTY_KEY).otherV().simplePath().has(ATTRIBUTES_PROPERTY_KEY))
                .until(eq(null)).limit(this.traversalLimit).values(ATTRIBUTES_PROPERTY_KEY).toStream().forEach(it -> {
                    attributes.addAll(JSON_UTILS.deserialize((String) it, Set.class, Attribute.class));
                });

        getGraph().traversal().V(vertex.id()).has(ATTRIBUTES_PROPERTY_KEY).emit()
                .repeat(outE().has(SCOPE_PROPERTY_KEY, test(elementOf(), scopes)).otherV().simplePath()
                        .has(ATTRIBUTES_PROPERTY_KEY))
                .until(eq(null)).limit(this.traversalLimit).values(ATTRIBUTES_PROPERTY_KEY).toStream().forEach(it -> {
                    attributes.addAll(JSON_UTILS.deserialize((String) it, Set.class, Attribute.class));
                });

        entity.setAttributes(attributes);
        entity.setAttributesAsJson(JSON_UTILS.serialize(attributes));
    }

    abstract String getEntityId(E entity);

    abstract String getEntityIdKey();

    abstract String getEntityLabel();

    abstract String getRelationshipKey();

    abstract void updateVertexProperties(final E entity, final Vertex vertex);

    abstract E vertexToEntity(final Vertex vertex);

    public Graph getGraph() {
        return this.graph;
    }

    public void setGraph(final Graph graph) {
        this.graph = graph;
    }

    public long getTraversalLimit() {
        return this.traversalLimit;
    }

    public void setTraversalLimit(final long traversalLimit) {
        this.traversalLimit = traversalLimit;
    }

}
