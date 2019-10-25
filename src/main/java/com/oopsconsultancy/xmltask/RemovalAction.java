package com.oopsconsultancy.xmltask;

import org.w3c.dom.Node;

/** 
 * removes the nominated node and its children
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class RemovalAction extends Action {

  public RemovalAction() {
  }

  public boolean apply(Node node) throws Exception {
    remove(node);
    return true;
  }

  public String toString() {
    return "RemovalAction()";
  }
}

