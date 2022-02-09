
# Code Analysis :

**App.java :**

Contains the main function.

The main function take input arguments. After parsing arguments, based on arguments mode will be selected.

Irrespective of mode, intialise the setup.

### Intialize:

Model model = initialize(args, parser);

- Parse input arguments. Print arguments and get mode.
- read Configuration properties
- Intialise RDF model  

There are three types of modes :

  - BUILD
  - CLI
  - SERVER
  
And Default mode is BUILD .

## BUILD :
Generate knowledge graph file  filevaidhyamegha_open_kg_clinical_trials.nt

- Create vocab model object and read MeSH vocabulary file which is in turtle format
- Create meshModel model object and read MeSH RDF file which is in nt format
- Add **trial_id** resoucres to model
- Add trial interventios to model, by extracting interventions from meshModel
- Add Mesh CoOccurences to model, by extracting CoOccurences from meshModel
- Add Pheno and Geno Types to model, by extracting Pheno and Geno Types from meshModel
- Write model to filevaidhyamegha_open_kg_clinical_trials.nt.

## SERVER :

  Build the application in server and wait for requests  serve

## CLI  :
Take input either a trial id or Pubmed Article id or Symptom (MeSH) or Disease (MeSH) and output the result.

- Read the query
- Read model output file ie., vaidhyamegha_open_kg_clinical_trials.nt
- And Execute the query over model 
- Print the results.


