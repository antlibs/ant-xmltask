package com.oopsconsultancy.xmltask;

import java.lang.reflect.Constructor;

/**
 * builds a XPathAnalyser.
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XPathAnalyserFactory {

  public static XPathAnalyser getAnalyser() throws Exception {
    return (XPathAnalyser) Class.forName("com.oopsconsultancy.xmltask.jdk15.XPathAnalyser15").newInstance();
  }

  public static XPathAnalyser getAnalyser(final String xpathFactory, final String xpathObjectModelUri) throws Exception {
    @SuppressWarnings("unchecked")
    final Class<XPathAnalyser> clazz = (Class<XPathAnalyser>) Class.forName("com.oopsconsultancy.xmltask.jdk15.XPathAnalyser15");
    final Constructor<XPathAnalyser> cstr = clazz.getConstructor(String.class, String.class);
    return cstr.newInstance(xpathFactory, xpathObjectModelUri);
  }
}
