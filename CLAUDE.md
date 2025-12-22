# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VaidhyaMegha Knowledge Graphs builds an open knowledge graph on clinical trials, connecting:
- Clinical trials from WHO ICTRP and ClinicalTrials.gov (750K+ trials)
- Medical conditions/interventions via MeSH vocabulary
- PubMed research articles
- Phenotype-genotype associations (PheGenI)
- Medical term co-occurrences (MRCOC/MEDLINE)

The output is an RDF graph (5M+ triples) queryable via SPARQL or GraphQL.

## Build Commands

```bash
# Build JAR with dependencies
mvn clean package assembly:single -DskipTests

# Run tests
mvn test

# Run specific test
mvn test -Dtest=AppTest
```

## Running the Application

Three execution modes controlled by `-m` flag:

```bash
# Mode 1: BUILD - Generate RDF knowledge graph (default)
java -jar -Xms4096M -Xmx8192M target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar

# Mode 2: CLI - Run SPARQL queries against pre-built RDF
java -jar -Xms4096M -Xmx8192M target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar \
  -m cli -q src/main/sparql/1_count_of_records.rq

# Mode 3: SERVER - Start GraphQL server (port 8080)
java -cp "target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar:lib/*" \
  com.vaidhyamegha.data_cloud.kg.App -m server
```

## Architecture

### Core Java Components (src/main/java/com/vaidhyamegha/data_cloud/kg/)

- **App.java**: Main entry point. Orchestrates the RDF graph building pipeline:
  1. Loads trials from AACT PostgreSQL database
  2. Links trials to MeSH terms (conditions/interventions)
  3. Associates trials with PubMed articles via Entrez API
  4. Adds MRCOC co-occurrences and PheGenI phenotype-genotype links
  5. Exports to N-Triples format

- **MODE.java**: Enum defining execution modes (BUILD, CLI, SERVER)

- **RESOURCE.java**: Factory for creating RDF resources with proper URIs:
  - TRIAL → clinicaltrials.gov or WHO ICTRP URLs
  - PUBMED_ARTICLE → pubmed.ncbi.nlm.nih.gov URLs
  - MESH_DUI → meshb.nlm.nih.gov URLs
  - GENE_ID → ncbi.nlm.nih.gov/gene URLs

- **Constants.java**: RDF namespace URIs and delimiters

- **EntrezClient.java**: PubMed E-utilities API client for fetching article associations

### Perl Examples (examples/)

Standalone Perl scripts demonstrating data linking approaches:
- `symptoms_diseases/` - Links symptoms to diseases via PubMed co-occurrence
- `trials_articles/` - Links clinical trials to PubMed articles
- `phenotype_genotype/` - Filters PheGenI data for specific symptoms
- `diseases_drugs/` - Links diseases to drugs via PubMed co-occurrence

**Note**: Perl examples have hardcoded paths. Use `git stash` workflow to temporarily modify paths for local testing.

Required Perl modules: `LWP::Simple`, `Array::Utils`

## Key Configuration Files

- `src/main/resources/config.properties` - Database credentials, SQL queries, API thresholds
- `src/main/resources/hql-config.json` - HyperGraphQL server configuration
- `src/main/resources/schema.graphql` - GraphQL schema definition

## Required External Data Files

Download to `data/open_knowledge_graph_on_clinical_trials/`:
- `vocabulary_1.0.0.ttl` - MeSH vocabulary (from NLM)
- `mesh2022.nt` - MeSH RDF data (from NLM)
- `PheGenI_Association_full.tab` - Phenotype-genotype associations (from NCBI)
- `detailed_CoOccurs_2021_selected_fields_sorted.txt` - MRCOC co-occurrence data (processed)

## Required External JARs

Download to `lib/`:
- `algs4.jar` - Princeton algorithms library
- `hypergraphql-3.0.1-exe.jar` - GraphQL interface

## Technology Stack

- Java 17, Maven 3.x
- Apache Jena 4.3.2 (RDF processing)
- HyperGraphQL 3.0.1 (GraphQL interface)
- PostgreSQL (AACT clinical trials database)
- Args4J (CLI parsing)

## Memory Requirements

- Minimum: 4GB heap (`-Xms4096M`)
- Recommended: 8GB heap (`-Xmx8192M`)
- Large datasets: 16GB+ system RAM
