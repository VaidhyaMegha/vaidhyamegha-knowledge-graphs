package com.vaidhyamegha.data_cloud.kg;


import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.http.codec.xml.Jaxb2XmlEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Many implementations for Entrez clients are available in the open. Below reference allowed a succinct implementation .
 * Reference : https://stackoverflow.com/questions/68209076/spring-resttemplate-works-for-string-but-not-for-my-class
 * More references are in docs/open_knowledge_graph_on_clinical_trials/README.md.
 */
public class EntrezClient {
    public static ESearchResult getPubMedIds(String trialId) {
        WebClient wb = WebClient.builder()
                .defaultHeaders(header -> {
                    header.setContentType(MediaType.APPLICATION_XML);
                })
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer -> {
                                    configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder());
                                    configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder());
                                })
                                .build()
                ).build();

        return wb.get()
                .uri("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term="
                        + trialId + "[si]&retmin=0&retmax=100&usehistory=y")
                .retrieve()
                .bodyToMono(ESearchResult.class)
                .block();
    }
}
