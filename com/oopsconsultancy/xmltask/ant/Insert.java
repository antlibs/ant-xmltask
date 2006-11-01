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
public class Insert implements Instruction {

  private XmlTask task = null;

  private String path = null;

  private String text = null; // text to insert (can be null)

  private InsertAction action = null;

  private InsertAction.Position position = InsertAction.Position.UNDER;

  private boolean expandProperties = true;

  /**
   * the buffer to insert
   */
  private String buffer;

  /**
   * the raw XML to insert
   */
  private String xml;

  /**
   * the file to insert
   */
  private File file;

  private String ifProperty;

  private String unlessProperty;

  public void setPath(String path) {
    this.path = path;
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

  private void log(final String msg, final int level) {
    if (task != null) {
      task.log(msg, level);
    }
    else {
      System.out.println(msg);
    }
  }

  public void setXml(final String xml) throws Exception {
    this.xml = xml;
  }

  public void setFile(final File file) throws Exception {
    this.file = file;
  }

  public void setExpandProperties(final boolean expandProperties) {
    this.expandProperties = expandProperties;
  }

  /**
   * used to insert literal text placed within the build.xml under the insert
   * element
   * 
   * @param text
   * @throws Exception
   */
  public void addText(final String text) throws Exception {
    this.text = text;
  }

  public void setBuffer(final String buffer) throws Exception {
    this.buffer = buffer;
  }

  private void register() {
    try {
      if (xml != null) {
		    action = InsertAction.fromString(xml, task);
      }
      if (file != null) {
		    action = InsertAction.fromFile(file, task);
      }
      else if (buffer != null) {
        action = InsertAction.fromBuffer(buffer, task);
      }
      else if (text != null) {
        if (expandProperties) {
          // we expand properties by default...
          text = ProjectHelper.replaceProperties(task.getProject(), text, task.getProject().getProperties());
        }
        action = InsertAction.fromString(text, task);
      }
    }
    catch (Exception e) {
      throw new BuildException("Failed to add text to insert/paste", e);
    }
    if (action != null && path != null) {
      action.setPosition(position);
      XmlReplace xmlReplace = new XmlReplace(path, action);
      xmlReplace.setIf(ifProperty);
      xmlReplace.setUnless(unlessProperty);
      task.add(xmlReplace);
    }
  }

  public void process(final XmlTask task) {
    this.task = task;
    register();
  }

  /* (non-Javadoc)
   * @see com.oopsconsultancy.xmltask.ant.Instruction#setIf(java.lang.String)
   */
  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
  }

  /* (non-Javadoc)
   * @see com.oopsconsultancy.xmltask.ant.Instruction#setUnless(java.lang.String)
   */
  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;
  }
}
