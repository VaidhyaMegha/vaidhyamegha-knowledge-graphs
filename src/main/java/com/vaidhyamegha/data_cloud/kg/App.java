package com.vaidhyamegha.data_cloud.kg;


import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

/**
 * Accept either a trial id or Pubmed Article id or Symptom (MeSH) or Disease (MeSH).
 * Find matches to the other 3
 */
public class App {

    private static final String PIPE = "\\|";
    @Option(name = "-o", aliases = "--output-rdf", usage = "Path to the final RDF file", required = false)
    private File out = new File("data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_open_kg_clinical_trials.nt");

    @Option(name = "-l", aliases = "--output-trials-list", usage = "Path to the list of trial ids file", required = false)
    private File trials = new File("data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_clinical_trials.csv");

    @Option(name = "-a", aliases = "--output-trials-articles", usage = "Path to the file to collect trial ids to pubmed articles links", required = false)
    private File trials_articles = new File("data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_clinical_trials_articles.csv");

    @Option(name = "-v", aliases = "--mesh-vocab-rdf", usage = "Path to the downloaded MeSH Vocabulary Turtle file.", required = false)
    private String meshVocab = "data/open_knowledge_graph_on_clinical_trials/vocabulary_1.0.0.ttl";

    @Option(name = "-m", aliases = "--mesh-rdf", usage = "Path to the downloaded MeSH RDF file.", required = false)
    private String meshRDF = "data/open_knowledge_graph_on_clinical_trials/mesh2022.nt";

    @Option(name = "-b", aliases = "--mode", usage = "Build RDF or query pre-built RDF?", required = false)
    private MODE mode = MODE.BUILD;

    @Option(name = "-t", aliases = "--trial-id", usage = "Clinical trial's registered id.", required = false)
    private String trial;

    @Option(name = "-p", aliases = "--article-id", usage = "PubMed id for the article.", required = false)
    private String article;

    @Option(name = "-s", aliases = "--symptom-id", usage = "MeSH id for a symptom.", required = false)
    private String symptom;

    @Option(name = "-d", aliases = "--disease-id", usage = "MeSH id for a disease.", required = false)
    private String disease;

    private Properties prop = null;

    public static void main(String[] args) throws IOException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            if (mode == MODE.BUILD) {
                parser.parseArgument(args);

                ClassLoader cl = App.class.getClassLoader();
                prop = readProperties(cl);

                Model model = ModelFactory.createDefaultModel();


                addAllTrials(model, prop);

                FileManager.getInternal().addLocatorClassLoader(cl);

                Model vocab = ModelFactory.createDefaultModel();
                vocab.read(meshVocab, "TURTLE");

                Model meshModel = ModelFactory.createDefaultModel();
                meshModel.read(meshRDF, "NT");

                addTrialConditions(model, meshModel, prop);
                addTrialInterventions(model, meshModel, prop);

                RDFDataMgr.write(new FileOutputStream(out), model, Lang.NT);
            } else {
                throw new UnsupportedOperationException("Query mode is not yet supported");
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java App [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();

            System.err.println("  Example: java App" + parser.printExample(ALL));
        }
    }

    private void addAllTrials(Model model, Properties prop) throws IOException {
        Property id = model.createProperty("TrialId");
        String query = prop.getProperty("aact_trial_ids");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(trials));
             BufferedWriter bwArticle = new BufferedWriter(new FileWriter(trials_articles))) {

            try (Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                    prop.getProperty("user"), prop.getProperty("password"));
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String trialId = resultSet.getString("trial_id");

                    Resource r = model.createResource("https://clinicaltrials.gov/ct2/show/" + trialId);

                    model.add(r, id, trialId);

                    addTrialArticles(r, model, trialId, bw, bwArticle);
                }
            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                throw new RuntimeException("Sorry, unable to connect to database");
            }

            query = prop.getProperty("ictrp_trial_ids");

            try (Connection conn = DriverManager.getConnection(prop.getProperty("ictrp_url"),
                    prop.getProperty("user"), prop.getProperty("password"));
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String trialId = resultSet.getString("trial_id");
                    String uri = "https://www.who.int/clinical-trials-registry-platform/" + trialId;

                    if (trialId.startsWith("NCT")) uri = "https://clinicaltrials.gov/ct2/show/" + trialId;

                    Resource r = model.createResource(uri);

                    model.add(r, id, trialId);

                    addTrialArticles(r, model, trialId, bw, bwArticle);
                }
            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                throw new RuntimeException("Sorry, unable to connect to database");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Sorry, couldn't write to trials file or trials-articles file");
        }
    }

    private void addTrialArticles(Resource r, Model model, String trialId, BufferedWriter bw, BufferedWriter bwArticle) throws IOException {
        Property prop = model.createProperty("Pubmed_Article");

        bw.write(trialId + "\n");

        if (Math.random() > 0.9999) { // constraining so that only a small number of Entrez API calls are made. TODO : Optimize this by checking if an id is already attempted before.
            List<String> articles = EntrezClient.getPubMedIds(trialId).getIdList();
            bwArticle.write(trialId + "|" + Arrays.toString(articles.toArray()) + "\n");

            for (String a : articles) model.add(r, prop, a);
        }

    }

    private void addTrialConditions(Model model, Model meshModel, Properties prop) {
        String query = prop.getProperty("aact_browse_conditions");
        Property p = model.createProperty("Condition");

        addTrialToMeSHLinks(model, meshModel, prop, query, p);
    }

    private void addTrialInterventions(Model model, Model meshModel, Properties prop) {
        String query = prop.getProperty("aact_browse_interventions");
        Property p = model.createProperty("Intervention");

        addTrialToMeSHLinks(model, meshModel, prop, query, p);
    }

    private void addTrialToMeSHLinks(Model model, Model meshModel, Properties prop, String query, Property p) {
        try (Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String trialId = resultSet.getString("nct_id");
                String conditionMeSHTerm = resultSet.getString("mesh_term");

                Resource r = model.createResource("https://clinicaltrials.gov/ct2/show/" + trialId);

                Literal literal = meshModel.createLiteral(conditionMeSHTerm, "en");
                Selector selector = new SimpleSelector(null, null, literal);
                StmtIterator si = meshModel.listStatements(selector);

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
        try (InputStream input = cl.getResourceAsStream("config.properties")) {

            Properties prop = new Properties();

            if (input == null) throw new RuntimeException("Sorry, unable to find config.properties");

            prop.load(input);

            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Sorry, unable to find config.properties");
        }
    }
}
