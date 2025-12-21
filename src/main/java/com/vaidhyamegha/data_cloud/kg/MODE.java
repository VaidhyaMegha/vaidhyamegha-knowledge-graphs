package com.vaidhyamegha.data_cloud.kg;

/**
 * Execution modes for the knowledge graph application.
 *
 * BUILD  - Build knowledge graph in Samyama using Cypher CREATE
 * CLI    - Run Cypher queries against Samyama
 * SERVER - Samyama already runs as server on port 6379
 */
enum MODE {
    BUILD("build"),
    CLI("cli"),
    SERVER("server");

    String name = "";

    MODE(String m) {
        if (m.equals("build") || m.equals("cli") || m.equals("server"))
            name = m;
        else throw new RuntimeException("Unsupported mode");
    }
}
