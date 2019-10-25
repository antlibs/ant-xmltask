package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.RemovalAction;
import com.oopsconsultancy.xmltask.XmlReplace;

/**
 * the Ant removal task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Remove implements Instruction {

  private String path = null;

  private String ifProperty;

  private String unlessProperty;

  public void setPath(String path) {
    this.path = path;
  }

  public void process(final XmlTask task) {
    XmlReplace xmlReplace = new XmlReplace(path, new RemovalAction());
    xmlReplace.setIf(ifProperty);
    xmlReplace.setUnless(unlessProperty);
    task.add(xmlReplace);
  }

  /**
   * sets a property determining execution
   *
   * @param ifProperty String
   */
  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
  }

  /**
   * sets a property determining execution
   *
   * @param unlessProperty String
   */
  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;
  }
}
