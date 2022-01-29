package com.vaidhyamegha.data_cloud.kg;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.http.codec.xml.Jaxb2XmlEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

public class EntrezClient {
    public static ESearchResult getPubMedIds(String trialId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));

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
