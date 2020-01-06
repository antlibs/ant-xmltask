package com.oopsconsultancy.xmltask;

import com.oopsconsultancy.xmltask.ant.Param;
import com.oopsconsultancy.xmltask.ant.XmlTask;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import org.w3c.dom.Node;

import java.util.List;

/**
 * The nominated target is called for
 * each matched node
 *
 * Taken heavily from the CallTarget.java src
 * in the Ant source distribution
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class CallAction extends Action implements XPathAnalyserClient {

  private final String target;
  private final XmlTask task;
  private final boolean inheritAll;
  private final boolean inheritRefs;
  private final String buffer;
  private final List<Param> params;

  private Ant callee;

  public CallAction(final String target, final XmlTask task, final boolean inheritAll,
                    final boolean inheritRefs, final String buffer, final List<Param> params) {
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
    callee = (Ant) task.getProject().createTask("ant");
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
    for (Param param : params) {
      if (param.getPath() != null) {
        param.setValue(null);
      }
    }
  }

  public void applyNode(Node n, Object callback) {
    Param param = (Param) callback;
    param.set(task, n.getNodeValue());
  }

  public void applyNode(String str, Object callback) {
    Param param = (Param) callback;
    param.set(task, str);
  }

  /**
   * iterates through the parameters, executing the XPath
   * engine where necessary and creating new properties
   * in the sub target, then calls on that.
   *
   * @param node Node
   * @return success
   * @throws Exception if something goes wrong
   */
  public boolean apply(Node node) throws Exception {
    init();
     resetParams();

    log("Calling target " + target + " for " + node
        + (buffer != null ? " (in buffer " + buffer : ""), Project.MSG_VERBOSE);

    if (buffer != null) {
      // record the complete (sub)node in the nominated buffer
      BufferStore.set(buffer, node, false, task);
    }

    if (params != null) {
      for (Param param : params) {
        if (param.getPath() != null) {
          XPathAnalyser xpa = task == null ? XPathAnalyserFactory.getAnalyser()
              : XPathAnalyserFactory.getAnalyser(task.getXpathFactory(), task.getXpathObjectModelUri());
          xpa.registerClient(this, param);
          xpa.analyse(node, param.getPath());
        }

        // now set the values
        if (param.getValue() != null) {
          Property p = callee.createProperty();
          p.setName(param.getName());
          p.setValue(param.getValue());
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

    // make sure we always pass the buffers!
    Ant.Reference buffers = new Ant.Reference();
    buffers.setProject(task.getProject());
    buffers.setRefId(BufferStore.BUFFERS_PROJECT_REF);
    buffers.setToRefid(BufferStore.BUFFERS_PROJECT_REF);
    callee.addReference(buffers);

    callee.execute();

    return true;
  }

  /**
   * builds a representation of the node hierarchy
   *
   * @param node Node
   * @param qualified boolean
   * @return the local or fully-qualified name
   */
  private String getNodePath(Node node, final boolean qualified) {
    StringBuilder op = new StringBuilder();
    while (node != null && node.getParentNode() != null) {
      if (node.getNodeType() != Node.TEXT_NODE) {
        op.insert(0, "/" + (qualified ? node.getLocalName() : node.getNodeName()));
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
    } else {
      System.out.println(msg);
    }
  }
}
