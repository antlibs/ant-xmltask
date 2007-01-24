package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import org.apache.tools.ant.*;

/**
 * cuts the nominated node and its children
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class CutAction extends CopyAction {

  public CutAction(final String buffer, final boolean append, final boolean attrValue, final Task task, final boolean isProperty) {
    super(buffer, append, attrValue, task, isProperty);
  }

  public boolean apply(final Node node) throws Exception {
    record(node);
    remove(node);
    return true;
  }

  public String toString() {
    return "CutAction(" + buffer + ")";
  }
}

