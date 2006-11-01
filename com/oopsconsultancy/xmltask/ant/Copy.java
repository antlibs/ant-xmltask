package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;

/**
 * the Ant copy task. Note that Cut derives from this
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Copy implements Instruction {

  protected String path = null;

  protected String buffer = null;

  protected String property = null;

  protected boolean append = false;

  protected boolean attrValue = false;

  protected String ifProperty;

  protected String unlessProperty;

  /**
   * copies a nominated node to either a buffer or a property
   */
  public Copy() {
  }

  public void setBuffer(final String buffer) {
    this.buffer = buffer;
  }

  public void setProperty(final String property) {
    this.property = property;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public void setAttrValue(final String val) {
    if ("true".equals(val) || "on".equals(val)) {
      this.attrValue = true;
    }
  }

  public void process(final XmlTask task) {
    XmlReplace xmlReplace = null;
    if (path != null && buffer != null) {
      xmlReplace = new XmlReplace(path, new CopyAction(buffer, append, attrValue, task, false));
    }
    else if (path != null && property != null) {
      xmlReplace = new XmlReplace(path, new CopyAction(property, append, attrValue, task, true));
    }
    if (xmlReplace != null) {
      xmlReplace.setIf(ifProperty);
      xmlReplace.setUnless(unlessProperty);
      task.add(xmlReplace);
    }
  }

  public void setAppend(final String val) {
    if ("true".equals(val) || "on".equals(val)) {
      append = true;
    }
  }

  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
  }

  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;

  }
}
