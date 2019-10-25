#!/usr/bin/perl

my @tests = (1..39,41..69);
if (@ARGV > 0) {
  @tests = @ARGV;
}
(my $jv = $ENV{'JAVAHOME'}) =~ s{^/usr/java/(.*)[/]$}{$1};
print "Java version = $jv\n";

foreach $i ( @tests ) {
  my $nofile = 0;
  if ($i == 62) {
    # which tests shouldn't return results ?
    $nofile = 1;
  }
  my $build = "build-$i.xml";
  if (`grep "JIS" $build` && $jv =~ /1.3/) {
    print "Skipping $build due to unsupported encoding in this JVM\n";
  }
  else {
    print "Running $build\n";
    `ant -buildfile $build`;
    my $res = $i."-out.xml";
    if (($? >> 8) == 0) {
      if (! -e $res) {
        print STDERR "ant -buildfile $build failed to create $res\n";
        print STDERR "TESTS FAIL\n";
        exit(1);
      }
      else {
        print "Comparing $res\n";
        if (! -e "results/$res") {
          print STDERR "Nothing to compare $res to\n";
          print STDERR "TESTS FAIL\n";
          exit(1);
        }
        `diff $res results/$res`;
        if (($? >> 8) != 0) {
          print STDERR "$res and results/$res differ!\n";
          print STDERR "TESTS FAIL\n";
          exit(1);
        }
        else {
          unlink $res;
        }
      }
    }
    else {
      print STDERR "ant -buildfile $build returned ".($? >> 8)."\n";
      if ($nofile) {
        # check for no file produced
        if (-e $res) {
          print STDERR "Erroneously produced an output file. Unexpected for this test!\n";
          print STDERR "TESTS FAIL\n";
          exit(1);
        }
        else {
          print "EXPECTED RESULT\n";
        }
      }
      else {
        exit(1);
      }  
    }
  }
}
print "------------------\nSCRIPTED TESTS OK\n";
exit(0);
