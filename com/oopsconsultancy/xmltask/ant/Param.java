package com.oopsconsultancy.xmltask.ant;

import org.apache.tools.ant.taskdefs.*;

/**
 * name + value (XPath expression) for the call instruction. The
 * value can be specified instead of the XPath to provide non-XML
 * config
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

  public void setName(final String name) {
    this.name = name;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public void setValue(final String value) {
    this.value = value;
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

  public void set(final XmlTask task, final String value) {
    this.value = value;
    task.getProject().setNewProperty(name, value);
  }

  public String toString() {
    return getName() + " (" + getPath() + ")";
  }
}
