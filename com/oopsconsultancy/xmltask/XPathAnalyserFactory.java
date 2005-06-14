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

// changed from java.vm.version since this doesn't
// work for JRockit!
    if (System.getProperty("java.version").indexOf("1.5") != -1) {
      analyser = (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk15.XPathAnalyser15").newInstance();
    }
    else {
      analyser = (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk14.XPathAnalyser14").newInstance();
    }
    return analyser;
  }
}
