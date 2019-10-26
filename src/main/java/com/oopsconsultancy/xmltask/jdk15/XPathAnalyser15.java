package com.oopsconsultancy.xmltask.jdk15;

import com.oopsconsultancy.xmltask.XPathAnalyser;
import com.oopsconsultancy.xmltask.XPathAnalyserClient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
  private XPathFactory xPathFactory;
  private XPath xPath;

  public XPathAnalyser15() {
    if (xPathFactory == null) {
      try {
        xPathFactory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
      } catch (Exception e) {
        System.out.println("Error: Could not initialize XPath api");
        e.printStackTrace(System.out);
      }
    }
    if (xPath == null && xPathFactory != null) {
        xPath = xPathFactory.newXPath();
    }
  }

  public void registerClient(XPathAnalyserClient client, Object callback) {
    this.client = client;
    this.callback = callback;
  }

  public int analyse(Node node, String xpath) throws Exception {
    int count = 0;
    Object result = null;
    try {
      result = xPath.evaluate(xpath, node, XPathConstants.NODESET);
    } catch (Exception e) {
    }
    if (result instanceof NodeList) {
      NodeList nl = (NodeList) result;
      Node n;
      for (int i = 0; i < nl.getLength(); i++) {
        n = nl.item(i);
        if (n instanceof ProcessingInstruction) {
          client.applyNode(n.getNodeValue(), callback);
        } else {
          client.applyNode(n, callback);
        }
        count++;
      }
    } else {
      result = xPath.evaluate(xpath, node, XPathConstants.STRING);
      String str = (String) result;
      client.applyNode(str, callback);
      count++;
    }

    return count;
  }
}
