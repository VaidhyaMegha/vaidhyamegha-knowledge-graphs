
open (OUT, ">phegeni.tsv") ;
open (USER1,"<PheGenI_Association_full.tab");
open (USER2,"<symptoms.tsv");

my(@array1) = <USER1>;
my(@array2) = <USER2>;
my ($i);
my ($j);

foreach $i (@array1)
{
     chomp($i);
     foreach $j (@array2)
     {
           chomp($j);
           if ($i=~/$j/g)
            {
                 my @elems = split "	",$i;
                 print OUT join"	",@elems[1,2,4,5,6,7,8,12],"\n";
              }
     }
}

close(USER1);
close(USER2);
close OUT;
