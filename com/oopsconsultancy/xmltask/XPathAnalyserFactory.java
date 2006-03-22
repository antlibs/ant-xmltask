package com.oopsconsultancy.xmltask;

/**
 * builds a XPathAnalyser. The concrete type
 * is determined by the JDK in use
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XPathAnalyserFactory {

  public static XPathAnalyser getAnalyser() throws Exception {
    XPathAnalyser analyser;
    boolean use15 = false;

    // changed from java.vm.version since this doesn't
    // work for JRockit!

    if (System.getProperty("java.version").indexOf("1.5") != -1) {
      use15 = true;
    }
    else if (System.getProperty("java.version").indexOf("1.6") != -1) {
      use15 = true;
    }

    if (use15) {
      // now this is a bit nasty. We've first selected a 1.5 but we're running
      // an IBM 1.5 then it still won't have the appropriate classes (below) and
      // we have to switch back to 1.4. See mail from B.Eckenfels@seeburger.de
      // 10/03/2006. This *could* be simplified but I have to test against so many
      // permutations that I'd rather do the below than make a potentially major
      // change
      try {
        Class.forName("com.sun.org.apache.xpath.internal.XPathAPI");
      }
      catch (Exception e) {
        use15 = false;
      }
    }


    // now build the appropriate analyser
    if (use15) {
      analyser = (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk15.XPathAnalyser15").newInstance();
    }
    else {
      analyser = (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk14.XPathAnalyser14").newInstance();
    }
    return analyser;
  }
}
