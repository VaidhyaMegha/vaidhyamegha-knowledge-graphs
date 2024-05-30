# Documentation

- Look at [article](out.pdf) for notes and references

## Steps to generate [article](out.pdf)

```
pwd # make sure that you are at equivalent path
/projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/docs/open_knowledge_graph_on_clinical_trials

rm -f out.*

docker run --rm -v "$(pwd):/data" -u "$(id -u)" pandocscholar/alpine

xdg-open  out.pdf
 
```

# Open Knowledge Graph on Clinical trials

- What are [knowledge graphs](https://arxiv.org/pdf/2003.02320.pdf)

## Specification

Below is a brief specification

- Inputs
  - Mesh RDF
  - WHO's clinical trials database - [ICTRP](https://www.who.int/clinical-trials-registry-platform).
  - US clinical trial [registry ](https://clinicaltrials.gov)data from CTTI's [AACT](https://aact.ctti-clinicaltrials.org/download) database.
- Outputs
  - Clinical Trials RDF with below constituent ids and their relationships
    - MeSH, Clinical Trial, PubMed Article, Symptom/Phenotype, Genotype(from Human Genome)
    - Additionally, clinical trial -> clinical trial links across trial registries will also be discovered and added.
  
## On-demand access : API : retrieve links given one or more of the ids as input

- Input :  one or more of these ids as input.   
- Output : { input : { id_type : xxx, key : key1, value : value1 }, output : \[ { id_type : xxx, key : key1, value : value1 } \] }
- ACMG

## Notes

- [Human symptoms–disease network](https://www.nature.com/articles/ncomms5212#MOESM1042)
- HSDN supplementary data files "Combined-Input.tsv", "Symptom-Occurence-Input.tsv", and "Disease-Occurence-Input.tsv" were taken as input and new output  [files](https://github.com/LeoBman/HSDN) which also have the MeSH IDs were created.
- [Analysing](https://github.com/dhimmel/hsdn) bipartite symptoms to diseases network : Instead of the supplementary data files from HSDN, files retrieved from above [LeoBman/HSDN](https://github.com/LeoBman/HSDN) were used. MeSH diseases where then mapped to the Disease Ontologies diseases used in [Hetionet v1.0](https://github.com/hetio/hetionet). Ultimately no data from HSDN was used in Hetionet, instead re-extracted symptom–disease relationships from [MRCOC - MEDLINE topic co-occurrence](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html) were used.
- Comparison of [hetio/medline](https://github.com/hetio/medline) to [MRCOC](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html)

  `MEDLINE produces co-occurrence files under the codename MRCOC. More information is available in the 2016 report Building an Updated MEDLINE Co-Occurrences (MRCOC) File. These files might be a viable alternative to the analyses in this repository for certain applications. However, they don't appear to contain topics for supplemental concept records (for example MeSH term C000591739). Feel free to open an issue with additional insights on or comparisons to MRCOC`

# References
- Look at References section in [Article.md](Article.md). Additional references are below.

## Knowledge graph

- Introduction to [knowledge graphs](https://arxiv.org/pdf/2003.02320.pdf)

## MeSH
- [Tree](https://meshb.nlm.nih.gov/treeView) view
- [Record](https://meshb.nlm.nih.gov/record/ui?ui=D019588) view along with 'Mesh Tree Structures'

## PheGenI
- What [is](https://www.genome.gov/27543987/2011-news-feature-new-web-portal-expands-view-of-genetic-association-data-for-researchers) PheGenI
- PheGenI: The Phenotype-Genotype Integrator [demo](https://www.youtube.com/watch?v=v_yEy--HcKc)
- Downstream analysis of PheGenI results [demo](https://www.youtube.com/watch?v=Tf9aNkKDF3o)

## Apache Jena
- [Download](https://jena.apache.org/download/index.cgi)
- [Read](https://jena.apache.org/documentation/io/rdf-input.html) RDF files
- [Write](https://jena.apache.org/documentation/io/rdf-output.html) RDF files
- [Querying](https://jena.apache.org/tutorials/rdf_api.html#ch-Querying-a-Model) a model
- [SparQL](https://jena.apache.org/tutorials/sparql_query1.html)
- index RDF files
- [query](https://towardsdatascience.com/extract-and-query-knowledge-graphs-using-apache-jena-sparql-engine-5c66648797a4) RDF files using indexes
- count triples [query](https://stackoverflow.com/a/51289880/294552)
- Execute SparQL query [programmatically](https://github.com/apache/jena/blob/main/jena-examples/src/main/java/arq/examples/ExProg1.java)

## GraphQL
- [Bridges between GraphQL and RDF](https://www.w3.org/Data/events/data-ws-2019/assets/position/Ruben%20Taelman.pdf)

## Java
- Configure Java [heap](https://stackoverflow.com/a/47388044/294552) size and if needed [stack](https://stackoverflow.com/a/44253141/294552) size.
- Invoke Entrez API using Spring [webclient](https://www.baeldung.com/spring-webclient-resttemplate)
  - Handling [XML response](https://stackoverflow.com/questions/68209076/spring-resttemplate-works-for-string-but-not-for-my-class)
- JAXB [impl](https://stackoverflow.com/a/61283181/294552) in Java 11
- JAXB [marshalling objects in lists](https://stackoverflow.com/a/3683678/294552)
- Java [multiple resources with autocloseable try-with-resources](https://stackoverflow.com/a/30553153/294552)
- [escape pipe character in grep](https://stackoverflow.com/a/23772497/294552)
- Other older relevant links on Java and Entrez
  - [download-pubmed-abstracts-in-java](https://stackoverflow.com/questions/5410151/download-pubmed-abstracts-in-java)
  - Github repo : [pubmed_ws_client](https://github.com/renaud/pubmed_ws_client)
- Read file into a [string](https://howtodoinjava.com/java/io/java-read-file-to-string-examples/)

## Linux commands
- Check if string exists in file in [bash](https://stackoverflow.com/a/4749368/294552)
- Prevent grep from exiting when match is [not](https://unix.stackexchange.com/a/330662/47615) found

## PostgreSQL
- PostgreSQL [array](https://www.postgresql.org/docs/9.1/arrays.html) columns
  - JDBC [insert](https://tonaconsulting.wordpress.com/2013/05/28/postgres-and-multi-dimensions-arrays-in-jdbc/) into array columns
- PostgreSQL date column with [default](https://stackoverflow.com/a/910937/294552) value
- PostgreSQL [upsert](https://www.postgresqltutorial.com/postgresql-upsert/) statement
- PostgreSQL - pg_restore - restore only one selected [schema](https://stackoverflow.com/a/970491/294552)
- PostgreSQL - [Array functions](https://www.postgresql.org/docs/8.4/functions-array.html)
- Execute query on PostgreSQL using psql [non-interactively](https://stackoverflow.com/a/6405296/294552)
- Save psql inline query output to a [file](https://stackoverflow.com/a/11870348/294552)
- In PostgreSQL formulate a query to get [all items](https://stackoverflow.com/a/34592639/294552) in an array column

## Postman
- [Tutorial](https://learning.postman.com/docs/sending-requests/supported-api-frameworks/graphql/) for using GraphQL with Postman

## Neo4j
- Supports [GraphQL](https://neo4j.com/developer/graphql/) API.
- [Aura DB](https://neo4j.com/cloud/aura) is Neo4j's cloud database service. 

### Possibly older threads
- GraphQL [queries](https://community.neo4j.com/t/grandstack-starter-using-postman-api-to-get-data-from-graphql/999/2) from Postman to neo4j.
- Discussion on SparQL for [neo4j](https://community.neo4j.com/t/sparql-for-neo4j/19583/5). 
  - Suggests it's feasible to execute SparQL 'get' query from Jena to Neo4j using n10s plugin. 
  
## Superset

### Integration to data sources
- GraphQL's [integration](https://github.com/apache/superset/issues/5389#issuecomment-510284311) in to Apache Superset.
  - Subsequent [fork](https://github.com/graphadvantage/incubator-superset-gql-neo4j) with commits from the above commenter - graphadvantage - to address this need.

### Superset's API
- REST [API](https://preset.io/blog/2020-10-01-superset-api/) of Superset with a comment on GraphQL.


