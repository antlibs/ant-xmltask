package com.oopsconsultancy.xmltask.jdk15;

import com.oopsconsultancy.xmltask.XPathAnalyser;
import com.oopsconsultancy.xmltask.XPathAnalyserClient;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    this(XPathFactory.class.getName(), XPathFactory.DEFAULT_OBJECT_MODEL_URI);
  }

  @SuppressWarnings("unchecked")
  public XPathAnalyser15(final String xpathFactory, final String xpathObjectModelUri) {
    if (xPathFactory == null) {
      try {
        final Class<XPathFactory> clazz = (Class<XPathFactory>) Class.forName(xpathFactory);
        final Method method = clazz.getMethod("newInstance", String.class);
        xPathFactory = (XPathFactory) method.invoke(null, xpathObjectModelUri);
      } catch (Exception e) {
        System.out.println("Error: Could not initialize XPath API");
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
    Document doc = node.getOwnerDocument();
    if (doc == null && node instanceof Document) {
      doc = (Document) node;
    }
    if (doc != null) {
      xPath.setNamespaceContext(new CachingNamespaceResolver(doc, false));
    }
    try {
      result = xPath.evaluate(xpath, node, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
        System.out.println("Error: incorrect XPath expression");
        e.printStackTrace(System.out);
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

  public class CachingNamespaceResolver implements NamespaceContext {
    private static final String DEFAULT_NS = "DEFAULT";
    private final Map<String, String> prefix2Uri = new HashMap<String, String>();
    private final Map<String, String> uri2Prefix = new HashMap<String, String>();

    /**
     * This constructor checks the document and stores all namespaces it can
     * find. If topLevelOnly is true, only namespaces in the root are used.
     *
     * @param document source document
     * @param topLevelOnly restrict the search to enhance performance
     */
    public CachingNamespaceResolver(Document document, boolean topLevelOnly) {
      examineNode(document.getFirstChild(), topLevelOnly);
      System.out.println("The list of the cached namespaces:");
      for (String key : prefix2Uri.keySet()) {
        System.out.println("prefix " + key + ": uri " + prefix2Uri.get(key));
      }
    }

    /**
     * A single node is read, the namespace attributes are retrieved and stored.
     *
     * @param node to examine
     * @param attributesOnly do not recurse if true
     */
    private void examineNode(Node node, boolean attributesOnly) {
      if (node == null) {
        return;
      }

      NamedNodeMap attributes = node.getAttributes();
      if (attributes != null) {
        for (int i = 0; i < attributes.getLength(); i++) {
          Node attribute = attributes.item(i);
          storeAttribute((Attr) attribute);
        }
      }

      if (attributesOnly) {
        return;
      }

      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE) {
          examineNode(child, false);
        }
      }
    }

    /**
     * Look at an attribute and store it, if it is a namespace attribute.
     *
     * @param attribute to examine
     */
    private void storeAttribute(Attr attribute) {
      // examine the attributes in namespace xmlns
      if (attribute.getNamespaceURI() == null
              || !attribute.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
        return;
      }

      // Default namespace xmlns
      if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
        putInCache(DEFAULT_NS, attribute.getNodeValue());
      } else {
        // The defined prefixes
        putInCache(attribute.getLocalName(), attribute.getNodeValue());
      }
    }

    private void putInCache(String prefix, String uri) {
      prefix2Uri.put(prefix, uri);
      uri2Prefix.put(uri, prefix);
    }

    /**
     * This method is called by XPath. It returns the default namespace, if the
     * prefix is null or "".
     *
     * @param prefix to search for
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
      if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
        return prefix2Uri.get(DEFAULT_NS);
      }
      return prefix2Uri.get(prefix);
    }

    /**
     * This method is not needed in this context, but can be implemented in a
     * similar way.
     */
    public String getPrefix(String namespaceURI) {
      return uri2Prefix.get(namespaceURI);
    }

    /**
     * This method is required by the NamespaceContext interface but is not
     * implemented.
     */
	public Iterator<String> getPrefixes(String namespaceURI) {
        return null;
    }
  }
}
