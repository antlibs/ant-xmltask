package com.oopsconsultancy.xmltask.ant;
 
import com.oopsconsultancy.xmltask.*;

/** 
 * the Ant rename task
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class Rename {

  private XmlTask task = null;

  private String path = null;
  private String to = null;

  public void setPath(String path) {
    this.path = path;
    process();
  }
  public void setTo(String to) {
    this.to = to;
    process();
  }

  void process() {
    if (path != null && to != null) {
      task.add(new XmlReplace(path, new RenameAction(to)));
    }  
  }

  public Rename(XmlTask task) {
    this.task = task;
  }
}

