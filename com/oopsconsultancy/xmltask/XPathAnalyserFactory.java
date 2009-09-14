package com.oopsconsultancy.xmltask;

/**
 * builds a XPathAnalyser. The concrete type
 * is determined by the JDK in use
 * 
 * This used to switch between 1.4/1.5 etc. Now we only support
 * 1.5 and beyond (September 2009)
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XPathAnalyserFactory {

  public static XPathAnalyser getAnalyser() throws Exception {
    XPathAnalyser analyser;

    return (XPathAnalyser)Class.forName("com.oopsconsultancy.xmltask.jdk15.XPathAnalyser15").newInstance();
  }
}
