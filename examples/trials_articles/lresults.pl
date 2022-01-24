
use LWP::Simple;
$db="pubmed";

$base="https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
open OUT, ">results.pl";
open (USER, "<NCTNumbers.txt") or die "Couldn't open file: $userfile1";

while($line=<USER>)
{

	chomp $line;
	$line=$line."[si]";

	$url=$base."esearch.fcgi?db=$db&term=$line&retmin=0&retmax=100&usehistory=y";

	$output=get($url);
  my @matches = $output =~ m(<Id>(.*)</Id>)g;
	print OUT "$line"."\t"."@matches\n";

}
	close USER;
  close OUT;
