package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;
import com.oopsconsultancy.xmltask.ant.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.w3c.dom.traversal.*;

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
public class CallAction extends Action implements XPathAnalyserClient {

  private final String target;
  private final XmlTask task;
  private final boolean inheritAll;
  private final boolean inheritRefs;
  private final String buffer;
  private final List params;

  private Ant callee;

  public CallAction(final String target, final XmlTask task, final boolean inheritAll, final boolean inheritRefs, final String buffer, final List params) {
    this.target = target;
    this.task = task;
    this.inheritAll = inheritAll;
    this.inheritRefs = inheritRefs;
    this.buffer = buffer;
    this.params = params;
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

  /**
   * reset the set of parameters. We only reset XPath settings
   * since properties will remain the same between invocations
   */
  private void resetParams() {
    for (Iterator i = params.iterator(); i.hasNext(); ) {
      Param param = (Param)i.next();
      if (param.getPath() != null) {
        param.setValue(null);
      }
    }
  }

  public void applyNode(Node n, Object callback) {
    Param param = (Param)callback;
    param.set(task, n.getNodeValue());
  }

  public void applyNode(String str, Object callback) {
    Param param = (Param)callback;
    param.set(task, str);
  }

  public boolean apply(Node node) throws Exception {
     if (callee == null) {
    init();
     }
     resetParams();

    log("Calling target " + target + " for " + node + (buffer != null ? " (in buffer "+buffer:""), Project.MSG_VERBOSE);

    if (buffer != null) {
      // record the complete (sub)node in the nominated buffer
      BufferStore.set(buffer, node, false, task);
    }

    if (params != null) {
      for (Iterator i = params.iterator(); i.hasNext(); ) {
        Param param = (Param)i.next();

        if (param.getPath() != null) {
          XPathAnalyser xpa = XPathAnalyserFactory.getAnalyser();
          xpa.registerClient(this, param);
          xpa.analyse(node, param.getPath());
        }

        // now set the values
        String val = param.getValue();
        Property p = callee.createProperty();
        p.setName(param.getName());
        if (val != null) {
          p.setValue(param.getValue());
        }
        else {
          p.setValue("${" +param.getName() + "}");
        }
      }
    }
    // record the path in special named properties. These are currently
    // undocumented and may disappear!
    String nodeStr = getNodePath(node, false);
    String fqnodeStr = getNodePath(node, true);
    Property p = callee.createProperty();
    p.setName("xmltask.path");
    p.setValue(nodeStr);
    p = callee.createProperty();
    p.setName("xmltask.fqpath");
    p.setValue(fqnodeStr);


    callee.setAntfile(task.getProject().getProperty("ant.file"));
    callee.setTarget(target);
    callee.setInheritAll(inheritAll);
    callee.setInheritRefs(inheritRefs);
    callee.execute();

    return true;
  }

  /**
   * builds a representation of the node hierarchy
   *
   * @param node
   * @param qualified
   * @return the local or fully-qualified name
   */
  private String getNodePath(Node node, final boolean qualified) {
    // stringbuffer not good for appending, so...
    String op = "";
    while (node != null && node.getParentNode() != null) {
      if (node.getNodeType() != Node.TEXT_NODE) {
        op = "/" + (qualified ? node.getLocalName() : node.getNodeName()) + op;
      }
      node = node.getParentNode();
    }
    return op.toString();
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
