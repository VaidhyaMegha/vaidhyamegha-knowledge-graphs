use warnings;
use strict;
use LWP::Simple;
use Array::Utils qw(:all);
use 5.010;
use Data::Dumper;

# Diseases to Drugs/Interventions Linking via PubMed Co-occurrence
#
# This script finds associations between diseases and drugs/interventions
# by identifying PubMed articles that mention BOTH a disease AND a drug.
# Follows the same workflow as symptoms_diseases.pl

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

# File paths - update these to match your local setup
my $data_dir = "/home/raghu-varma/Downloads/vaidhyamegha-knowledge-graphs/data/diseases_drugs";

open OUT, ">$data_dir/diseases_drugs_cooccurrence.tsv" or die "Couldn't open output file: $!";
open(FILE1, "<$data_dir/diseases.txt") or die "Couldn't open file: diseases.txt";
open(FILE2, "<$data_dir/drugs.txt") or die "Couldn't open file: drugs.txt";

# Read disease and drug names for later use
my @disease_names;
my @drug_names;

# Phase 1: Fetch PubMed IDs for diseases
print "Phase 1: Fetching PubMed IDs for diseases...\n";
open(FH, ">$data_dir/diseases_pmids.txt") or die $!;
while ($line1 = <FILE1>) {
    chomp $line1;
    push @disease_names, $line1;
    my $search_term = $line1 . "[mesh]";
    $url = $base . "esearch.fcgi?db=$db&term=$search_term&retmin=0&retmax=5000&usehistory=y";
    $output = get($url);
    if (defined $output) {
        my @matches = $output =~ m(<Id>(.*)</Id>)g;
        print FH "@matches\n";
        print "  $line1: found " . scalar(@matches) . " articles\n";
    } else {
        print FH "\n";
        print "  $line1: query failed\n";
    }
    sleep(1);  # Rate limiting - be nice to NCBI servers
}
close FH;
close FILE1;

# Phase 2: Fetch PubMed IDs for drugs
print "\nPhase 2: Fetching PubMed IDs for drugs...\n";
open(FH2, ">$data_dir/drugs_pmids.txt") or die $!;
while ($line2 = <FILE2>) {
    chomp $line2;
    push @drug_names, $line2;
    my $search_term = $line2 . "[mesh]";
    $url2 = $base . "esearch.fcgi?db=$db&term=$search_term&retmin=0&retmax=5000&usehistory=y";
    $output2 = get($url2);
    if (defined $output2) {
        my @matches2 = $output2 =~ m(<Id>(.*)</Id>)g;
        print FH2 "@matches2\n";
        print "  $line2: found " . scalar(@matches2) . " articles\n";
    } else {
        print FH2 "\n";
        print "  $line2: query failed\n";
    }
    sleep(1);  # Rate limiting
}
close FH2;
close FILE2;

# Phase 3: Load PubMed IDs into arrays
print "\nPhase 3: Loading PubMed IDs into memory...\n";
open(DATA1, "<$data_dir/diseases_pmids.txt") or die "Couldn't open file: diseases_pmids.txt";
my @AoA;
while (<DATA1>) {
    my @line = split;
    push @AoA, [ @line ];
}
close DATA1;

open(DATA2, "<$data_dir/drugs_pmids.txt") or die "Couldn't open file: drugs_pmids.txt";
my @AoA2;
while (<DATA2>) {
    my @line = split;
    push @AoA2, [ @line ];
}
close DATA2;

# Phase 4: Find co-occurrences and write TSV output
print "\nPhase 4: Finding co-occurrences and writing TSV output...\n";

# Write TSV header
print OUT "Disease\tDrug\tCooccurrence_Count\tSample_PMIDs\n";

my $associations_found = 0;

foreach my $i (0 .. $#AoA) {
    foreach my $j (0 .. $#AoA2) {
        my @common = intersect(@{$AoA[$i]}, @{$AoA2[$j]});
        my $count = scalar(@common);

        if ($count > 0) {
            my $disease = $disease_names[$i];
            my $drug = $drug_names[$j];

            # Get first 10 PMIDs as sample
            my @sample = @common[0 .. ($count > 10 ? 9 : $count - 1)];
            my $sample_pmids = join(",", @sample);

            print OUT "$disease\t$drug\t$count\t$sample_pmids\n";
            $associations_found++;
        }
    }
}

close OUT;

# Summary
print "\n" . "=" x 50 . "\n";
print "SUMMARY\n";
print "=" x 50 . "\n";
print "  Diseases processed: " . scalar(@disease_names) . "\n";
print "  Drugs processed:    " . scalar(@drug_names) . "\n";
print "  Associations found: $associations_found\n";
print "  Output file:        $data_dir/diseases_drugs_cooccurrence.tsv\n";
print "=" x 50 . "\n";
