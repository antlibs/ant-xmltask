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
   * cuts a nominated node and copies to either a buffer or a property
   */
  public void process(final XmlTask task) {
    XmlReplace xmlReplace = null;
    if (path != null && buffer != null) {
      xmlReplace = new XmlReplace(path, new CutAction(buffer, append, attrValue, task, false, trim, propertySeparator));
    }
    else if (path != null && property != null) {
      xmlReplace = new XmlReplace(path, new CutAction(property, append, attrValue, task, true, trim, propertySeparator));
    }
    if (xmlReplace != null) {
      xmlReplace.setIf(ifProperty);
      xmlReplace.setUnless(unlessProperty);
      task.add(xmlReplace);
    }
  }
}
