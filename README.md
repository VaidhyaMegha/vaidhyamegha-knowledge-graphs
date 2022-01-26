# Introduction

This repo will host open knowledge graphs from [VaidhyaMegha](https://vaidhyamegha.com).

# Open Knowledge Graph on Clinical Trials

VaidhyaMegha is planning to build an open knowledge graph on clinical trials, comprising

- Clinical trial ids from across the globe
- MeSH ids for
    - Symptoms/Phenotype
    - Diseases
- PubMed Article ids
- Genotype(from Human Genome),

## Specification

Below is a very brief specification

- Inputs
    - Mesh RDF
    - WHO's clinical trials database - [ICTRP](https://www.who.int/clinical-trials-registry-platform).
    - US clinical trial [registry ](https://clinicaltrials.gov)data from CTTI's [AACT](https://aact.ctti-clinicaltrials.org/download) database.
    - Data from clinical trial registries across the globe scraped from their websites' ex: [India](http://ctri.nic.in/Clinicaltrials/login.php)
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

Last 3 are covered in the ['examples'](examples) folder. They were covered in prior work in separate public repos.
