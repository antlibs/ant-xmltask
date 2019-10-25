package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.RenameAction;
import com.oopsconsultancy.xmltask.XmlReplace;

/**
 * the Ant rename task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Rename implements Instruction {

  private XmlTask task = null;

  private String path = null;
  private String to = null;

  private String ifProperty;

  private String unlessProperty;

  public void setPath(String path) {
    this.path = path;
  }
  public void setTo(String to) {
    this.to = to;
  }

  private void register() {
    if (path != null && to != null) {
      XmlReplace xmlReplace = new XmlReplace(path, new RenameAction(to));
      xmlReplace.setIf(ifProperty);
      xmlReplace.setUnless(unlessProperty);
      task.add(xmlReplace);
    }
  }

  public void process(final XmlTask task) {
    this.task = task;
    register();
  }

  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
  }

  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;
  }
}
