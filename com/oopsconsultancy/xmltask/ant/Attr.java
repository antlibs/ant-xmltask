package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;

/**
 * the Ant attribute modification task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class Attr {

  private XmlTask task = null;

  private String path = null;
  private String attr = null;
  private String value = null;
  private Boolean remove = null;

  public void setPath(String path) {
    this.path = path;
    register();
  }

  public void setAttr(String attr) {
    this.attr = attr;
    register();
  }

  public void setValue(String value) {
    this.value = value;
    register();
  }

  public void setRemove(String remove) {
    if ("true".equals(remove) || "yes".equals(remove)) {
      this.remove = Boolean.TRUE;
    }
    else {
      this.remove = Boolean.FALSE;
    }
    register();
  }

  private void register() {
    if ((value != null || remove != null) && attr != null && path != null) {
      task.add(new XmlReplace(path, new AttrAction(attr, value, remove, task)));
    }
  }

  public Attr(XmlTask task) {
    this.task = task;
  }
}

