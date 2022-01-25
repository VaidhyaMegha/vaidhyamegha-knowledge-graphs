use warnings;
use strict;
use LWP::Simple;
use Array::Utils qw(:all);
use 5.010;
use Data::Dumper;

my ($db);
my ($base);
my ($line1);
my ($line2);
my ($url);
my ($url2);
my ($output);
my ($output2);

$db = "pubmed";
$base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
open OUT, ">/projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/symptoms_diseases/final.txt";
open(USER1, "</projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/symptoms_diseases/d100.txt") or die "Couldn't open file: d100.txt";
open(USER2, "</projects/VaidhyaMegha/vaidhyamegha-knowledge-graphs/data/symptoms_diseases/s100.txt") or die "Couldn't open file: s100.txt";

open(FH, ">>dp.txt") or die $!;
while ($line1 = <USER1>) {
    chomp $line1;
    $line1 = $line1 . "[mesh]";
    $url = $base . "esearch.fcgi?db=$db&term=$line1&retmin=0&retmax=50000&usehistory=y";
    $output = get($url);
    my @matches = $output =~ m(<Id>(.*)</Id>)g;
    print FH "@matches\n";
}
close FH;

open(FH2, ">>sp.txt") or die $!;
while ($line2 = <USER2>) {
    chomp $line2;
    $url2 = $base . "esearch.fcgi?db=$db&term=$line2&retmin=0&retmax=50000&usehistory=y";
    $output2 = get($url2);
    my @matches2 = $output2 =~ m(<Id>(.*)</Id>)g;
    print FH2 "@matches2\n";
}
close FH2;

open(DATA1, "<dp.txt") or die "Couldn't open file: dp.txt";
my @AoA;
while (<DATA1>) {
    my @line = split;
    push @AoA, [ @line ];
}
close DATA1;

open(DATA2, "<sp.txt") or die "Couldn't open file: sp.txt";
my @AoA2;
while (<DATA2>) {
    my @line = split;
    push @AoA2, [ @line ];
}
close DATA2;

foreach my $i (0 .. $#AoA) {
    foreach my $j (0 .. $#AoA2) {
        my @s = intersect(@{$AoA[$i]}, @{$AoA2[$j]});
        print OUT "@s\n";
    }
}

close USER1;
close USER2;
close OUT;
