package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import org.apache.tools.ant.*;

/**
 * cuts the nominated node and its children
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class CutAction extends CopyAction {

  public CutAction(String buffer, boolean append, boolean attrValue, Task task) {
    super(buffer, append, attrValue, task);
  }

  public boolean apply(Node node) throws Exception {
    record(node);
    remove(node);
    return true;
  }

  public String toString() {
    return "CutAction(" + buffer + ")";
  }
}

