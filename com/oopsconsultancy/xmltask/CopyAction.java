package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import org.apache.tools.ant.*;

/**
 * copies the nominated node and its children
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class CopyAction extends Action {

  final protected String buffer;
  final protected boolean append;
  final protected boolean attrValue;
  final protected Task task;

  public CopyAction(String buffer, boolean append, boolean attrValue, Task task) {
    this.buffer = buffer;
    this.append = append;
    this.attrValue = attrValue;
    this.task = task;
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
    BufferStore.set(buffer, node, append, task);
  }

  public boolean apply(Node node) throws Exception {
    record(node);
    return true;
  }

  public String toString() {
    return "CopyAction(" + buffer + ")";
  }
}
