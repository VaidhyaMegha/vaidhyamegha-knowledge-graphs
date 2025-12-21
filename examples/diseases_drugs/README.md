# Diseases to Drugs/Interventions

## Install Dependencies

```bash
sudo apt-get install -y libarray-utils-perl libwww-perl
```

## Importance of Disease-Drug Relationships

- **Disease** is an abnormal condition that negatively affects the functionality of an organism. Understanding which drugs are used to treat specific diseases is crucial for medical research and clinical practice.
- **Drug/Intervention** refers to therapeutic agents or treatments used to manage, treat, or prevent diseases.
- Each **article** in PubMed is associated with metadata that includes major topics (MeSH terms). By using a Perl script with the NCBI E-utilities, we can retrieve PubMed identifiers for any disease or drug term. The disease and drug terms are defined by MeSH. We can find an association between diseases and drugs by using the PubMed IDs.
- This [program](./diseases_drugs.pl) gives the PubMed identifiers with co-occurrence of diseases and drugs, along with association counts in TSV format.

## Files

- Input file for Diseases: [diseases.txt](../../data/diseases_drugs/diseases.txt)
- Input file for Drugs: [drugs.txt](../../data/diseases_drugs/drugs.txt)
- Output file: [diseases_drugs_cooccurrence.tsv](../../data/diseases_drugs/diseases_drugs_cooccurrence.tsv) contains disease-drug associations with co-occurrence counts in TSV format.

## Output Format (TSV)

| Column | Description |
|--------|-------------|
| Disease | Disease name (MeSH term) |
| Drug | Drug/intervention name (MeSH term) |
| Cooccurrence_Count | Number of PubMed articles mentioning both |
| Sample_PMIDs | Comma-separated list of sample PubMed IDs (first 10) |

## Usage

```bash
# Navigate to project root
cd /path/to/vaidhyamegha-knowledge-graphs

# Run the script
perl examples/diseases_drugs/diseases_drugs.pl
```

## How It Works

This script follows the same workflow as [symptoms_diseases.pl](../symptoms_diseases/symptoms_diseases.pl):

1. **Phase 1**: Read disease terms and query PubMed E-utilities to get PubMed IDs for each disease
2. **Phase 2**: Read drug terms and query PubMed E-utilities to get PubMed IDs for each drug
3. **Phase 3**: Load PubMed IDs from intermediate files into memory (Array of Arrays)
4. **Phase 4**: For each disease-drug pair, find the intersection of PubMed IDs and write to TSV output

## API Details

- **Base URL**: `https://eutils.ncbi.nlm.nih.gov/entrez/eutils/`
- **Endpoint**: `esearch.fcgi`
- **Database**: `pubmed`
- **Search Qualifier**: `[mesh]` for MeSH-indexed terms

Example API call:
```
https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=Diabetes%20Mellitus[mesh]&retmax=5000
```
