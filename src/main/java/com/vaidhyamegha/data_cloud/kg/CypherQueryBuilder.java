package com.vaidhyamegha.data_cloud.kg;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder for generating Cypher CREATE queries.
 *
 * Converts knowledge graph data into Cypher statements that Samyama can execute.
 *
 * Usage:
 *   // Create a node
 *   String query = CypherQueryBuilder.createNode("Trial", Map.of("id", "NCT001", "title", "Study"));
 *   // Result: CREATE (n:Trial {id: 'NCT001', title: 'Study'})
 *
 *   // Create an edge
 *   String query = CypherQueryBuilder.createEdge("Trial", "id", "NCT001", "STUDIES", "Condition", "name", "Diabetes");
 *   // Result: MATCH (a:Trial {id: 'NCT001'}), (b:Condition {name: 'Diabetes'}) CREATE (a)-[:STUDIES]->(b)
 */
public class CypherQueryBuilder {

    /**
     * Create a Cypher query to create a node with a label and properties.
     *
     * @param label The node label (e.g., "Trial", "Condition", "Drug")
     * @param properties Map of property name to value
     * @return Cypher CREATE query string
     *
     * Example:
     *   createNode("Trial", Map.of("id", "NCT001", "title", "Cancer Study"))
     *   Returns: CREATE (n:Trial {id: 'NCT001', title: 'Cancer Study'})
     */
    public static String createNode(String label, Map<String, Object> properties) {
        String propsString = formatProperties(properties);
        return String.format("CREATE (n:%s {%s})", label, propsString);
    }

    /**
     * Create a Cypher query to create a node with just a label and ID.
     *
     * @param label The node label
     * @param id The unique identifier for the node
     * @return Cypher CREATE query string
     */
    public static String createNode(String label, String id) {
        return String.format("CREATE (n:%s {id: \"%s\"})", label, escapeString(id));
    }

    /**
     * Create a Cypher query to create an edge between two existing nodes.
     *
     * @param fromLabel Label of the source node
     * @param fromKey Property key to match source node
     * @param fromValue Property value to match source node
     * @param relationshipType The edge/relationship type (e.g., "STUDIES", "USES")
     * @param toLabel Label of the target node
     * @param toKey Property key to match target node
     * @param toValue Property value to match target node
     * @return Cypher query string
     *
     * Example:
     *   createEdge("Trial", "id", "NCT001", "STUDIES", "Condition", "name", "Diabetes")
     *   Returns: MATCH (a:Trial {id: 'NCT001'}), (b:Condition {name: 'Diabetes'}) CREATE (a)-[:STUDIES]->(b)
     */
    public static String createEdge(String fromLabel, String fromKey, String fromValue,
                                    String relationshipType,
                                    String toLabel, String toKey, String toValue) {
        return String.format(
            "MATCH (a:%s {%s: \"%s\"}), (b:%s {%s: \"%s\"}) CREATE (a)-[:%s]->(b)",
            fromLabel, fromKey, escapeString(fromValue),
            toLabel, toKey, escapeString(toValue),
            relationshipType
        );
    }

    /**
     * Create a Cypher query to create an edge with properties.
     *
     * @param fromLabel Label of the source node
     * @param fromKey Property key to match source node
     * @param fromValue Property value to match source node
     * @param relationshipType The edge/relationship type
     * @param edgeProperties Properties for the edge itself
     * @param toLabel Label of the target node
     * @param toKey Property key to match target node
     * @param toValue Property value to match target node
     * @return Cypher query string
     */
    public static String createEdgeWithProperties(
            String fromLabel, String fromKey, String fromValue,
            String relationshipType, Map<String, Object> edgeProperties,
            String toLabel, String toKey, String toValue) {
        String propsString = formatProperties(edgeProperties);
        return String.format(
            "MATCH (a:%s {%s: \"%s\"}), (b:%s {%s: \"%s\"}) CREATE (a)-[:%s {%s}]->(b)",
            fromLabel, fromKey, escapeString(fromValue),
            toLabel, toKey, escapeString(toValue),
            relationshipType, propsString
        );
    }

    /**
     * Create a Cypher query to create a node and edge together.
     * Useful when creating a new node that links to an existing node.
     *
     * @param existingLabel Label of existing node
     * @param existingKey Key to match existing node
     * @param existingValue Value to match existing node
     * @param relationshipType The edge type
     * @param newLabel Label for the new node
     * @param newProperties Properties for the new node
     * @return Cypher query string
     */
    public static String createNodeAndEdge(
            String existingLabel, String existingKey, String existingValue,
            String relationshipType,
            String newLabel, Map<String, Object> newProperties) {
        String propsString = formatProperties(newProperties);
        return String.format(
            "MATCH (a:%s {%s: \"%s\"}) CREATE (a)-[:%s]->(b:%s {%s})",
            existingLabel, existingKey, escapeString(existingValue),
            relationshipType,
            newLabel, propsString
        );
    }

