package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.UncommentAction;
import com.oopsconsultancy.xmltask.XmlReplace;

/**
 * @author brian performs an uncomment action
 */
public class Uncomment {

  private String path;

  public Uncomment() {
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public void process(final XmlTask task) {
    task.add(new XmlReplace(path, new UncommentAction()));
  }
}
