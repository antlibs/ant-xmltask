package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.AnonymousCallAction;
import com.oopsconsultancy.xmltask.CallAction;
import com.oopsconsultancy.xmltask.XmlReplace;
import org.apache.tools.ant.taskdefs.MacroDef;

import java.util.ArrayList;
import java.util.List;

/**
 * the Ant call task
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class Call implements Instruction {

  private String path = null;

  private String target = null;

  private String buffer = null;

  private boolean inheritAll = true;

  private boolean inheritRefs = false;

  private List params = new ArrayList();

  private MacroDef macro;

  private String unlessProperty;

  private String ifProperty;

  /**
   * executes a target for a set of nodes
   */
  public Call() {
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setBuffer(String buffer) {
    this.buffer = buffer;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setInheritAll(boolean inheritAll) {
    this.inheritAll = inheritAll;
  }

  public void setInheritRefs(boolean inheritRefs) {
    this.inheritRefs = inheritRefs;
  }

  public void addConfiguredParam(Param param) {
    params.add(param);
  }

  public Object createActions() {
    macro = new MacroDef();
    return macro.createSequential();
  }

  public void process(final XmlTask task) {
    XmlReplace xmlReplace = null;
    if (path != null && target != null) {
      xmlReplace = new XmlReplace(path, new CallAction(target, task, inheritAll, inheritRefs, buffer, params));
    }
    else if (path != null && macro != null) {
      xmlReplace = new XmlReplace(path, new AnonymousCallAction(macro, task, buffer, params));
    }
    if (xmlReplace != null) {
      xmlReplace.setIf(ifProperty);
      xmlReplace.setUnless(unlessProperty);
      task.add(xmlReplace);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.oopsconsultancy.xmltask.ant.Instruction#setIf(java.lang.String)
   */
  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;

  }

  /*
   * (non-Javadoc)
   *
   * @see com.oopsconsultancy.xmltask.ant.Instruction#setUnless(java.lang.String)
   */
  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;

  }
}
