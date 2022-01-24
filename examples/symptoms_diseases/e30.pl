use LWP::Simple;
use Array::Utils qw(:all);
use 5.010;
use Data::Dumper;
$db="pubmed";
$base="https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
open OUT, ">final.txt";
open (USER1, "<d100.txt") or die "Couldn't open file: $userfile1";
open (USER2, "<s100.txt") or die "Couldn't open file: $userfile2";
while($line1=<USER1>)
{
	chomp $line1;
	$line1=$line1."[mesh]";
	$url=$base."esearch.fcgi?db=$db&term=$line1&retmin=0&retmax=50000&usehistory=y";
	$output=get($url);
	my @matches = $output =~ m(<Id>(.*)</Id>)g;
	open(FH, ">>dp.txt") or die $!;
	print FH "@matches\n";
	close FH;
}
while($line2=<USER2>)
{
	chomp $line2;
	$url2=$base."esearch.fcgi?db=$db&term=$line2&retmin=0&retmax=50000&usehistory=y";
	$output2=get($url2);
	my @matches2 = $output2 =~ m(<Id>(.*)</Id>)g;
	open(FH2, ">>sp.txt") or die $!;
	print FH2 "@matches2\n";
	close FH2;
}
open (DATA1, "<dp.txt") or die "Couldn't open file: $userfile1";
my @AoA ;
while  (<DATA1>)
{
  my @line = split ;
  push @AoA, [@line] ;
}
close DATA1;
open (DATA2, "<sp.txt") or die "Couldn't open file: $userfile2";
my @AoA2 ;
while  (<DATA2>)
{
  my @line = split ;
  push @AoA2, [@line] ;
}
close DATA2;
foreach my $i (0..$#AoA)
{
 	foreach my $j (0..$#AoA2)
	{
		my @s = intersect (@{$AoA[$i]},@{$AoA2[$j]});
		print OUT "@s\n";
	}
}
close USER1;
close USER2;
close OUT;
