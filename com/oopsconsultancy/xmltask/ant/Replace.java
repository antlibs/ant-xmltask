package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;
import org.apache.tools.ant.*;
import java.io.*;

/**
 * the Ant replacement task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Replace {

  private XmlTask task = null;
  private Action action = null;
  private String path = null;
  private boolean expandProperties = true;

  public void setPath(String path) {
    this.path = path;
    register();
  }
  public void setWithtext(String to) throws Exception {
    action = new TextAction(to);
    register();
  }
  public void setWithxml(String to) throws Exception {
    action = XmlAction.xmlActionfromString(to, task);
    register();
  }

  public void setExpandProperties(final boolean expandProperties) {
    this.expandProperties = expandProperties;
    register();
  }

  /**
   * used to insert literal text placed within the build.xml under
   * the replace element
   *
   * @param text
   * @throws Exception
   */
  public void addText(String text) throws Exception {
    if (expandProperties) {
      text = ProjectHelper.replaceProperties(task.getProject(), text, task.getProject().getProperties());
    }
    action = XmlAction.xmlActionfromString(text, task);
    register();
  }

  public void setWithfile(File to) throws Exception {
    action = XmlAction.xmlActionfromFile(to, task);
    register();
  }
  public void setWithBuffer(String buffer) throws Exception {
    action = XmlAction.xmlActionfromBuffer(buffer, task);
    register();
  }

  private void register() {
    if (path != null && action != null) {
      task.add(new XmlReplace(path, action));
    }
  }

  public Replace(XmlTask task) {
    this.task = task;
  }
}


