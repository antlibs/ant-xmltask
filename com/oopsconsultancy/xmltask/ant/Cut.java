package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;

/**
 * the Ant cut task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Cut extends Copy {

  public Cut() {
  }

  /**
   * cuts a nominated node and copies to either a
   * buffer or a property
   */
  protected void process(XmlTask task) {
    if (path != null && buffer != null) {
      task.add(new XmlReplace(path, new CutAction(buffer, append, attrValue, task, false)));
    }
    else if (path != null && property != null) {
      task.add(new XmlReplace(path, new CutAction(property, append, attrValue, task, true)));
    }
  }
}
