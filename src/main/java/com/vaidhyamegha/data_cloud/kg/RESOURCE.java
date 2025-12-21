package com.vaidhyamegha.data_cloud.kg;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import static com.vaidhyamegha.data_cloud.kg.Constants.OPEN_KG_CT_NS_TYPE;

enum RESOURCE {TRIAL, PUBMED_ARTICLE, GENE_ID, MESH_DUI, NS_TYPE;

    // Get URI string without creating Jena Resource (for Samyama)
    String createURI(String rId) {
        rId = rId.trim();
        switch (this) {
            case TRIAL:
                if (!rId.startsWith("NCT"))
                    return "https://www.who.int/clinical-trials-registry-platform/" + rId;
                return "https://clinicaltrials.gov/ct2/show/" + rId;
            case PUBMED_ARTICLE:
                return "https://pubmed.ncbi.nlm.nih.gov/" + rId;
            case MESH_DUI:
                return "https://meshb.nlm.nih.gov/record/ui?ui=" + rId;
            case GENE_ID:
                return "https://www.ncbi.nlm.nih.gov/gene/" + rId;
            case NS_TYPE:
                return OPEN_KG_CT_NS_TYPE;
            default:
                throw new RuntimeException("Unsupported resource type " + this);
        }
    }

    // Original method for Jena Resource (kept for backward compatibility)
    Resource createResource(Model model, String rId) {
        rId = rId.trim();
        switch (this) {
            case TRIAL:
                String uri = "https://clinicaltrials.gov/ct2/show/" + rId;

                if (!rId.startsWith("NCT"))
                    uri = "https://www.who.int/clinical-trials-registry-platform/" + rId;

                return model.createResource(uri);
            case PUBMED_ARTICLE:
                uri = "https://pubmed.ncbi.nlm.nih.gov/" + rId;
                return model.createResource(uri);
            case MESH_DUI:
                uri = "https://meshb.nlm.nih.gov/record/ui?ui=" + rId;
                return model.createResource(uri);
            case GENE_ID:
                uri = "https://www.ncbi.nlm.nih.gov/gene/" + rId;
                return model.createResource(uri);
            case NS_TYPE:
                uri = OPEN_KG_CT_NS_TYPE;
                return model.createResource(uri);
            default:
                throw new RuntimeException("Unsupported resource type " + this);
        }
    }
}
