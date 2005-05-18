package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;

/**
 * the Ant copy task. Note that Cut derives from this
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Copy {

  protected String path = null;
  protected String buffer = null;
  protected boolean append = false;
  protected boolean attrValue = false;

  public Copy() {
  }

  public void setBuffer(String buffer) {
    this.buffer = buffer;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setAttrValue(String val) {
    if ("true".equals(val) || "on".equals(val)) {
      this.attrValue = true;
    }
  }

  protected void process(XmlTask task) {
    if (path != null && buffer != null) {
      task.add(new XmlReplace(path, new CopyAction(buffer, append, attrValue, task)));
    }
  }

  public void setAppend(String val) {
    if ("true".equals(val) || "on".equals(val)) {
      append = true;
    }
  }
}
