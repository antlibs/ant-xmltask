package com.oopsconsultancy.xmltask.ant;
 
import com.oopsconsultancy.xmltask.*;

/** 
 * the Ant removal task
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class Remove {

  private XmlTask task = null;

  private String path = null;

  public void setPath(String path) {
    this.path = path;
    task.add(new XmlReplace(path, new RemovalAction()));
  }

  public Remove(XmlTask task) {
    this.task = task;
  }
}

