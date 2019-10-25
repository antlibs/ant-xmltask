package com.oopsconsultancy.xmltask;

import org.apache.tools.ant.Task;
import org.w3c.dom.Node;

import java.io.File;

/**
 * removes the existing node, and inserts the specified
 * XML at that point
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XmlAction extends InsertAction {

  /*
  public final static String DUMMY = "XMLTASK";
  public final static String DUMMYNODE = "<" + DUMMY + ">";
  public final static String DUMMYENODE = "</" + DUMMY + ">";
  */

  // protected Document doc2 = null;
  // private DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
  // private boolean wellFormed = true;
  // private String buffer = null;

  /*
  {
    dfactory.setNamespaceAware(true);
  }
  */

  public XmlAction() {
    super();
  }

  public static XmlAction xmlActionfromString(String txml, Task task) throws Exception {
    return new XmlAction(txml, task);
  }
  public static XmlAction xmlActionfromFile(File xml, Task task) throws Exception {
    return new XmlAction(xml, task);
  }
  public static XmlAction xmlActionfromBuffer(String buffer, Task task) throws Exception {
    XmlAction xa = new XmlAction();
    xa.buffer = buffer;
    xa.task = task;
    return xa;
  }

  /* *
   * performs the reading of the xml. Can handle non-well
   * formed documents
   *
   * @param xml
   * @throws Exception
   */
   /*
  private void readXml(String xml) throws Exception {
    StringReader sr = new StringReader(xml);
    DocumentBuilder db = getBuilder();
    doc2 = db.parse(new InputSource(sr));
  }
  */

  /*
  private DocumentBuilder getBuilder() throws ParserConfigurationException {
    DocumentBuilder db = dfactory.newDocumentBuilder();
    db.setErrorHandler(new ErrorHandler(){
        public void error(SAXParseException e) {
          System.err.println(e.getMessage());
        }
        public void fatalError(SAXParseException e) {
          // I want to disable the error o/p for non-well
          // formed documents
        }
        public void warning(SAXParseException e) {
          System.err.println(e.getMessage());
        }
        });
    return db;
  }
  */


  private XmlAction(String txml, Task task) throws Exception {
    super(txml, task);
    /*
    try {
      readXml(txml);
    }
    catch (SAXParseException e) {
      // it could not be well-formed, so we'll wrap and try again...
      readXml(DUMMYNODE + txml + DUMMYENODE);
      wellFormed = false;
    }
    */
  }

  private XmlAction(File xml, Task task) throws Exception {
    super(xml, task);
    /*
    InputSource in2 = new InputSource(new FileInputStream(xml));
    try {
      DocumentBuilder db = getBuilder();
      doc2 = db.parse(in2);
    }
    catch (SAXParseException e) {
      // it could not be well-formed, so we'll wrap and try again...
      BufferedReader bfr = new BufferedReader(new FileReader(xml));
      StringBuffer sxml = new StringBuffer();
      while (bfr.ready()) {
        sxml.append(bfr.readLine() + "\n");
      }
      bfr.close();
      readXml(DUMMYNODE + sxml.toString() + DUMMYENODE);
      wellFormed = false;
    }
    */
  }

  public boolean apply(Node node) throws Exception {
    pos = Position.BEFORE;
    remove(node);
    return insert(node);
  }

  /*
  public boolean apply(Node node) throws Exception {
    Node pn = node.getParentNode();
    Node newnode = null;
    remove(node);

    if (buffer != null) {
      // we'll get the doc to replace with from the buffer...
      Node n2 = BufferStore.get(buffer);
      if (n2 != null) {
        newnode = doc.importNode(n2, true);
        wellFormed = false;
      }

    }
    else if (doc2 != null) {
      newnode = doc.importNode(doc2.getDocumentElement(), true);
    }

    if (newnode != null) {
      if (!wellFormed) {
        // I need to extract the nodes below the dummy root node
        DocumentFragment frag = doc.createDocumentFragment();
        NodeList children = newnode.getChildNodes();
        for (int c = 0; c < children.getLength(); ) {
          // we can do this as the appendChild is removing at the same time
          frag.appendChild(children.item(c));
        }
        newnode = frag;
      }
      pn.insertBefore(newnode, node);
    }
    return true;
  }
  */

  public String toString() {
    return "XmlAction(" + (doc2 == null ? (buffer == null ? "" : "buffer " + buffer) : doc2.getDocumentElement().toString()) + ")";
  }
}
