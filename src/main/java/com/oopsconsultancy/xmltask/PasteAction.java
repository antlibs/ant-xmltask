package com.oopsconsultancy.xmltask;

import org.w3c.dom.Node;

/**
 * pastes a buffer below the nominated node and its children
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class PasteAction extends Action {

  private String buffer;

  public PasteAction(String buffer) {
    this.buffer = buffer;
  }

  public boolean apply(Node node) throws Exception {
    return true;
  }

  public String toString() {
    return "PasteAction(" + buffer + ")";
  }
}
