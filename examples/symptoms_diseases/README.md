# Symptoms to Diseases

## Importance of symptom and disease relationship

- **Disease** is an abnormal condition that negatively affects the functionality of an organism. Symptom is a physical or mental feature which can indicate a condition. The relation between the diseases and their symptoms are important to diagnose any disease. This information is also useful for medical research purposes.
- Each **article** in the PubMed is associated to metadata that includes major topics of the article. By using a perl script with the NCBI E-utilities, we can retrieve PubMed identifiers of any symptom and disease terms.The symptom and disease terms are defined by MeSH. We can find an association between symptoms and diseases by using the PubMed ids.
- This [program](./symptoms_diseases.pl) gives the pubmed identifiers with co-occurrence of symptoms and diseases.
- Input file for Diseases : ![s100.txt](../../data/symptoms_diseases/s100.txt)
- Input file for Symptoms : [d100.txt](../../data/symptoms_diseases/d100.txt)
- Output file : [final.txt](../../data/symptoms_diseases/final.txt) contains list of pubmed identifiers of co-occurrence of diseases and symptoms.
