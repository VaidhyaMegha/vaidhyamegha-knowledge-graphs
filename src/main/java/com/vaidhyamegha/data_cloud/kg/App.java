package com.vaidhyamegha.data_cloud.kg;


import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import org.apache.jena.util.FileManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

/**
 * Accept either a trial id or Pubmed Article id or Symptom (MeSH) or Disease (MeSH).
 * Find matches to the other 3
 */
public class App {

    private static final String PIPE = "\\|";
    @Option(name = "-o", aliases = "--output-rdf", usage = "Path to the final RDF file" , required = false)
    private File out = new File("data/open_knowledge_graph_on_clinical_trials/vaidhyamegha_open_kg_clinical_trials.nt");

    @Option(name = "-v", aliases = "--mesh-vocab-rdf", usage = "Path to the downloaded MeSH Vocabulary Turtle file.", required = false)
    private String meshVocab = "data/open_knowledge_graph_on_clinical_trials/vocabulary_1.0.0.ttl";

    @Option(name = "-c", aliases = "--clinical-trials", usage = "Path to the trials file with MeSH ids for conditions.", required = false)
    private String trialsFile = "data/open_knowledge_graph_on_clinical_trials/browse_conditions.txt";

    @Option(name = "-m", aliases = "--mesh-rdf", usage = "Path to the downloaded MeSH RDF file.", required = false)
    private String meshRDF = "data/open_knowledge_graph_on_clinical_trials/mesh2022.nt";

    @Option(name = "-t", aliases = "--trial-id", usage = "Clinical trial's registered id.", required = false)
    private String trial;

    @Option(name = "-a", aliases = "--article-id", usage = "PubMed id for the article.", required = false)
    private String article;

    @Option(name = "-s", aliases = "--symptom-id", usage = "MeSH id for a symptom.", required = false)
    private String symptom;

    @Option(name = "-d", aliases = "--disease-id", usage = "MeSH id for a disease.", required = false)
    private String disease;

    public static void main(String[] args) throws IOException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            FileManager.getInternal().addLocatorClassLoader(App.class.getClassLoader());

            Model vocab = ModelFactory.createDefaultModel() ;
            vocab.read(meshVocab, "TURTLE") ;

            Model model = ModelFactory.createDefaultModel() ;
            model.read(meshRDF, "NT") ;

            Property p = model.createProperty("Condition");

            BufferedReader br = new BufferedReader(new FileReader(trialsFile));
            String line = br.readLine();

            while((line = br.readLine()) != null) {
                String[] l = line.split(PIPE);
                Literal literal = model.createLiteral(l[2], "en");
                Selector selector = new SimpleSelector(null, null, literal);
                StmtIterator si = model.listStatements(selector);

                if(si.hasNext()) model.add(model.createResource(l[1]), p, si.nextStatement().getSubject());
            }

            RDFDataMgr.write(new FileOutputStream(out), model, Lang.NT) ;

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java App [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java App" + parser.printExample(ALL));
        }
    }
}
