package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import org.apache.tools.ant.*;

/**
 * Performs modification of the attributes for the
 * selected nodes
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class AttrAction extends Action {

  private String attr = null;
  private String value = null;
  private Boolean remove = null;
  private Task task = null;

  public AttrAction(String attr, String value, Boolean remove, Task task) {
    this.attr = attr;
    this.value = value;
    this.remove = remove;
    this.task = task;
  }

  /**
   * modifies the attribute (or adds one if it exists). If the
   * node can't have an attribute, then this is reported and the
   * task exits
   *
   * @param node
   * @throws Exception
   */
  public boolean apply(Node node) throws Exception {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      if (remove == Boolean.TRUE) {
        ((Element)node).removeAttribute(attr);
      }
      else {
        ((Element)node).setAttribute(attr, value);
      }
      return true;
    }
    else {
      log(node + " can't have any attributes");
      return false;
    }
  }

  private void log(String msg) {
    if (task != null) {
      task.log(msg);
    }
    else {
      System.out.println(msg);
    }
  }

  public String toString() {
    return "AttrReplace(" + attr + "=" + value + ", remove=" +(remove == Boolean.TRUE ? "yes":"no")+")";
  }
}


