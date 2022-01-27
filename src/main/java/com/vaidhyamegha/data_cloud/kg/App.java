package com.vaidhyamegha.data_cloud.kg;


import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import org.apache.jena.util.FileManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.sql.*;
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

    @Option(name = "-v", aliases = "--mesh-vocab-rdf", usage = "Path to the downloaded MeSH Vocabulary Turtle file.", required = false)
    private String meshVocab = "data/open_knowledge_graph_on_clinical_trials/vocabulary_1.0.0.ttl";

    @Option(name = "-m", aliases = "--mesh-rdf", usage = "Path to the downloaded MeSH RDF file.", required = false)
    private String meshRDF = "data/open_knowledge_graph_on_clinical_trials/mesh2022.nt";

    @Option(name = "-b", aliases = "--mode", usage = "Build RDF or query pre-built RDF?", required = false)
    private MODE mode = MODE.BUILD;

    @Option(name = "-t", aliases = "--trial-id", usage = "Clinical trial's registered id.", required = false)
    private String trial;

    @Option(name = "-a", aliases = "--article-id", usage = "PubMed id for the article.", required = false)
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

                FileManager.getInternal().addLocatorClassLoader(cl);
                Model vocab = ModelFactory.createDefaultModel();
                vocab.read(meshVocab, "TURTLE");

                Model model = ModelFactory.createDefaultModel();
                model.read(meshRDF, "NT");

                addTrialConditions(model, prop);

                addTrialInterventions(model, prop);

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

    private void addTrialConditions(Model model, Properties prop) {
        String SQL_SELECT = prop.getProperty("aact_browse_conditions");
        Property p = model.createProperty("Condition");

        addTrialToMeSHLinks(model, prop, SQL_SELECT, p);
    }

    private void addTrialInterventions(Model model, Properties prop) {
        String SQL_SELECT = prop.getProperty("aact_browse_interventions");
        Property p = model.createProperty("Intervention");

        addTrialToMeSHLinks(model, prop, SQL_SELECT, p);
    }

    private void addTrialToMeSHLinks(Model model, Properties prop, String SQL_SELECT, Property p) {
        Property id = model.createProperty("TrialId");

        try (Connection conn = DriverManager.getConnection(prop.getProperty("aact_url"),
                prop.getProperty("user"), prop.getProperty("password"));
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String trialId = resultSet.getString("nct_id");
                String conditionMeSHTerm = resultSet.getString("mesh_term");

                Resource r = model.createResource("https://clinicaltrials.gov/ct2/show/" + trialId);

                model.add(r, id, trialId);

                Literal literal = model.createLiteral(conditionMeSHTerm, "en");
                Selector selector = new SimpleSelector(null, null, literal);
                StmtIterator si = model.listStatements(selector);

                if (si.hasNext()) model.add(r, p, si.nextStatement().getSubject());
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
