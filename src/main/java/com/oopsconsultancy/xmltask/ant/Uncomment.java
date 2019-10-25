package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.UncommentAction;
import com.oopsconsultancy.xmltask.XmlReplace;

/**
 * @author brian performs an uncomment action
 */
public class Uncomment implements Instruction {

  private String path;
  private String unlessProperty;
  private String ifProperty;

  public Uncomment() {
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public void process(final XmlTask task) {
    XmlReplace xmlReplace = new XmlReplace(path, new UncommentAction());
    xmlReplace.setIf(ifProperty);
    xmlReplace.setUnless(unlessProperty);
    task.add(xmlReplace);
  }

  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
    
  }

  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;
    
  }
}
