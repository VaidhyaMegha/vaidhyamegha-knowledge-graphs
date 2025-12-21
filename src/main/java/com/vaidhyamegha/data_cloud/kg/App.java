package com.vaidhyamegha.data_cloud.kg;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.sql.ResultSet;
import java.util.*;

import static com.vaidhyamegha.data_cloud.kg.Constants.*;
import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

/**
 * Accept either a trial id or Pubmed Article id or Symptom (MeSH) or Disease (MeSH).
 * Find matches to the other 3
 */
public class App {

    @Option(name = "-o", aliases = "--output-rdf", usage = "Path to the final RDF file", required = false)
    private String output = "data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_open_kg_clinical_trials.nt";

    @Option(name = "-l", aliases = "--output-trials-list", usage = "Path to the list of trial ids file", required = false)
    private File trials = new File("data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_clinical_trials.csv");

    @Option(name = "-g", aliases = "--phegeni", usage = "Path to phegeni file", required = false)
    private File phegeni = new File("data/open_knowledge_graph_on_clinical_trials/PheGenI_Association_full.tab");

    @Option(name = "-v", aliases = "--mesh-vocab-rdf", usage = "Path to the downloaded MeSH Vocabulary Turtle file.", required = false)
    private String meshVocab = "data/open_knowledge_graph_on_clinical_trials/vocabulary_1.0.0.ttl";

    @Option(name = "-co", aliases = "--mrcoc-sorted-file", usage = "Path to sorted MRCOC detailed co occurrence file for selected fields.", required = false)
    private String mrcoc = "data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt";

    @Option(name = "-me", aliases = "--mesh-rdf", usage = "Path to the downloaded MeSH RDF file.", required = false)
    private String meshRDF = "data/open_knowledge_graph_on_clinical_trials/mesh2022.nt";

    @Option(name = "-h", aliases = "--hql-config-file", usage = "Path to the HyperGraphQL config file path.", required = false)
    public static String hqlConfig = "src/main/resources/hql-config.json";

    @Option(name = "-m", aliases = "--mode", usage = "Build RDF or query pre-built RDF?", required = false)
    private MODE mode = MODE.BUILD;

    @Option(name = "-q", aliases = "--query", usage = "Query file", required = false)
    private String query = "src/main/sparql/1_count_of_records.rq";

    @Option(name = "-t", aliases = "--trial-id", usage = "Clinical trial's registered id.", required = false)
    private String trial;

    @Option(name = "-p", aliases = "--article-id", usage = "PubMed id for the article.", required = false)
    private String article;

    @Option(name = "-s", aliases = "--symptom-id", usage = "MeSH id for a symptom.", required = false)
    private String symptom;

    @Option(name = "-d", aliases = "--disease-id", usage = "MeSH id for a disease.", required = false)
    private String disease;

    // Samyama client for connecting to Samyama Graph Database
    private SamyamaClient samyamaClient = null;

    private Properties prop = null;

    public static void main(String[] args) throws IOException {
        new App().doMain(args);
    }

