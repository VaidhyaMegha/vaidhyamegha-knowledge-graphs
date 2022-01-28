# Introduction

This repo will host open knowledge graphs from [VaidhyaMegha](https://vaidhyamegha.com).

# Open Knowledge Graph on Clinical Trials

VaidhyaMegha is building an open [knowledge graph](https://arxiv.org/pdf/2003.02320.pdf) on clinical trials, comprising

- Clinical trial ids from across the globe
- MeSH ids for
    - Symptoms/Phenotype
    - Diseases
- PubMed Article ids
- Genotype(from Human Genome),

## Getting Started

- Compile
  `mvn clean package assembly:single`
- Run
  `java -jar -Xms4096M -Xmx8192M target/vaidhyamegha-knowledge-graphs-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Features as on current release - 0.4

- Graph includes trials across the globe from nearly 20 clinical trial registries. Data is sourced from WHO's ICTRP and clinicaltrials.gov
- Links from trial to MeSH vocabulary are added for conditions and interventions employed in the trial.
- Links from trial to PubMed articles are added. PubMed's expert curate this information for each article.


## Release notes 

- v0.4
  - List of trial ids to be incrementally bounced against Entrez API from an AWS server to generate the necessary incremental mappings b/w trials and PubMed articles
  
  ```
  $ grep "Pubmed_Article" data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_open_kg_clinical_trials.nt 
  <https://clinicaltrials.gov/ct2/show/NCT00400075> <Pubmed_Article> "25153486" .
  <https://clinicaltrials.gov/ct2/show/NCT03934957> <Pubmed_Article> "34064657" .
  ```

- v0.3
  - Adding links between trials and interventions in addition to trials and conditions.
  - conditions and interventions are fetched from database (instead of files). Corresponding edges b/w trials and conditions, trials and interventions are added to RDF. For example :

  ```
    <https://clinicaltrials.gov/ct2/show/NCT00093782> <Condition> <http://id.nlm.nih.gov/mesh/2022/T000687> .
    <https://clinicaltrials.gov/ct2/show/NCT00093782> <Intervention> <http://id.nlm.nih.gov/mesh/2022/T538652> .
  ```

  - All global trial's - 756,169 - are added to RDF. For example :

  ```
  <https://clinicaltrials.gov/ct2/show/NCT00172328> <TrialId> "NCT00172328" .
  <https://www.who.int/clinical-trials-registry-platform/CTRI/2021/05/033487> <TrialId> "CTRI/2021/05/033487" .
  ```

  - Starting with a fresh model for final RDF. MeSH ids that are not linked to any trial not considered. This reduces the graph size considerably.
  - Trial records are fetched from ICTRP's weekly + periodic full export and AACT's daily + monthly full snapshot. 
  - Trials are written down to a file (will be used later) : [vaidhyamegha_clinical_trials.csv](data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_clinical_trials.csv)

  ```
    $ wc -l vaidhyamegha_clinical_trials.csv
    755272 vaidhyamegha_clinical_trials.csv
  ```

  - Download the RDF from [here](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs/releases/tag/v0.3).
- v0.2
  - Clinical trials are linked to the RDF nodes corresponding to the MeSH terms for conditions. For example : 
  - Download the enhanced RDF from [here](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs/releases/tag/v0.2).

## Next steps 

- Full list of trial ids to be used in combination with id_information table to generate a final list of unique trials using WQUPC algorithm
- Add secondary trial ids to graph (this may increase graph size considerably). However, it could be of utility.

## Specification

Below is a very brief specification

- Inputs
    - Mesh RDF
    - WHO's clinical trials database - [ICTRP](https://www.who.int/clinical-trials-registry-platform).
    - US clinical trial [registry ](https://clinicaltrials.gov)data from CTTI's [AACT](https://aact.ctti-clinicaltrials.org/download) database.
    - Data from clinical trial registries across the globe scraped from their websites' ex: [India](http://ctri.nic.in/Clinicaltrials/login.php)
    - MEDLINE Co-Occurrences [(MRCOC)](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html) Files
- Outputs
    - Clinical Trials RDF with below constituent ids and their relationships
        - MeSH, Clinical Trial, PubMed Article, Symptom/Phenotype, Genotype(from Human Genome)
        - Additionally, clinical trial -> clinical trial links across trial registries will also be discovered and added.

## Source code

- Source code would be hosted [here](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs).

## Reference

VaidhyaMegha's [prior work](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs/tree/main/examples) on

- clinical trial registries data [linking](https://ct.biosdlc.com/).
- symptoms to diseases linking.
- phenotype to genotype linking.
- trials to research articles linking.

Last 3 are covered in the ["examples"](examples) folder [here](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs). They were covered in separate public repos [here](https://github.com/VaidhyaMegha/) earlier.

## Documentation

More notes is available [here](docs/open_knowledge_graph_on_clinical_trials/README.md)
