#!/bin/bash
set -ex

rm -rf temp_nt
mkdir temp_nt
cd temp_nt

split -l100000 /data/vaidhyamegha_open_kg_clinical_trials.nt
echo "open knowledge-graph-1" > ../commands.txt
echo "clear" > ../commands.txt
echo "open knowledge-graph-1" > ../commands.txt

ls x* | while read f; do

	echo "load /data/tools/eclipse-rdf4j-3.6.3/temp_nt/${f}.nt" >> ../commands.txt
	mv "${f}" "${f}.nt"; 

done;


echo "quit" >> ../commands.txt

cd ../

cat commands.txt | ./bin/console.sh


rm -rf temp_nt