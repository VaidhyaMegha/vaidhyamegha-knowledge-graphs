package com.vaidhyamegha.data_cloud.kg;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.logging.Logger;

/**
 * Client for connecting to Samyama Graph Database.
 *
 * Samyama uses the Redis protocol (RESP) on port 6379.
 * This client wraps Jedis to send Cypher queries via GRAPH.QUERY command.
 *
 * Usage:
 *   SamyamaClient client = new SamyamaClient("localhost", 6379, "knowledge_graph");
 *   client.executeQuery("CREATE (n:Trial {id: 'NCT001'})");
 *   client.close();
 */
public class SamyamaClient implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(SamyamaClient.class.getName());

    private final JedisPool pool;
    private final String graphName;

    /**
     * Create a new Samyama client with default settings.
     * Connects to localhost:6379 with graph name "knowledge_graph"
     */
    public SamyamaClient() {
        this("localhost", 6379, "knowledge_graph");
    }

    /**
     * Create a new Samyama client with custom settings.
     *
     * @param host Samyama server host
     * @param port Samyama server port (default 6379)
     * @param graphName Name of the graph to use
     */
    public SamyamaClient(String host, int port, String graphName) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);  // Max connections in pool
        poolConfig.setMaxIdle(5);    // Max idle connections

        this.pool = new JedisPool(poolConfig, host, port);
        this.graphName = graphName;
        this.host = host;
        this.port = port;

        logger.info("SamyamaClient initialized: " + host + ":" + port + " graph=" + graphName);
    }

    /**
     * Execute a Cypher query on Samyama.
     *
     * @param cypherQuery The Cypher query to execute (e.g., "CREATE (n:Person {name: 'Alice'})")
     * @return The result from Samyama
     */
    public Object executeQuery(String cypherQuery) {
        // Use raw socket to send RESP command directly
        // Bypasses any Jedis command formatting issues
        try (java.net.Socket socket = new java.net.Socket(host, port);
             java.io.OutputStream out = socket.getOutputStream();
             java.io.BufferedReader in = new java.io.BufferedReader(
                 new java.io.InputStreamReader(socket.getInputStream()))) {

            // Build RESP array: *3\r\n$11\r\nGRAPH.QUERY\r\n$<len>\r\n<graph>\r\n$<len>\r\n<query>\r\n
            String cmd = "GRAPH.QUERY";
            StringBuilder resp = new StringBuilder();
            resp.append("*3\r\n");
            resp.append("$").append(cmd.length()).append("\r\n").append(cmd).append("\r\n");
            resp.append("$").append(graphName.length()).append("\r\n").append(graphName).append("\r\n");
            resp.append("$").append(cypherQuery.length()).append("\r\n").append(cypherQuery).append("\r\n");

            out.write(resp.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.flush();

            // Read response
            String response = in.readLine();
            return response;
        } catch (Exception e) {
            logger.severe("Error executing query: " + e.getMessage());
            throw new RuntimeException("Failed to execute Cypher query: " + cypherQuery, e);
        }
    }

    // Store host/port for raw socket connection
    private final String host;
    private final int port;

    /**
     * Execute a CREATE query to add a node.
     *
     * @param cypherQuery The CREATE query
     * @return true if successful
     */
    public boolean createNode(String cypherQuery) {
        try {
            executeQuery(cypherQuery);
            return true;
        } catch (Exception e) {
            logger.warning("Failed to create node: " + e.getMessage());
            return false;
        }
    }

    /**
     * Execute a CREATE query to add an edge.
     *
     * @param cypherQuery The CREATE query for edge
     * @return true if successful
     */
    public boolean createEdge(String cypherQuery) {
        try {
            executeQuery(cypherQuery);
            return true;
        } catch (Exception e) {
            logger.warning("Failed to create edge: " + e.getMessage());
            return false;
        }
    }

    /**
     * Execute a batch of CREATE queries.
     * More efficient than individual calls.
     *
     * @param queries List of Cypher CREATE queries
     * @return Number of successful queries
     */
    public int executeBatch(List<String> queries) {
        int successCount = 0;
        for (String query : queries) {
            try {
                executeQuery(query);
                successCount++;
            } catch (Exception e) {
                logger.warning("Batch query failed: " + query + " - " + e.getMessage());
            }
        }
        logger.info("Batch executed: " + successCount + "/" + queries.size() + " successful");
        return successCount;
    }

    /**
     * Test connection to Samyama.
     *
     * @return true if connected
     */
    public boolean testConnection() {
        try (Jedis jedis = pool.getResource()) {
            String response = jedis.ping();
            return "PONG".equals(response);
        } catch (Exception e) {
            logger.severe("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear all data in the graph.
     * Use with caution!
     */
    public void clearGraph() {
        executeQuery("MATCH (n) DETACH DELETE n");
        logger.info("Graph cleared: " + graphName);
    }

    /**
     * Get count of nodes in the graph.
     *
     * @return Number of nodes
     */
    public Object getNodeCount() {
        return executeQuery("MATCH (n) RETURN count(n)");
    }

    @Override
    public void close() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
            logger.info("SamyamaClient connection pool closed");
        }
    }
}
