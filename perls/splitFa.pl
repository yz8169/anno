use strict;
use warnings;
use Getopt::Long;

my ($n);
GetOptions(
    "n:i"=>\$n
);

open A,$ARGV[0];
my $rec=0;my $num=1;
$/=">";<A>;
open OUT,">part$num.fa";
while(<A>){
    chomp;$rec++;
    print OUT ">$_";
    if($rec >= $n){
        close OUT;
        $rec=0; $num++;
        open OUT,">part$num.fa";
    }
}
close A;close OUT;
