package com.oopsconsultancy.xmltask.ant;

import java.io.*;
import com.oopsconsultancy.xmltask.*;
import org.apache.tools.ant.*;

/**
 * the Ant insertion task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Insert {

  private XmlTask task = null;

  private String path = null;
  private InsertAction action = null;
  private InsertAction.Position position = InsertAction.Position.UNDER;
  private boolean expandProperties = true;

  public void setPath(String path) {
    this.path = path;
    register();
  }

  public void setPosition(String pos) {
    if ("before".equals(pos)) {
      position = InsertAction.Position.BEFORE;
    }
    else if ("after".equals(pos)) {
      position = InsertAction.Position.AFTER;
    }
    else if ("under".equals(pos)) {
      position = InsertAction.Position.UNDER;
    }
    else {
      log("Don't recognise position '" + pos + "'", Project.MSG_WARN);
    }
    if (action != null) {
      action.setPosition(position);
    }
  }

  private void log(String msg, int level) {
    if (task != null) {
      task.log(msg, level);
    }
    else {
      System.out.println(msg);
    }
  }

  public void setXml(String to) throws Exception {
    action = InsertAction.fromString(to, task);
    register();
  }

  public void setFile(File to) throws Exception {
    action = InsertAction.fromFile(to, task);
    register();
  }

  public void setExpandProperties(final boolean expandProperties) {
    this.expandProperties = expandProperties;
    register();
  }

  /**
   * used to insert literal text placed within the build.xml under
   * the insert element
   *
   * @param text
   * @throws Exception
   */
  public void addText(String text) throws Exception {
    if (expandProperties) {
      // we expand properties by default...
      text = ProjectHelper.replaceProperties(task.getProject(), text, task.getProject().getProperties());
    }
    action = InsertAction.fromString(text, task);
    register();
  }

  public void setBuffer(String buffer) throws Exception {
    action = InsertAction.fromBuffer(buffer, task);
    register();
  }
  private void register() {
    if (action != null && path != null) {
      action.setPosition(position);
      task.add(new XmlReplace(path, action));
    }
  }

  public Insert(XmlTask task) {
    this.task = task;
  }
}

