# Graph Database vs Knowledge Graph

This document explains the difference between a Graph Database and a Knowledge Graph, and how they work together.

---

## Quick Summary

| Term | What it is | Example |
|------|------------|---------|
| **Graph Database** | A database engine that stores and queries graph data | Samyama, Neo4j, Amazon Neptune |
| **Knowledge Graph** | Structured domain knowledge represented as nodes and edges | Clinical Trials KG, Google Knowledge Graph |
| **Knowledge Graph Builder** | Tool/application that creates and loads knowledge graphs | VaidhyaMegha Java App |

---

## Graph Database (Samyama)

A **Graph Database** is a general-purpose storage and query engine. It:

- Stores data as **nodes** (entities) and **edges** (relationships)
- Provides a **query language** (Cypher) to search and traverse the graph
- Handles **persistence** (saving data to disk)
- Is **domain-agnostic** - it doesn't know or care what data you store

### What Samyama Provides:

```
┌─────────────────────────────────────────────┐
│                SAMYAMA                      │
├─────────────────────────────────────────────┤
│  1. Cypher Parser     - Understands queries │
│  2. Query Planner     - Optimizes execution │
│  3. Query Executor    - Runs the query      │
│  4. Storage Engine    - RocksDB persistence │
│  5. RESP Protocol     - Redis-compatible API│
└─────────────────────────────────────────────┘
```

### Analogy:
- **Samyama = Empty warehouse** with an inventory management system
- It can store anything, but it's empty until you fill it

---

## Knowledge Graph

A **Knowledge Graph** is structured domain knowledge represented as a graph. It:

- Defines **what entities exist** (nodes: Trials, Drugs, Conditions)
- Defines **how entities relate** (edges: STUDIES, USES, REFERENCED_IN)
- Contains **domain-specific data** with meaningful properties
- Represents **real-world knowledge** in a queryable format

### What a Knowledge Graph Contains:

```
┌─────────────────────────────────────────────┐
│         CLINICAL TRIALS KNOWLEDGE GRAPH     │
├─────────────────────────────────────────────┤
│  Nodes:                                     │
│    - Trial (id, title, status, phase)       │
│    - Condition (name, mesh_id)              │
│    - Drug (name, mesh_id)                   │
│    - Article (pmid, uri)                    │
│    - Gene (gene_id, uri)                    │
│                                             │
│  Edges:                                     │
│    - Trial -[STUDIES]-> Condition           │
│    - Trial -[USES]-> Drug                   │
│    - Trial -[REFERENCED_IN]-> Article       │
│    - Condition -[ASSOCIATED_WITH]-> Gene    │
└─────────────────────────────────────────────┘
```

### Analogy:
- **Knowledge Graph = Organized inventory** inside the warehouse
- It's the actual content with structure and meaning

---

## Knowledge Graph Builder

A **Knowledge Graph Builder** is the tool that creates knowledge graphs. It:

- **Extracts** data from various sources (databases, APIs, files)
- **Transforms** raw data into graph structure (decides nodes, edges, properties)
- **Loads** the structured data into the graph database

### What VaidhyaMegha Java App Does:

```
┌─────────────────────────────────────────────────────────────┐
│           KNOWLEDGE GRAPH BUILDER (Java App)                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  EXTRACT:                                                   │
│    ├── PostgreSQL (AACT) ──────> 561K Clinical Trials       │
│    ├── Entrez API ─────────────> PubMed Articles            │
│    ├── MeSH Files ─────────────> Medical Vocabulary         │
│    └── PheGenI File ───────────> Gene Associations          │
│                                                             │
│  TRANSFORM:                                                 │
│    ├── Trial record ───────────> Trial Node                 │
│    ├── MeSH condition ─────────> Condition Node             │
│    ├── MeSH intervention ──────> Drug Node                  │
│    └── Relationships ──────────> Edges                      │
│                                                             │
│  LOAD:                                                      │
│    └── Cypher CREATE queries ──> Samyama Graph DB           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Analogy:
- **Builder = Warehouse worker** who collects items, organizes them, and shelves them

---

## How They Work Together

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   DATA SOURCES   │     │     BUILDER      │     │   GRAPH DATABASE │
│                  │     │                  │     │                  │
│  - PostgreSQL    │────>│  Java App        │────>│  Samyama         │
│  - Entrez API    │     │  (ETL Pipeline)  │     │                  │
│  - MeSH Files    │     │                  │     │  Stores the      │
│  - PheGenI       │     │  Transforms to   │     │  Knowledge Graph │
│                  │     │  Graph Structure │     │                  │
└──────────────────┘     └──────────────────┘     └──────────────────┘
                                                           │
                                                           │
                                                           ▼
                                                  ┌──────────────────┐
                                                  │  CYPHER QUERIES  │
                                                  │                  │
                                                  │  MATCH (t:Trial) │
                                                  │  -[:STUDIES]->   │
                                                  │  (c:Condition)   │
                                                  │  RETURN c.name   │
                                                  └──────────────────┘
```

---

## Using Samyama for Different Domains

Samyama is domain-agnostic. You can use it for ANY domain by creating different knowledge graphs:

### Example: Multiple Domains in One Database

```
┌─────────────────────────────────────────────────────────────────┐
│                         SAMYAMA                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │ Clinical Trials KG  │  │ E-commerce KG       │              │
│  │                     │  │                     │              │
│  │ Trial, Drug,        │  │ Product, Customer,  │              │
│  │ Condition, Article  │  │ Order, Category     │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │ Fraud Detection KG  │  │ Social Network KG   │              │
│  │                     │  │                     │              │
│  │ Account, Transaction│  │ User, Post,         │              │
│  │ Device, Location    │  │ Comment, Like       │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### For Each Domain, You Need:

| Domain | Data Source | Builder | Result |
|--------|-------------|---------|--------|
| Clinical Trials | AACT, PubMed, MeSH | Java App | Trials KG |
| E-commerce | Product DB, Orders DB | Python ETL | Products KG |
| Fraud Detection | Transaction logs | Spark Pipeline | Fraud KG |
| Social Network | User activity logs | Stream Processor | Social KG |

---

## Key Takeaways

1. **Graph Database (Samyama)** = The storage engine (domain-agnostic)
2. **Knowledge Graph** = The structured data inside (domain-specific)
3. **Builder** = The tool that creates and loads knowledge graphs
4. **One Samyama can hold multiple knowledge graphs** for different domains
5. **Each domain needs its own builder** to create its knowledge graph

---

## Our Implementation

```
VaidhyaMegha Knowledge Graphs Project
│
├── Samyama Graph DB (Rust)
│   ├── Cypher parser and executor
│   ├── RocksDB storage
│   └── RESP protocol server
│
├── Knowledge Graph Builder (Java)
│   ├── SamyamaClient.java - Connects to Samyama
│   ├── CypherQueryBuilder.java - Builds Cypher queries
│   └── App.java - ETL pipeline
│
└── Result: Clinical Trials Knowledge Graph
    ├── 1,000 Trial nodes
    ├── 629 Condition nodes
    ├── 1,152 Drug nodes
    ├── 575 Article nodes
    ├── 13,598 STUDIES edges
    ├── 10,842 USES edges
    └── 587 REFERENCED_IN edges
```

---

## Conclusion

- **Samyama** is the tool (like a Swiss Army knife)
- **Knowledge Graph** is what you build with it (like a carved sculpture)
- **Builder** is the craftsman who creates the sculpture

For Samyama to be useful in different domains, you need builders for each domain that create and load domain-specific knowledge graphs.
