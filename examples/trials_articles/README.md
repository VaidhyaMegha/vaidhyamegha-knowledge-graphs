#  Clinical trials to research articles

- We can search PubMed database by specifying clinical trial id(s) and retrieve all the relevant journal articles. The NLM (The world's largest medical library, the U.S. National Library of Medicine is part of the National Institutes of Health) extracts  trail ids from an article and places them into the article's metadata in secondary id field.
- How to query single trial id manually?? Example: To search for journal articles related to a clinical trial id say NCT00000419, use PubMed’s application protocol interface (API) called e-Utils, which can be accessed through URL https://www.ncbi.nlm.nih.gov/books/NBK25497/ Now, specify clinical trial id in the eutils api as shown below:
 `https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=NCT01874691[si]`
- All the journal articles related to the clinical trial id will be displayed like below
![Resuts](../../docs/open_knowledge_graph_on_clinical_trials/images/trials_articles/Trials-Articles.png)
- In the above url, query string [SI] refers to Secondary ID which can be used to search for articles. This field contains accession numbers of various databases (molecular sequence data, gene expression or chemical compounds etc.)
- How to query multiple trial id’s ? Here,we have taken a large number of clinical trial numbers in one file and the results were taken into another file which contains pubmed articles of respective trials.
- Input [file](../../data/trials_articles/NCTNumbers.txt) contains multiple NCT numbers which can be used as a query in PubMed API(e-utility) search field.
- Perl [script](./trials_articles.pl) will search for each clinical trial id specified in the input file against PubMed database using PubMed(API) E-Utils.
- Output [file](../../data/trials_articles/results.txt) contains PMIDs (pubmed records) of respective clinical trials.

