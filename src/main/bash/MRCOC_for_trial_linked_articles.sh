#!/bin/bash
set -ex

PGPASSWORD=0Jg7GdFObf psql -U postgres -h 10.240.64.9 -d aact -t -A -F"|" -c "select p from public.trial_article , unnest(pubmed_articles) p order by p asc" > data/open_knowledge_graph_on_clinical_trials/pubmed_articles.txt

cut -d '|' -f1,9,15 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt
sort -u  data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt

# https://unix.stackexchange.com/a/330662/47615
# Below bash implementation is an inefficient way of searching articles in MRCOC.
# A better approach would be to sort the file to searched-in and use binary search as suggested here : https://stackoverflow.com/a/37492791/294552
# https://stackoverflow.com/a/70918198/294552 : Instead sorting both files and applying a file co-parsing approach suggested by [Madhulatha](https://www.linkedin.com/in/mandarapu-madhulatha-72bb6b2a/) seems optimal. It is implemented in java.
cat data/open_knowledge_graph_on_clinical_trials/pubmed_articles.txt | while read f; do
    article=$(echo ${f} | cut -d'|' -f 1 );
    echo "${article}|";
    grep -F "${article}|" data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt >> data/open_knowledge_graph_on_clinical_trials/filtered_co_occurrence.txt || true;
done;