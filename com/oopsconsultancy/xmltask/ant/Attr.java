package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;

/**
 * the Ant attribute modification task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Attr implements Instruction {

  private XmlTask task = null;

  private String path = null;
  private String attr = null;
  private String value = null;
  private Boolean remove = null;

  public void setPath(String path) {
    this.path = path;
  }

  public void setAttr(String attr) {
    this.attr = attr;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setRemove(String remove) {
    if ("true".equals(remove) || "yes".equals(remove)) {
      this.remove = Boolean.TRUE;
    }
    else {
      this.remove = Boolean.FALSE;
    }
  }

  private void register() {
    if ((value != null || remove != null) && attr != null && path != null) {
      task.add(new XmlReplace(path, new AttrAction(attr, value, remove, task)));
    }
  }

  public void process(final XmlTask task) {
    this.task = task;
    register();
  }
}

