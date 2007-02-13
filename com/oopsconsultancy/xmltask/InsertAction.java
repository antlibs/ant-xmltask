package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import javax.xml.parsers.*;
import org.apache.tools.ant.*;

/**
 * performs the insertion of XML below the qualifying nodes.
 * Note that we can insert both well-formed and rootless
 * XML documents under a given node. The document may not have
 * a root node, in which case we can insert the given document
 * as the root node
 *
 * Note that this has donated huge chunks of code to the uncomment
 * task and both deserve refactoring
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class InsertAction extends Action {

  public final static String DUMMY = "XMLTASK";
  public final static String DUMMYNODE = "<" + DUMMY + ">";
  public final static String DUMMYENODE = "</" + DUMMY + ">";

  /**
   * represents the insertion points for a document fragment/element
   */
  public static class Position {
    private final String label;
    private Position(final String label) {
      this.label = label;
    }
    public final static Position UNDER = new Position("under");
    public final static Position BEFORE = new Position("before");
    public final static Position AFTER = new Position("after");

    public String toString() {
      return label;
    }
  }

  protected Document doc2 = null;
  private DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
  protected Position pos = Position.UNDER;
  protected boolean wellFormed = true;
  protected String buffer = null;
  protected Task task = null;

  {
    // why do I do this ? Causes problems with
    // Pete Hale and namespace-scoped insertions
    dfactory.setNamespaceAware(true);
  }

  private DocumentBuilder getBuilder() throws ParserConfigurationException {
    DocumentBuilder db = dfactory.newDocumentBuilder();
    db.setErrorHandler(new ErrorHandler(){
        public void error(final SAXParseException e) {
          System.err.println(e.getMessage());
        }
        public void fatalError(final SAXParseException e) {
          // I want to disable the error o/p for non-well
          // formed documents
        }
        public void warning(final SAXParseException e) {
          System.err.println(e.getMessage());
        }
        });
    return db;
  }

  public static InsertAction fromString(final String txml, final Task task) throws Exception {
    return new InsertAction(txml, task);
  }

  public static InsertAction fromFile(final File file, final Task task) throws Exception {
    return new InsertAction(file, task);
  }

  public static InsertAction fromBuffer(final String buffer, final Task task) throws Exception {
    InsertAction ia = new InsertAction();
    ia.buffer = buffer;
    ia.task = task;
    return ia;
  }

  /**
   * builds an empty insertion. This is used in conjunction with
   * buffers (ie. the buffer contents will change, so we can't record
   * the buffer contents here....)
   */
  protected InsertAction() {
  }

  /**
   * builds an insertion action and reads the xml to
   * insert from a string. If the xml reading gives an
   * exception it may be because it's not well formed. So
   * we attempt to read again giving the doc a root element
   *
   * @param txml
   * @param task
   * @throws Exception
   */
  protected InsertAction(final String txml, final Task task) throws Exception {
    this.task = task;
    try {
      readXml(txml);
    }
    catch (SAXParseException e) {
      // it could not be well-formed, so we'll wrap and try again...
      readXml(DUMMYNODE + txml + DUMMYENODE);
      wellFormed = false;
    }
  }

  /**
   * builds an insertion action and reads the xml to
   * insert from a file. If the xml reading gives an
   * exception it may be because it's not well formed. So
   * we attempt to read again giving the doc a root element
   *
   * @param xml
   * @param task
   * @throws Exception
   */
  protected InsertAction(final File xml, final Task task) throws Exception {
    this.task = task;
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
  }

  /**
   * performs the reading of the xml. Can handle non-well
   * formed documents
   *
   * @param xml
   * @throws Exception
   */
  protected void readXml(final String xml) throws Exception {
    StringReader sr = new StringReader(xml);
    DocumentBuilder db = getBuilder();
    doc2 = db.parse(new InputSource(sr));
  }


  public void setPosition(final Position val) {
    pos = val;
  }

  /**
   * inserts the specified XML below this node. If the node
   * isn't an element, then this is reported and the task exits
   *
   * @param node
   * @throws Exception
   */
  public boolean apply(final Node node) throws Exception {
    return insert(node);
  }

  private void log(final String msg, final int level) {
    if (task != null) {
      task.log(msg, level);
    }
    else {
      System.out.println(msg);
    }
  }

  /**
   * performs the insertion under the given node
   *
   * @param node
   * @return true on success
   * @throws Exception
   */
  protected boolean insert(final Node node) throws Exception {
    Node newnode = null;
    if (buffer != null) {
      Node[] n2 = BufferStore.get(buffer, task);
      if (n2 != null) {
        // note the reverse iteration here to preserve ordering
        // (certainly for position="after". What about other
        // positions?)
        for (int n =  n2.length - 1; n >= 0;  n--) {
          log("Inserting " + n2[n], Project.MSG_VERBOSE);
          newnode = doc.importNode(n2[n], true);
          insertNode(node, newnode);
        }
        return true;
      }
    }
    else if (doc2 != null) {
      newnode = doc.importNode(doc2.getDocumentElement(), true);
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
      return insertNode(node, newnode);
    }
    return false;
  }

  /**
   * inserts the new node (which may be a text/element/attribute/fragment)
   * in some position relative to the existing node
   *
   * @param existingNode the existing node
   * @param newnode the node to insert
   * @return true on success
   * @throws Exception
   */
  private boolean insertNode(final Node existingNode, final Node newnode) throws Exception {

    // we first select on the position, and then determine
    // what to do based on the node types

    if (pos == Position.UNDER) {
      // place the new node under the selected one
      if (existingNode instanceof Document) {
        log("Building a root element", Project.MSG_VERBOSE);
        existingNode.appendChild(newnode);
      }
      else if (existingNode instanceof Element) {
        if (newnode instanceof Attr) {
          ((Element)existingNode).setAttributeNodeNS((Attr)newnode);
        }
        else {
          existingNode.appendChild(newnode);
        }
      }
      else if (existingNode instanceof Attr) {
        // we can insert into an attribute node, but only
        // from a text node (e.g. something from a buffer)
        if (newnode instanceof Text) {
          Attr existingAttr = (Attr)existingNode;
          existingAttr.setValue(newnode.getNodeValue());
        }
        else {
          System.err.println(newnode + " must be a text node to insert in an attribute");
          return false;
        }
      }
      else {
        System.err.println(existingNode + " not an element node");
        return false;
      }
    }
    if (pos == Position.BEFORE) {
      // place the new node before the current one
      Node parent = existingNode.getParentNode();
      if (parent == null) {
        System.err.println("Attempt to insert prior to root node");
        return false;
      }
      parent.insertBefore(newnode, existingNode);
    }
    if (pos == Position.AFTER) {
      // place the new node after the current one. Because
      // we can only use 'insertBefore', we find the next node
      // and then insert prior to that. We don't have to
      // worry about getNextSibling() returning null - see
      // the insertBefore() doc
      Node parent = existingNode.getParentNode();
      if (parent == null) {
        System.err.println("Attempt to insert after root node");
        return false;
        }
      parent.insertBefore(newnode, existingNode.getNextSibling());
    }
    return true;
  }

  /**
   * standard diagnostics
   */
  public String toString() {
    return "InsertAction(" + (doc2 == null ? (buffer == null ? "" : "buffer " + buffer) : doc2.getDocumentElement().toString()) +
           ", position [" + pos + "])";
  }
}

