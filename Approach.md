# Knowledge Graphs

- What are [knowledge graphs](https://arxiv.org/pdf/2003.02320.pdf)

## Open KGs

## Enterprise KGs

# Projects

## Health

### Open Knowledge Graph on Clinical trials
- Input : Mesh RDF
- Output : Clinical Trials RDF with below constituent ids and their relationships
  - MeSH, Clinical Trial, PubMed Article, Symptom/Phenotype, Genotype(from Human Genome), Disease/ICD10
  - Additionally, clinical trial -> clinical trial links are also 'discovered' and added.
  
#### API : retrieve links given one or more of the ids as input
- Input :  one or more of these ids as input.   
- Output : { input : { id_type : xxx, key : key1, value : value1 }, output : \[ { id_type : xxx, key : key1, value : value1 } \] }
- ACMG

#### Notes

- [Human symptoms–disease network](https://www.nature.com/articles/ncomms5212#MOESM1042)
- HSDN supplementary data files "Combined-Input.tsv", "Symptom-Occurence-Input.tsv", and "Disease-Occurence-Input.tsv" were taken as input and new output  [files](https://github.com/LeoBman/HSDN) which also have the MeSH IDs were created.
- [Analysing](https://github.com/dhimmel/hsdn) bipartite symptoms to diseases network : Instead of the supplementary data files from HSDN, files retrieved from above [LeoBman/HSDN](https://github.com/LeoBman/HSDN) were used. MeSH diseases where then mapped to the Disease Ontologies diseases used in [Hetionet v1.0](https://github.com/hetio/hetionet). Ultimately no data from HSDN was used in Hetionet, instead re-extracted symptom–disease relationships from [MRCOC - MEDLINE topic co-occurrence](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html) were used.
- Comparison of [hetio/medline](https://github.com/hetio/medline) to [MRCOC](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html)

  `MEDLINE produces co-occurrence files under the codename MRCOC. More information is available in the 2016 report Building an Updated MEDLINE Co-Occurrences (MRCOC) File. These files might be a viable alternative to the analyses in this repository for certain applications. However, they don't appear to contain topics for supplemental concept records (for example MeSH term C000591739). Feel free to open an issue with additional insights on or comparisons to MRCOC`
- 
- 

## Commerce

### eCommerce

### Finance

## Manufacturing

# Tools

- Apache Jena 
  - index RDF files
  - [query](https://towardsdatascience.com/extract-and-query-knowledge-graphs-using-apache-jena-sparql-engine-5c66648797a4) RDF files using indexes

