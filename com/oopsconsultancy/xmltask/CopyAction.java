package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import org.apache.tools.ant.*;

/**
 * copies the nominated node and its children. We can copy the
 * node to a buffer or to a system property
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class CopyAction extends Action {

  final protected String buffer;
  final protected boolean append;
  final protected boolean attrValue;
  final protected Task task;
  final protected boolean isProperty;

  public CopyAction(String buffer, boolean append, boolean attrValue, Task task, boolean isProperty) {
    this.buffer = buffer;
    this.append = append;
    this.attrValue = attrValue;
    this.task = task;
    this.isProperty = isProperty;
  }

  /**
   * records the selected node. For attributes we can
   * record the value as a text object if required
   *
   * @param node
   * @throws Exception
   */
  protected void record(Node node) throws Exception {
    if (node instanceof Attr && attrValue) {
      node = node.getOwnerDocument().createTextNode(((Attr)node).getValue());
    }

    if (!isProperty) {
      BufferStore.set(buffer, node, append, task);
    }
    else {
      // this currently supports only text nodes
      if (node instanceof Text) {
        task.getProject().setNewProperty(buffer, node.getNodeValue());
      }
    }
  }

  public boolean apply(Node node) throws Exception {
    record(node);
    return true;
  }

  public String toString() {
    return "CopyAction(" + buffer + ")";
  }
}