    /**
     * Create a MERGE query - creates node only if it doesn't exist.
     * Useful for avoiding duplicate nodes.
     *
     * @param label The node label
     * @param properties Map of properties
     * @return Cypher MERGE query string
     */
    public static String mergeNode(String label, Map<String, Object> properties) {
        String propsString = formatProperties(properties);
        return String.format("MERGE (n:%s {%s})", label, propsString);
    }

    /**
     * Format a properties map into Cypher property string.
     * Handles string escaping and type conversion.
     * Uses double quotes for strings to avoid issues with apostrophes in values.
     *
     * @param properties Map of property name to value
     * @return Formatted string like "key1: \"value1\", key2: 123"
     */
    private static String formatProperties(Map<String, Object> properties) {
        return properties.entrySet().stream()
            .map(entry -> {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    // Use double quotes - apostrophes in values don't need escaping
                    return key + ": \"" + escapeString((String) value) + "\"";
                } else if (value instanceof Number) {
                    return key + ": " + value;
                } else if (value instanceof Boolean) {
                    return key + ": " + value;
                } else {
                    return key + ": \"" + escapeString(value.toString()) + "\"";
                }
            })
            .collect(Collectors.joining(", "));
    }

    /**
     * Escape special characters in strings for Cypher.
     * Since we use double quotes for strings, we only need to escape:
     * - Backslashes (must be first)
     * - Double quotes
     * - Control characters (newlines, tabs, etc.)
     *
     * @param input The string to escape
     * @return Escaped string safe for Cypher queries
     */
    private static String escapeString(String input) {
        if (input == null) return "";
        return input
            .replace("\\", "\\\\")  // Escape backslashes first
            .replace("\"", "\\\"")  // Escape double quotes
            .replace("\n", "\\n")   // Escape newlines
            .replace("\r", "\\r")   // Escape carriage returns
            .replace("\t", "\\t");  // Escape tabs
        // Note: Single quotes (apostrophes) don't need escaping inside double-quoted strings
    }

    // ============================================================
    // Convenience methods for Clinical Trials Knowledge Graph
    // ============================================================

    /**
     * Create a Trial node.
     */
    public static String createTrial(String trialId, String title, String status) {
        Map<String, Object> props = new HashMap<>();
        props.put("id", trialId);
        if (title != null) props.put("title", title);
        if (status != null) props.put("status", status);
        return createNode("Trial", props);
    }

    /**
     * Create a Condition (disease) node.
     */
    public static String createCondition(String meshId, String name) {
        Map<String, Object> props = new HashMap<>();
        props.put("mesh_id", meshId);
        if (name != null) props.put("name", name);
        return createNode("Condition", props);
    }

    /**
     * Create a Drug/Intervention node.
     */
    public static String createDrug(String meshId, String name) {
        Map<String, Object> props = new HashMap<>();
        props.put("mesh_id", meshId);
        if (name != null) props.put("name", name);
        return createNode("Drug", props);
    }

    /**
     * Create a PubMed Article node.
     */
    public static String createArticle(String pmid, String title) {
        Map<String, Object> props = new HashMap<>();
        props.put("pmid", pmid);
        if (title != null) props.put("title", title);
        return createNode("Article", props);
    }

    /**
     * Create a Gene node.
     */
    public static String createGene(String geneId, String symbol) {
        Map<String, Object> props = new HashMap<>();
        props.put("gene_id", geneId);
        if (symbol != null) props.put("symbol", symbol);
        return createNode("Gene", props);
    }

    /**
     * Create STUDIES edge: Trial -[:STUDIES]-> Condition
     */
    public static String createTrialStudiesCondition(String trialId, String meshId) {
        return createEdge("Trial", "id", trialId, "STUDIES", "Condition", "mesh_id", meshId);
    }

    /**
     * Create USES edge: Trial -[:USES]-> Drug
     */
    public static String createTrialUsesDrug(String trialId, String meshId) {
        return createEdge("Trial", "id", trialId, "USES", "Drug", "mesh_id", meshId);
    }

    /**
     * Create REFERENCED_IN edge: Trial -[:REFERENCED_IN]-> Article
     */
    public static String createTrialReferencedInArticle(String trialId, String pmid) {
        return createEdge("Trial", "id", trialId, "REFERENCED_IN", "Article", "pmid", pmid);
    }

    /**
     * Create ASSOCIATED_WITH edge: Condition -[:ASSOCIATED_WITH]-> Gene
     */
    public static String createConditionAssociatedWithGene(String meshId, String geneId) {
        return createEdge("Condition", "mesh_id", meshId, "ASSOCIATED_WITH", "Gene", "gene_id", geneId);
    }
}
