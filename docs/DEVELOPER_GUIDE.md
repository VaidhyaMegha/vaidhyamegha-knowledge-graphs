# VaidhyaMegha Knowledge Graphs - Developer Guide

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Environment Setup](#environment-setup)
5. [Data Preparation](#data-preparation)
6. [Building the Project](#building-the-project)
7. [Running the Application](#running-the-application)
8. [API Usage](#api-usage)
9. [Development Workflow](#development-workflow)
10. [Troubleshooting](#troubleshooting)
11. [Contributing](#contributing)

## Project Overview

VaidhyaMegha Knowledge Graphs is an open-source project that builds comprehensive knowledge graphs for medical and clinical research. The primary focus is on creating an **Open Knowledge Graph on Clinical Trials** that connects:

- **Clinical Trials** from global registries (WHO ICTRP, ClinicalTrials.gov)
- **Medical Conditions & Interventions** using MeSH vocabulary
- **Research Articles** from PubMed
- **Phenotype-Genotype Associations** from PheGenI
- **Medical Term Co-occurrences** from MEDLINE

### Key Features
- 🔗 **Multi-source Integration**: Combines data from 5+ major medical databases
- 🌐 **Global Coverage**: Includes 750K+ clinical trials worldwide
- 📊 **Rich Relationships**: 5+ million RDF triples connecting medical entities
- 🔍 **Flexible Querying**: SPARQL and GraphQL interfaces
- 🚀 **Scalable Architecture**: Java-based with efficient RDF processing

## Architecture

### Technology Stack
- **Language**: Java 17
- **Build Tool**: Maven 3.x
- **RDF Framework**: Apache Jena 4.3.2
- **Database**: PostgreSQL (AACT)
- **GraphQL**: HyperGraphQL 3.0.1
- **Command Line**: Args4J

### Core Components

```
src/main/java/com/vaidhyamegha/data_cloud/kg/
├── App.java              # Main application entry point
├── Constants.java        # Application constants and URIs
├── EntrezClient.java     # PubMed API client
├── ESearchResult.java    # Entrez API response model
├── MODE.java            # Execution mode enum (BUILD/CLI/SERVER)
└── RESOURCE.java        # RDF resource factory
```

### Data Flow
1. **Data Ingestion**: Load clinical trials from AACT database
2. **Entity Linking**: Map trials to MeSH terms for conditions/interventions
3. **Article Association**: Link trials to PubMed articles via Entrez API
4. **Graph Enhancement**: Add phenotype-genotype and co-occurrence data
5. **RDF Generation**: Export as N-Triples format
6. **Query Interface**: Expose via SPARQL/GraphQL APIs

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Maven**: 3.6+ 
- **Memory**: Minimum 8GB RAM (16GB recommended)
- **Storage**: 10GB+ free space for data files
- **Network**: Internet access for data downloads

### External Dependencies
- PostgreSQL client libraries (included in Maven dependencies)
- Access to AACT database (credentials required)

## Environment Setup

### 1. Clone the Repository
```bash
git clone https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs.git
cd vaidhyamegha-knowledge-graphs
```

### 2. Create Required Directories
```bash
mkdir -p lib
mkdir -p data/open_knowledge_graph_on_clinical_trials
```

### 3. Download External Libraries
```bash
# Download Princeton's algorithms library
curl -o lib/algs4.jar https://algs4.cs.princeton.edu/code/algs4.jar

# Download HyperGraphQL
curl -L -o lib/hypergraphql-3.0.1-exe.jar \
  https://github.com/hypergraphql/hypergraphql/releases/download/3.0.1/hypergraphql-3.0.1-exe.jar
```

### 4. Configure Database Connection
Create `src/main/resources/config.properties` (see sample configuration):
```properties
# AACT Database Configuration
aact_url=jdbc:postgresql://aact-db.ctti-clinicaltrials.org:5432/aact
user=your_username
password=your_password

# API Configuration
ENTREZ_API_CALL_THRESHOLD=0.1

# SQL Queries
aact_browse_conditions=SELECT nct_id, mesh_term FROM browse_conditions WHERE mesh_term IS NOT NULL
aact_browse_interventions=SELECT nct_id, mesh_term FROM browse_interventions WHERE mesh_term IS NOT NULL
insert_trial_articles=INSERT INTO trial_articles (trial_id, article_ids) VALUES (?, ?)
```

## Data Preparation

### Required Data Files

Download the following files to `data/open_knowledge_graph_on_clinical_trials/`:

#### 1. MeSH Vocabulary (Required)
```bash
curl -o data/open_knowledge_graph_on_clinical_trials/vocabulary_1.0.0.ttl \
  https://nlmpubs.nlm.nih.gov/projects/mesh/rdf/2022/vocabulary_1.0.0.ttl
```

#### 2. MeSH RDF Data (Required)
```bash
curl -o data/open_knowledge_graph_on_clinical_trials/mesh2022.nt.gz \
  https://nlmpubs.nlm.nih.gov/projects/mesh/rdf/2022/mesh2022.nt.gz
gunzip data/open_knowledge_graph_on_clinical_trials/mesh2022.nt.gz
```

#### 3. PheGenI Data (Required)
```bash
curl -o data/open_knowledge_graph_on_clinical_trials/PheGenI_Association_full.tab \
  "https://www.ncbi.nlm.nih.gov/projects/gap/eqtl/EpiViewBE.cgi?type=dl.tab"
```

#### 4. MEDLINE Co-occurrence Data (Required)
```bash
curl -o data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt.gz \
  https://data.lhncbc.nlm.nih.gov/public/ii/information/MRCOC/detailed_CoOccurs_2021.txt.gz
gunzip data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt.gz

# Process the data
cut -d '|' -f1,9,15 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt > \
  data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt

sort -u data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt > \
  data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt
```

### Data Size Expectations
- **MeSH Vocabulary**: ~50MB
- **MeSH RDF**: ~2GB
- **PheGenI**: ~100MB
- **MRCOC**: ~5GB (compressed), ~15GB (uncompressed)
- **Final RDF Output**: ~2GB (5M+ triples)

## Building the Project

### Compile and Package
```bash
# Clean and build with dependencies
mvn clean package assembly:single -DskipTests

# Verify the build
ls -la target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar
```

### Build Verification
```bash
# Test the application
java -jar target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar --help
```

## Running the Application

### Mode 1: Build RDF Knowledge Graph
```bash
# Build the complete knowledge graph (requires 4-8GB RAM)
java -jar -Xms4096M -Xmx8192M \
  target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar

# Output: data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_open_kg_clinical_trials.nt
```

### Mode 2: CLI SPARQL Queries
```bash
# Run a sample count query
java -jar -Xms4096M -Xmx8144M \
  target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar \
  -m cli -q src/main/sparql/1_count_of_records.rq

# Expected output: 5523173 triples
```

### Mode 3: GraphQL Server
```bash
# Start the GraphQL server
java -cp "target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar:lib/*" \
  com.vaidhyamegha.data_cloud.kg.App -m server

# Server will start on http://localhost:8080/graphql
```

## API Usage

### SPARQL Queries

Create custom SPARQL queries in `src/main/sparql/`:

```sparql
# Example: Find trials for a specific condition
SELECT ?trial ?condition WHERE {
  ?trial <https://vaidhyamegha.com/open_kg/Condition> ?condition .
  ?condition rdfs:label "Diabetes Mellitus" .
}
LIMIT 10
```

### GraphQL Queries

#### Sample Query (JSON Response)
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "query": "{ trial_GET(limit: 10) { label } }"
  }'
```

#### Sample Query (N-Triples Response)
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Accept: application/ntriples" \
  -d '{
    "query": "{ trial_GET(limit: 30, offset: 1) { label } }"
  }'
```

### GraphQL Schema
The schema is defined in `src/main/resources/schema.graphql`:
```graphql
type trial {
  label: String
  condition: [condition]
  intervention: [intervention]
  article: [article]
}

type Query {
  trial_GET(limit: Int, offset: Int): [trial]
}
```

## Development Workflow

### Project Structure
```
vaidhyamegha-knowledge-graphs/
├── src/
│   ├── main/
│   │   ├── java/           # Java source code
│   │   ├── resources/      # Configuration files
│   │   ├── sparql/         # SPARQL query templates
│   │   └── sql/           # SQL scripts
│   └── test/              # Unit tests
├── data/                  # Data files (gitignored)
├── docs/                  # Documentation
├── examples/              # Example projects
├── lib/                   # External JAR files
└── assembly/              # Maven assembly configuration
```

### Adding New Features

#### 1. Adding New Data Sources
1. Create a new method in `App.java` (e.g., `addNewDataSource()`)
2. Define RDF properties in `Constants.java`
3. Add database queries to `config.properties`
4. Update the main processing pipeline in `doMain()`

#### 2. Adding New Query Types
1. Create SPARQL templates in `src/main/sparql/`
2. Add command-line options in `App.java`
3. Update the CLI mode handler

#### 3. Extending GraphQL Schema
1. Modify `src/main/resources/schema.graphql`
2. Update HyperGraphQL configuration in `hql-config.json`

### Testing

#### Unit Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppTest
```

#### Integration Testing
```bash
# Test with sample data
java -jar target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar \
  -m cli -q src/main/sparql/1_count_of_records.rq
```

### Performance Optimization

#### Memory Management
- **Minimum**: 4GB heap (`-Xms4096M`)
- **Recommended**: 8GB heap (`-Xmx8192M`)
- **Large datasets**: 16GB+ system RAM

#### Database Optimization
- Use connection pooling for multiple queries
- Implement batch processing for large datasets
- Consider database indexing for frequently queried fields

## Troubleshooting

### Common Issues

#### 1. OutOfMemoryError
```bash
# Increase heap size
java -jar -Xms8192M -Xmx16384M target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar
```

#### 2. Database Connection Issues
- Verify AACT credentials in `config.properties`
- Check network connectivity to `aact-db.ctti-clinicaltrials.org:5432`
- Ensure PostgreSQL JDBC driver is in classpath

#### 3. Missing Data Files
```bash
# Verify all required files exist
ls -la data/open_knowledge_graph_on_clinical_trials/
# Should contain: vocabulary_1.0.0.ttl, mesh2022.nt, PheGenI_Association_full.tab, detailed_CoOccurs_2021_selected_fields_sorted.txt
```

#### 4. GraphQL Server Issues
- Check if port 8080 is available
- Verify HyperGraphQL JAR is in `lib/` directory
- Review `hql-config.json` configuration

### Debug Mode
```bash
# Enable debug logging
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
  -jar target/vaidhyamegha-knowledge-graphs-v0.9-jar-with-dependencies.jar
```

### Log Analysis
- Application logs are output to console
- Database connection errors appear in stack traces
- RDF parsing errors indicate malformed data files

## Contributing

### Development Environment
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Follow Java coding standards
4. Add unit tests for new functionality
5. Update documentation as needed

### Code Style
- Use Java 17 features appropriately
- Follow existing naming conventions
- Add JavaDoc comments for public methods
- Maintain consistent indentation (4 spaces)

### Pull Request Process
1. Ensure all tests pass: `mvn test`
2. Update DEVELOPER_GUIDE.md if adding new features
3. Provide clear commit messages
4. Reference relevant issues in PR description

### Reporting Issues
- Use GitHub Issues for bug reports
- Include system information (Java version, OS, memory)
- Provide steps to reproduce
- Attach relevant log output

## Additional Resources

### Documentation
- [Main README](README.md) - Project overview and quick start
- [Academic Paper](docs/open_knowledge_graph_on_clinical_trials/out.pdf) - Detailed methodology
- [Examples](examples/) - Sample implementations

### External Resources
- [Apache Jena Documentation](https://jena.apache.org/documentation/)
- [HyperGraphQL Documentation](https://www.hypergraphql.org/documentation/)
- [AACT Database Documentation](https://aact.ctti-clinicaltrials.org/)
- [MeSH RDF Documentation](https://hhs.github.io/meshrdf/)

### Community
- [VaidhyaMegha GitHub](https://github.com/VaidhyaMegha)
- [Project Issues](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs/issues)

---

**Last Updated**: July 2024  
**Version**: v0.9  
**Maintainer**: VaidhyaMegha Team
