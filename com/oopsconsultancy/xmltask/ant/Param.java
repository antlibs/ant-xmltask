package com.oopsconsultancy.xmltask.ant;

import org.apache.tools.ant.taskdefs.*;

/**
 * name + value (XPath expression) for the call instruction
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Param {

  private String name;
  private String path;
  private String value;

  public Param() {
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getValue() {
    return value;
  }

  public String getPath() {
    return path;
  }

  public String getName() {
    return name;
  }

  public void set(XmlTask task, String value) {
    this.value = value;
    task.getProject().setNewProperty(name, value);
  }
}
