package com.oopsconsultancy.xmltask.ant;
 
import com.oopsconsultancy.xmltask.*;

/** 
 * the Ant rename task
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Rename implements Instruction {

  private XmlTask task = null;

  private String path = null;
  private String to = null;

  public void setPath(String path) {
    this.path = path;
  }
  public void setTo(String to) {
    this.to = to;
  }

  void register() {
    if (path != null && to != null) {
      task.add(new XmlReplace(path, new RenameAction(to)));
    }  
  }

  public void process(final XmlTask task) {
    this.task = task;
    register();
  }
}

