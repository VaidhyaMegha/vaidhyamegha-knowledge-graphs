use warnings;
use strict;
use LWP::Simple;

my ($db);
my ($base);
my ($line);
my ($url);
my ($output);

$db = "pubmed";

$base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

open OUT, ">/projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/trials_articles/results.txt";
open(USER, "</projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/trials_articles/NCTNumbers.txt") or die "Couldn't open file: NCTNumbers.txt";

while ($line = <USER>) {
    chomp $line;
    $line = $line . "[si]";

    $url = $base . "esearch.fcgi?db=$db&term=$line&retmin=0&retmax=100&usehistory=y";

    $output = get($url);
    my @matches = $output =~ m(<Id>(.*)</Id>)g;
    print OUT "$line" . "\t" . "@matches\n";

}

close USER;
close OUT;
