package com.oopsconsultancy.xmltask;

import java.util.logging.*;

/**
 * builds a XPathAnalyser. The concrete type
 * is determined by the JDK in use
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XPathAnalyserFactory {

  private Logger logger = Logger.getLogger("com.oopsconsultancy.xmltask.XPathAnalyserFactory");

  public static XPathAnalyser getAnalyser() throws Exception {
    XPathAnalyser analyser;

    if (System.getProperty("java.vm.version").indexOf("1.5") != -1) {
      analyser = (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk15.XPathAnalyser15").newInstance();
    }
    else {
      analyser = (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk14.XPathAnalyser14").newInstance();
    }
    return analyser;
  }
}
