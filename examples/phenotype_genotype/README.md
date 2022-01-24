# Phenotype to Genotype
- In this example, let’s learn about the association of symptoms and diseases, and Phenotype-Genotype.
- **NOTE**: First read through [Symptoms and Diseases](../symptoms_diseases/README.md) example as this example depends on it.
- Once the association between diseases and symptoms are identified, we can find the phenotype and genotype information based on symptoms. Let’s take a look at “Phenotype-Genotype” integrator.
- **Phenotype** is the composite of the organism’s observable characteristics.
- **Genotype** is the part of the genetic makeup of a cell which determines one of its characteristics.

# Phegeni

- Phegeni is a web interface that integrates various genomic databases with genome wide association study (GWAS).
- The genomic databases are from National Center for Biotechnology Information (NCBI) and the association data from National Human Genome Research Institute (NHGRI). Here, the phenotype terms are MESH terms .
- The GWAS is a study of a genome-wide set of genetic variants in different individuals to observe if any variant is associated with phenotype/trait.
- Clinicians and epidemiologists are interested with the results of GWAS because it helps to study design considerations and generation of biological hypotheses.
- GWAS consists of various results that is SNP rs,Gene ,Gene ID,Gene2,Gene ID2,Chromosome and Pubmed ids.
- Phegeni Association results can be accessed from here - https://www.ncbi.nlm.nih.gov/gap/phegeni
- [This](./bs1.pl) program gives the list of SNP rs,Gene ,Gene ID,Gene2,Gene ID2,Chromosome and Pubmed ids of respective phenotype term.
- **Input** file contains a list of phenotype search terms based on MESH and the sample file looks as below.
- **Output** file: Output file contains list of SNP rs,Gene ,Gene ID,Gene2,Gene ID2,Chromosome and Pubmed ids of respective phenotype term.
- In this way,we can retrieve genetic variants related to any Phenotype(s).
