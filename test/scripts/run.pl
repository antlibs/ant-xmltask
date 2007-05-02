#!/usr/bin/perl
# $Id$

#
# tests for xmltask. This swaps tests in/out depending on the
# VM being run. Plus some tests give no output, some fail etc.
# The script below handles all of that, but should be re-written
# as a data-set-driven test suite

my $CP=$ENV{'CLASSPATH'};

my @tests = (1..116);
if (@ARGV > 0) {
  @tests = @ARGV;
}


(my $jv = $ENV{'JAVAHOME'}) =~ s{^/usr/java/(.*)[/]$}{$1};
print "Java version = $jv\n";

my $xmlcatalog = "../../classes/org/apache/tools/ant/types/XMLCatalog.class";
if (-e $xmlcatalog) {
  # xmlcatalog.class found, so we may pick up
  # the wrong XMLCatalog.class
  print STDERR "XMLCatalog found. Possible XMLCatalog confusion. Removing .class file\n";
  unlink $xmlcatalog;
}

foreach $i ( @tests ) {
  # clear the temp dir
  rmdir "temp";
  mkdir "temp";

  my $nofile = 0;
  my $args = "";
  if ($i == 62 || $i == 75 || $i == 81 || $i == 87 || $i == 94 || $i == 97 || $i == 98 || $i == 112) {
    # which tests shouldn't return results ?
    $nofile = 1;
    print "No output expected for #" . $i . "\n";
  }
  if ($i == 88) {
    $args = "test test";
  }
  my $build = "build-$i.xml";
  if (`grep "JIS" $build` && ($jv =~ /1.3/ || $jv =~ /ibm_sdk50/)) {
    print "Skipping $build due to unsupported encoding in this JVM\n";
  }
  else {
    print "Running $build\n";
    if ($i == 106) {
      print "Modifying classpath!\n";
      $ENV{'CLASSPATH'}="/home/brian/java/jakarta-ant/1.6.3//lib/ant.jar:/home/brian/java/jakarta-ant/1.6.3//lib/ant-commons-net.jar";
    }
    else {
      $ENV{'CLASSPATH'}=$CP;
    }
    `ant -buildfile $build $args`;
    my $res = $i."-out.xml";
    my $cmp = "results/".$res;
    if ($jv =~ /1\.[56]/ && -e "results/".$i."-1.5-out.xml") {
      # swap in a 1.5/6 file if it exists
      $cmp = "results/".$i."-1.5-out.xml";
    }
    if (($? >> 8) == 0) {
      if (! -e $res) {
        if ($nofile == 0) {
          print STDERR "ant -buildfile $build failed to create $res\n";
          print STDERR "TESTS FAIL\n";
          exit(1);
        }
        else {
          # no file produced, as expected
        }
      }
      else {
        print "Comparing $res vs $cmp\n";
        if (! -e $cmp) {
          print STDERR "Nothing to compare $res to\n";
          print STDERR "TESTS FAIL\n";
          exit(1);
        }
        `diff $res $cmp`;
        if (($? >> 8) != 0) {
          print STDERR "$res and $cmp differ!\n";
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
