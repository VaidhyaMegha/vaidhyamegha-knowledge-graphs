use warnings;
use strict;
open(OUT, ">/projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/phenotype_genotype/phegeni.tsv");
open(FILE1, "</projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/open_knowledge_graph_on_clinical_trials/PheGenI_Association_full.tab");
open(FILE2, "</projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/phenotype_genotype/symptoms.tsv");

my (@array1) = <FILE1>;
my (@array2) = <FILE2>;
my ($i);
my ($j);

foreach $i (@array1) {
    chomp($i);
    foreach $j (@array2) {
        chomp($j);
        if ($i =~ /$j/g) {
            my @elems = split "	", $i;
            print OUT join "	", @elems[1, 2, 4, 5, 6, 7, 8, 12], "\n";
        }
    }
}

close(FILE1);
close(FILE2);
close OUT;
