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

  public CopyAction(final String buffer, final boolean append, final boolean attrValue, final Task task, final boolean isProperty) {
    this.buffer = buffer;
    this.append = append;
    this.attrValue = attrValue;
    this.task = task;
    this.isProperty = isProperty;
    if (attrValue && isProperty) {
    	task.log("Specifying 'attr' for properties is now deprecated", Project.MSG_VERBOSE);
    }
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
    	// this is only required for buffers...
      Document doc = node.getOwnerDocument();
      String value = ((Attr)node).getValue();
      node = doc.createTextNode(value);
    }

    if (!isProperty) {
      BufferStore.set(buffer, node, append, task);
    }
    else {
      // this currently supports only text and comment nodes
      if (node instanceof Text || node instanceof Comment || node instanceof Attr) {
    	task.log("Copying '"+ node.getNodeValue()+"' to property " + buffer, Project.MSG_VERBOSE);
        task.getProject().setNewProperty(buffer, node.getNodeValue());
      }
      else if (node == null) {
        task.log("Can only copy/cut text() nodes and attribute values to properties (found no node)", Project.MSG_WARN);
      }
      else {
        task.log("Can only copy/cut text() nodes and attribute values to properties (found "+node.getClass().getName()+")", Project.MSG_WARN);
      }
      if (append) {
        task.log("Cannot append values to properties", Project.MSG_WARN);
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
