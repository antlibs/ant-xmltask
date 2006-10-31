package com.oopsconsultancy.xmltask.ant;
 
import com.oopsconsultancy.xmltask.*;

/** 
 * the Ant removal task
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Remove implements Instruction {

  private String path = null;

  public void setPath(String path) {
    this.path = path;
  }


  public void process(final XmlTask task) {
    task.add(new XmlReplace(path, new RemovalAction()));
  }
}

