package com.oopsconsultancy.xmltask.jdk15;

import com.oopsconsultancy.xmltask.XPathAnalyser;
import com.oopsconsultancy.xmltask.XPathAnalyserClient;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XBoolean;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.objects.XString;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * uses the JDK 1.5 XPath API
 * to analyse XML docs
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XPathAnalyser15 implements XPathAnalyser {

  private XPathAnalyserClient client;
  private Object callback;

  public void registerClient(XPathAnalyserClient client, Object callback) {
    this.client = client;
    this.callback = callback;
  }

  public int analyse(Node node, String xpath) throws Exception {
    int count = 0;
    XObject result = XPathAPI.eval(node, xpath);
    if (result instanceof XNodeSet) {
      NodeIterator nl = result.nodeset();
      Node n;
      while ((n = nl.nextNode()) != null) {
        client.applyNode(n, callback);
        count++;
      }
    }
    else if (result instanceof XBoolean ||
             result instanceof XNumber ||
             result instanceof XString) {
      String str = result.str();
      client.applyNode(str, callback);
      count++;
    }
    return count;
  }
}

