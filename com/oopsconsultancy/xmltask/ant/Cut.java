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

  protected void process(XmlTask task) {
    if (path != null && buffer != null) {
      task.add(new XmlReplace(path, new CutAction(buffer, append, attrValue, task)));
    }
  }
}
