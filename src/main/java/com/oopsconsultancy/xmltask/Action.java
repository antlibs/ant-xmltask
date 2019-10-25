package com.oopsconsultancy.xmltask;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * the basic abstraction of an xml action
 * eg. text modification, xml insertion etc.
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public abstract class Action {

  /**
   * the list of nodes to remove once modifications have been applied
   */
  private List removals = new ArrayList();

  /**
   * the document to work on
   */
  protected Document doc = null;

  /**
   * sets the document to process
   *
   * @param doc
   */
  public void setDocument(Document doc) {
    this.doc = doc;
  }

  public Document getDocument() {
    return doc;
  }

  /**
   * records a node to remove once all modifications
   * have occurred
   *
   * @param n
   */
  protected void remove(Node n) {
    removals.add(n);
  }

  /**
   * called once modifications have occurred. All registered
   * nodes for removal are disconnected before the next XPath
   * match is processed
   */
  protected final void complete() {
      completeAction();
    for (int r = 0; r < removals.size(); r++) {
      Node rn = (Node)removals.get(r);
      if (rn.getNodeType() == Node.ATTRIBUTE_NODE) {
        Attr attr = (Attr)rn;
        Element element = attr.getOwnerElement();
        element.removeAttributeNode(attr);
      }
      else {
        rn.getParentNode().removeChild(rn);
      }
    }
    removals = new ArrayList();
  }

  /**
   * an action completion. Provided for actions to perform clean up
   * etc.
   */
  protected void completeAction() {
      
  }
  public abstract boolean apply(Node node) throws Exception;
}

