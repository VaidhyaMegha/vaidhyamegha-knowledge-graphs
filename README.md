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

## Release notes 
- v0.3
  - conditions and interventions are fetched from database (instead of files). Corresponding edges b/w trials and conditions, trials and interventions are added to RDF.
- v0.2 : Clinical trials are linked to the RDF nodes corresponding to the MeSH terms for conditions. 
- Download the enhanced RDF from [here](https://github.com/VaidhyaMegha/vaidhyamegha-knowledge-graphs/releases/tag/v0.2).

## Next steps 

- Full list of trial ids to be generated from ICTRP's weekly + Full export and AACT's weekly full snapshot
- List of trial ids to be incrementally bounced against Entrez API from an AWS server to generate the necessary incremental mappings b/w trials and PubMed articles
- Full list of trial ids to be used in combination with id_information table to generate a final list of unique trials using WQUPC algorithm
- 

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