    public void doMain(String[] args)  {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            Model model = initialize(args, parser);

            if (mode == MODE.BUILD) {
                // Initialize Samyama client using config properties
                String host = prop.getProperty("samyama_host", "localhost");
                int port = Integer.parseInt(prop.getProperty("samyama_port", "6379"));
                String graphName = prop.getProperty("samyama_graph_name", "knowledge_graph");

                samyamaClient = new SamyamaClient(host, port, graphName);

                // Test connection to Samyama
                if (!samyamaClient.testConnection()) {
                    throw new RuntimeException("Cannot connect to Samyama at " + host + ":" + port);
                }
                System.out.println("Connected to Samyama Graph Database");

                // Load MeSH vocabulary for term lookups (still using Jena for reading RDF files)
                Model meshModel = ModelFactory.createDefaultModel();
                meshModel.read(meshRDF, "NT");

                // Build knowledge graph in Samyama
                addAllTrialsToSamyama();
                addTrialArticlesToSamyama();  // Fetch PubMed articles via Entrez API
                addTrialConditionsToSamyama(meshModel);
                addTrialInterventionsToSamyama(meshModel);
                // Note: addMeSHCoOccurrencesToSamyama requires MRCOC file (optional)
                // addMeSHCoOccurrencesToSamyama(meshModel);
                addPhenotypeGenotypesToSamyama(meshModel);

                System.out.println("Knowledge graph built successfully in Samyama");
                samyamaClient.close();
            } else if(mode == MODE.CLI) {
                // Initialize Samyama client
                String host = prop.getProperty("samyama_host", "localhost");
                int port = Integer.parseInt(prop.getProperty("samyama_port", "6379"));
                String graphName = prop.getProperty("samyama_graph_name", "knowledge_graph");

                samyamaClient = new SamyamaClient(host, port, graphName);

                // Check if query is a file path or direct query string
                String cypherQuery;
                File queryFile = new File(query);
                if (queryFile.exists()) {
                    // Read query from file
                    cypherQuery = Files.readString(Path.of(query));
                    System.out.println("Loaded query from file: " + query);
                } else {
                    // Use directly as query string
                    cypherQuery = query;
                }

                System.out.println("Executing Cypher query: " + cypherQuery);
                System.out.println("Results: ");
                System.out.println("-------- ");

                // Execute query and print results
                Object result = samyamaClient.executeQuery(cypherQuery);
                System.out.println(result);

                samyamaClient.close();
            }  else if(mode == MODE.SERVER) {
                // Samyama already runs as its own server on port 6379
                // Just print instructions for connecting
                String host = prop.getProperty("samyama_host", "localhost");
                int port = Integer.parseInt(prop.getProperty("samyama_port", "6379"));
                String graphName = prop.getProperty("samyama_graph_name", "knowledge_graph");

                System.out.println("=========================================");
                System.out.println("Samyama Graph Database Server");
                System.out.println("=========================================");
                System.out.println("Samyama should be running at: " + host + ":" + port);
                System.out.println("Graph name: " + graphName);
                System.out.println("");
                System.out.println("To connect, use redis-cli:");
                System.out.println("  redis-cli -h " + host + " -p " + port);
                System.out.println("");
                System.out.println("Example queries:");
                System.out.println("  GRAPH.QUERY " + graphName + " \"MATCH (t:Trial) RETURN t LIMIT 10\"");
                System.out.println("  GRAPH.QUERY " + graphName + " \"MATCH (t:Trial)-[:STUDIES]->(c:Condition) RETURN t, c\"");
                System.out.println("=========================================");
            } else {
                throw new UnsupportedOperationException("Non-build modes are not yet supported");
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java App [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();

            System.err.println("  Example: java App" + parser.printExample(ALL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Model initialize(String[] args, CmdLineParser parser) throws CmdLineException {
        parser.parseArgument(args);

        ClassLoader cl = App.class.getClassLoader();
        prop = readProperties(cl);

        Model model = ModelFactory.createDefaultModel();
        FileManager.getInternal().addLocatorClassLoader(cl);

        System.out.println(Arrays.toString(args));
        System.out.println(mode);
        return model;
    }

    private void addPhenotypeGenotypes(Model model, Model meshModel) {
        String line = "";
        Property pGene = model.createProperty( NAMED_EDGE +"Gene");
        Property pGeneID = model.createProperty( NAMED_EDGE +"GeneID");

        try (BufferedReader br = new BufferedReader(new FileReader(phegeni));) {

            while((line = br.readLine())!= null) {
                String trait = line.split(TAB)[1];
                String geneId1 = line.split(TAB)[5];
                String geneId2 = line.split(TAB)[7];

                StmtIterator si = findStatements(meshModel, trait);

                if (si.hasNext()) {
                    Statement s = si.nextStatement();
                    Resource rId1 = RESOURCE.GENE_ID.createResource(model, geneId1);
                    Resource rId2 = RESOURCE.GENE_ID.createResource(model, geneId2);

                    model.add(s);

                    model.add(rId1, pGeneID, geneId1);
                    model.add(rId1, pGene, s.getSubject());

                    model.add(rId2, pGeneID, geneId2);
                    model.add(rId2, pGene, s.getSubject());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Sorry, couldn't read MeSH co-occurrence links");
        }
    }

    private StmtIterator findStatements(Model meshModel, String literalValue) {
        Literal literal = meshModel.createLiteral(literalValue, "en");
        Selector selector = new SimpleSelector(null, null, literal);
        return meshModel.listStatements(selector);
    }

    private void addMeSHCoOccurrences(Model model, Model meshModel) { //TODO: we will use mesHModel more appropriately soon to pick the RDF node directly from there.
        Property pMeSHDUI = model.createProperty( NAMED_EDGE +"MeSH_DUI");
        String qAllArticles = prop.getProperty("all_articles");
        String line = "";

        try (BufferedReader br = new BufferedReader(new FileReader(mrcoc));
             Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                     prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement sAllArticles = conn.prepareStatement(qAllArticles); ) {

            ResultSet resultSet = sAllArticles.executeQuery();

            while (resultSet.next()) {
                String article = resultSet.getString("article");

                if(line != null && !article.equals(line.split(PIPE)[0]))
                    while((line = br.readLine())!= null) if (article.equals(line.split(PIPE)[0])) break;

                if (line == null) break;

                do {
                    String[] ids = line.split(PIPE);

                    Resource r = RESOURCE.PUBMED_ARTICLE.createResource(model, ids[0]);
                    Resource dui1 = RESOURCE.MESH_DUI.createResource(model, ids[1]);
                    Resource dui2 = RESOURCE.MESH_DUI.createResource(model, ids[2]);

                    model.add(r, pMeSHDUI, dui1);
                    model.add(r, pMeSHDUI, dui2);

                    line = br.readLine();
                } while (line!= null && article.equals(line.split(PIPE)[0]));
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Sorry, unable to connect to database");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Sorry, couldn't read MeSH co-occurrence links");
        }
    }

    private void addAllTrials(Model model) {
        Property pType = model.createProperty( RDF_SYNTAX_NS_TYPE);
        Property pTrialId = model.createProperty( RDF_SCHEMA_LABEL);

        String qTrialIds = prop.getProperty("trial_ids");
        String qTrialArticles = prop.getProperty("select_trial_articles");
        Resource nsTypeResource = RESOURCE.NS_TYPE.createResource(model, "");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(trials));
                Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                     prop.getProperty("user"), prop.getProperty("password"));
                PreparedStatement sTrialIds = conn.prepareStatement(qTrialIds);
                PreparedStatement sTrialArticles = conn.prepareStatement(qTrialArticles); ) {

            ResultSet resultSet = sTrialIds.executeQuery();

            while (resultSet.next()) {
                String trialId = resultSet.getString("trial_id");
                Resource r = RESOURCE.TRIAL.createResource(model, trialId);

                model.add(r, pType, nsTypeResource);
                model.add(r, pTrialId, trialId);

                bw.write(trialId + "\n");

                insertTrialArticles(trialId);
            }

            resultSet = sTrialArticles.executeQuery();

            while (resultSet.next()) {
                String trial = resultSet.getString("trial");
                Array pubmedArticles = resultSet.getArray("pubmed_articles");

                Integer[] articles = (Integer[]) pubmedArticles.getArray();

                addTrialArticles(model, trial, articles);
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Sorry, unable to connect to database");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Sorry, couldn't write to trials file or trials-articles file");
        }
    }

    private void addTrialArticles(Model model, String trial, Integer[] articles) {
        Property pPubMedArticle = model.createProperty( NAMED_EDGE +"Pubmed_Article");
        Property pArticleId = model.createProperty( RDF_SCHEMA_LABEL);

        for (Integer a : articles) {
            Resource rArticle = RESOURCE.PUBMED_ARTICLE.createResource(model,String.valueOf(a));

            model.add(rArticle, pArticleId, String.valueOf(a));

            Resource rTrial = RESOURCE.TRIAL.createResource(model, trial);

            model.add(rTrial, pPubMedArticle, rArticle);
        }
    }

    private void insertTrialArticles(String trialId) {
        // constraining so that only a small number of Entrez API calls are made. TODO : Optimize this by checking if an id is already attempted before.
        if (Math.random() > Double.parseDouble(prop.getProperty("ENTREZ_API_CALL_THRESHOLD"))) {
            List<Integer> articles = EntrezClient.getPubMedIds(trialId).getIdList();

            insertTrialPubMedArticles(trialId, articles);
        }
    }

    private void insertTrialPubMedArticles(String trialId, List<Integer> s) {
        try (Connection c = DriverManager.getConnection(prop.getProperty("aact_url"),
                prop.getProperty("user"), prop.getProperty("password"));) {
            PreparedStatement stmt = c.prepareStatement(prop.getProperty("insert_trial_articles"));

            Array array = c.createArrayOf("integer", s.toArray());

            stmt.setString(1, trialId);
            stmt.setArray(2, array);

            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Sorry, couldn't write to trials file or trials-articles database table");
        }
    }

    private void addTrialConditions(Model model, Model meshModel) {
        String query = prop.getProperty("aact_browse_conditions");
        Property p = model.createProperty( NAMED_EDGE +"Condition");

        addTrialToMeSHLinks(model, meshModel, query, p);
    }

    private void addTrialInterventions(Model model, Model meshModel) {
        String query = prop.getProperty("aact_browse_interventions");
        Property p = model.createProperty( NAMED_EDGE +"Intervention");

        addTrialToMeSHLinks(model, meshModel, query, p);
    }

    private void addTrialToMeSHLinks(Model model, Model meshModel, String query, Property p) {
        try (Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String trialId = resultSet.getString("nct_id");
                String conditionMeSHTerm = resultSet.getString("mesh_term");

                Resource r = RESOURCE.TRIAL.createResource(model, trialId);

                StmtIterator si = findStatements(meshModel, conditionMeSHTerm);

                if (si.hasNext()) {
                    Statement s = si.nextStatement();

                    model.add(s);
                    model.add(r, p, s.getSubject());
                }
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Sorry, unable to connect to database");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Sorry, unable to connect to database");
        }
    }

    private Properties readProperties(ClassLoader cl) {
        // Try config-local.properties first (for local development), fallback to config.properties
        InputStream input = cl.getResourceAsStream("config-local.properties");
        String configFile = "config-local.properties";

        if (input == null) {
            input = cl.getResourceAsStream("config.properties");
            configFile = "config.properties";
        }

        try {
            Properties prop = new Properties();

            if (input == null) throw new RuntimeException("Sorry, unable to find config.properties");

            System.out.println("INFO: Loading configuration from " + configFile);
            prop.load(input);

            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Sorry, unable to find config.properties");
        } finally {
            if (input != null) {
                try { input.close(); } catch (IOException e) { /* ignore */ }
            }
        }
    }

    // ============================================================
    // Samyama Methods - Create nodes and edges in Samyama Graph DB
    // ============================================================

    /**
     * Load all clinical trials from AACT database and create Trial nodes in Samyama.
     */
    private void addAllTrialsToSamyama() {
        String qTrialIds = prop.getProperty("trial_ids");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(trials));
             Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                     prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement sTrialIds = conn.prepareStatement(qTrialIds)) {

            ResultSet resultSet = sTrialIds.executeQuery();
            int count = 0;

            while (resultSet.next()) {
                String trialId = resultSet.getString("trial_id");

                // Create Trial node in Samyama
                String uri = RESOURCE.TRIAL.createURI(trialId);
                Map<String, Object> props = new HashMap<>();
                props.put("id", trialId);
                props.put("uri", uri);

                String cypherQuery = CypherQueryBuilder.createNode("Trial", props);
                samyamaClient.createNode(cypherQuery);

                bw.write(trialId + "\n");
                count++;

                if (count % 10000 == 0) {
                    System.out.println("Created " + count + " Trial nodes...");
                }
            }

            System.out.println("Total Trial nodes created: " + count);

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Unable to connect to database");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't write to trials file");
        }
    }

