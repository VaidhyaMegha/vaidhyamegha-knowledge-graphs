#!/bin/bash
set -ex

PGPASSWORD=0Jg7GdFObf psql -U postgres -h 10.240.64.9 -d aact -t -A -F"|" -c "select p from public.trial_article , unnest(pubmed_articles) p order by p asc" > data/open_knowledge_graph_on_clinical_trials/pubmed_articles.txt

cut -d '|' -f1,9,15 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt
sort -u  data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt

# https://unix.stackexchange.com/a/330662/47615
# Inefficient way of searching articles in MRCOC. Instead file co-parsing approach suggested by [Madhulatha](https://www.linkedin.com/in/mandarapu-madhulatha-72bb6b2a/) is implemented in java.
cat data/open_knowledge_graph_on_clinical_trials/pubmed_articles.txt | while read f; do
    article=$(echo ${f} | cut -d'|' -f 1 );
    echo "${article}|";
    grep -F "${article}|" data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt >> data/open_knowledge_graph_on_clinical_trials/filtered_co_occurrence.txt || true;
done;