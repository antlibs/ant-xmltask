package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import com.oopsconsultancy.xmltask.ant.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

/**
 * The nominated target is called for
 * each matched node
 *
 * Taken heavily from the CallTarget.java src
 * in the Ant source distribution
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class CallAction extends Action {

  private final String target;
  private final XmlTask task;
  private final boolean inheritAll;
  private final boolean inheritRefs;
  private final String buffer;

  private Ant callee;

  public CallAction(String target, XmlTask task, boolean inheritAll, boolean inheritRefs, String buffer) {
    this.target = target;
    this.task = task;
    this.inheritAll = inheritAll;
    this.inheritRefs = inheritRefs;
    this.buffer = buffer;
  }

  /**
   * init this task by creating new instance of the ant task and
   * configuring it's by calling its own init method.
   */
  public void init() {
    callee = (Ant)task.getProject().createTask("ant");
    callee.setOwningTarget(task.getOwningTarget());
    callee.setTaskName(task.getTaskName());
    callee.setLocation(task.getLocation());
    callee.init();
  }

  public boolean apply(Node node) throws Exception {
    if (callee == null) {
      init();
    }
    log("Calling target " + target + " for " + node + (buffer != null ? " (in buffer "+buffer:""), Project.MSG_VERBOSE);

    if (buffer != null) {
      BufferStore.set(buffer, node, false, task);
    }

    callee.setAntfile(task.getProject().getProperty("ant.file"));
    callee.setTarget(target);
    callee.setInheritAll(inheritAll);
    callee.setInheritRefs(inheritRefs);
    callee.execute();
    return true;
  }

  public String toString() {
    return "CallAction(" + target + ")";
  }

  private void log(String msg, int level) {
    if (task != null) {
      task.log(msg, level);
    }
    else {
      System.out.println(msg);
    }
  }
}