    /**
     * Fetch PubMed articles linked to trials via Entrez API and create Article nodes + REFERENCED_IN edges.
     * This calls the NCBI Entrez E-utilities API for each trial, with rate limiting.
     */
    private void addTrialArticlesToSamyama() {
        String qTrialIds = prop.getProperty("trial_ids");
        Set<String> createdArticles = new HashSet<>();
        int articleCount = 0;
        int edgeCount = 0;
        int apiCalls = 0;

        System.out.println("Fetching PubMed articles for trials from Entrez API...");
        System.out.println("Note: API rate-limited to ~3 requests/second");

        try (Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement sTrialIds = conn.prepareStatement(qTrialIds)) {

            ResultSet resultSet = sTrialIds.executeQuery();

            while (resultSet.next()) {
                String trialId = resultSet.getString("trial_id");

                try {
                    // Rate limiting: pause 350ms between API calls (~3 requests/sec)
                    if (apiCalls > 0) {
                        Thread.sleep(350);
                    }

                    // Call Entrez API to get PubMed articles linked to this trial
                    ESearchResult result = EntrezClient.getPubMedIds(trialId);
                    apiCalls++;

                    if (result != null && result.getIdList() != null) {
                        for (Integer pmid : result.getIdList()) {
                            String pmidStr = String.valueOf(pmid);

                            // Create Article node if not already created
                            if (!createdArticles.contains(pmidStr)) {
                                Map<String, Object> props = new HashMap<>();
                                props.put("pmid", pmidStr);
                                props.put("uri", RESOURCE.PUBMED_ARTICLE.createURI(pmidStr));

                                String createNodeQuery = CypherQueryBuilder.createNode("Article", props);
                                samyamaClient.createNode(createNodeQuery);
                                createdArticles.add(pmidStr);
                                articleCount++;
                            }

                            // Create REFERENCED_IN edge: Trial -[:REFERENCED_IN]-> Article
                            String createEdgeQuery = CypherQueryBuilder.createTrialReferencedInArticle(trialId, pmidStr);
                            samyamaClient.createEdge(createEdgeQuery);
                            edgeCount++;
                        }
                    }

                    if (apiCalls % 100 == 0) {
                        System.out.println("Processed " + apiCalls + " trials, created " + articleCount + " articles, " + edgeCount + " edges...");
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching articles for trial " + trialId + ": " + e.getMessage());
                    // Continue with next trial
                }
            }

            System.out.println("Total Entrez API calls: " + apiCalls);
            System.out.println("Total Article nodes created: " + articleCount);
            System.out.println("Total REFERENCED_IN edges created: " + edgeCount);

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Unable to connect to database");
        }
    }

    /**
     * Load trial conditions from AACT and create Condition nodes + STUDIES edges in Samyama.
     */
    private void addTrialConditionsToSamyama(Model meshModel) {
        String query = prop.getProperty("aact_browse_conditions");
        addTrialToMeSHLinksToSamyama(meshModel, query, "Condition", "STUDIES");
    }

    /**
     * Load trial interventions from AACT and create Drug nodes + USES edges in Samyama.
     */
    private void addTrialInterventionsToSamyama(Model meshModel) {
        String query = prop.getProperty("aact_browse_interventions");
        addTrialToMeSHLinksToSamyama(meshModel, query, "Drug", "USES");
    }

    /**
     * Helper method to create MeSH-related nodes and edges in Samyama.
     */
    private void addTrialToMeSHLinksToSamyama(Model meshModel, String query, String nodeLabel, String edgeType) {
        try (Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            int nodeCount = 0;
            int edgeCount = 0;
            Set<String> createdNodes = new HashSet<>();  // Track created nodes to avoid duplicates

            while (resultSet.next()) {
                String trialId = resultSet.getString("nct_id");
                String meshTerm = resultSet.getString("mesh_term");

                // Find MeSH DUI from meshModel
                StmtIterator si = findStatements(meshModel, meshTerm);

                if (si.hasNext()) {
                    Statement s = si.nextStatement();
                    String meshUri = s.getSubject().getURI();
                    // Extract DUI from URI (last part)
                    String meshDui = meshUri.substring(meshUri.lastIndexOf("/") + 1);

                    // Create MeSH node if not already created
                    if (!createdNodes.contains(meshDui)) {
                        Map<String, Object> props = new HashMap<>();
                        props.put("mesh_id", meshDui);
                        props.put("name", meshTerm);
                        props.put("uri", RESOURCE.MESH_DUI.createURI(meshDui));

                        String createNodeQuery = CypherQueryBuilder.createNode(nodeLabel, props);
                        samyamaClient.createNode(createNodeQuery);
                        createdNodes.add(meshDui);
                        nodeCount++;
                    }

                    // Create edge: Trial -[STUDIES/USES]-> Condition/Drug
                    String createEdgeQuery = CypherQueryBuilder.createEdge(
                            "Trial", "id", trialId,
                            edgeType,
                            nodeLabel, "mesh_id", meshDui
                    );
                    samyamaClient.createEdge(createEdgeQuery);
                    edgeCount++;

                    if (edgeCount % 10000 == 0) {
                        System.out.println("Created " + edgeCount + " " + edgeType + " edges...");
                    }
                }
            }

            System.out.println("Total " + nodeLabel + " nodes created: " + nodeCount);
            System.out.println("Total " + edgeType + " edges created: " + edgeCount);

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Unable to connect to database");
        }
    }

    /**
     * Load MeSH co-occurrences and create edges in Samyama.
     */
    private void addMeSHCoOccurrencesToSamyama(Model meshModel) {
        String qAllArticles = prop.getProperty("all_articles");
        String line = "";
        int edgeCount = 0;
        Set<String> createdArticles = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(mrcoc));
             Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                     prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement sAllArticles = conn.prepareStatement(qAllArticles)) {

            ResultSet resultSet = sAllArticles.executeQuery();

            while (resultSet.next()) {
                String article = resultSet.getString("article");

                // Create Article node if not exists
                if (!createdArticles.contains(article)) {
                    Map<String, Object> props = new HashMap<>();
                    props.put("pmid", article);
                    props.put("uri", RESOURCE.PUBMED_ARTICLE.createURI(article));

                    String createNodeQuery = CypherQueryBuilder.createNode("Article", props);
                    samyamaClient.createNode(createNodeQuery);
                    createdArticles.add(article);
                }

                if (line != null && !article.equals(line.split(PIPE)[0]))
                    while ((line = br.readLine()) != null) if (article.equals(line.split(PIPE)[0])) break;

                if (line == null) break;

                do {
                    String[] ids = line.split(PIPE);
                    String dui1 = ids[1];
                    String dui2 = ids[2];

                    // Create COOCCURS_WITH edges from Article to MeSH terms
                    String edge1 = CypherQueryBuilder.createEdge(
                            "Article", "pmid", ids[0],
                            "COOCCURS_WITH",
                            "Condition", "mesh_id", dui1
                    );
                    samyamaClient.createEdge(edge1);

                    String edge2 = CypherQueryBuilder.createEdge(
                            "Article", "pmid", ids[0],
                            "COOCCURS_WITH",
                            "Condition", "mesh_id", dui2
                    );
                    samyamaClient.createEdge(edge2);

                    edgeCount += 2;
                    line = br.readLine();
                } while (line != null && article.equals(line.split(PIPE)[0]));
            }

            System.out.println("Total Article nodes created: " + createdArticles.size());
            System.out.println("Total COOCCURS_WITH edges created: " + edgeCount);

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RuntimeException("Unable to connect to database");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't read MeSH co-occurrence file");
        }
    }

    /**
     * Load phenotype-genotype associations and create Gene nodes + edges in Samyama.
     */
    private void addPhenotypeGenotypesToSamyama(Model meshModel) {
        String line = "";
        int geneCount = 0;
        int edgeCount = 0;
        Set<String> createdGenes = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(phegeni))) {

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(TAB);
                if (parts.length < 8) continue;

                String trait = parts[1];
                String geneId1 = parts[5];
                String geneId2 = parts[7];

                StmtIterator si = findStatements(meshModel, trait);

                if (si.hasNext()) {
                    Statement s = si.nextStatement();
                    String meshUri = s.getSubject().getURI();
                    String meshDui = meshUri.substring(meshUri.lastIndexOf("/") + 1);

                    // Create Gene nodes if not exists
                    for (String geneId : new String[]{geneId1, geneId2}) {
                        if (!geneId.isEmpty() && !createdGenes.contains(geneId)) {
                            Map<String, Object> props = new HashMap<>();
                            props.put("gene_id", geneId);
                            props.put("uri", RESOURCE.GENE_ID.createURI(geneId));

                            String createNodeQuery = CypherQueryBuilder.createNode("Gene", props);
                            samyamaClient.createNode(createNodeQuery);
                            createdGenes.add(geneId);
                            geneCount++;
                        }

                        // Create ASSOCIATED_WITH edge: Gene -> Condition
                        if (!geneId.isEmpty()) {
                            String edgeQuery = CypherQueryBuilder.createEdge(
                                    "Gene", "gene_id", geneId,
                                    "ASSOCIATED_WITH",
                                    "Condition", "mesh_id", meshDui
                            );
                            samyamaClient.createEdge(edgeQuery);
                            edgeCount++;
                        }
                    }
                }
            }

            System.out.println("Total Gene nodes created: " + geneCount);
            System.out.println("Total ASSOCIATED_WITH edges created: " + edgeCount);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't read PheGenI file");
        }
    }
}
