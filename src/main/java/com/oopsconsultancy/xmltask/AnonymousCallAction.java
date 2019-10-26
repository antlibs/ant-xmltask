package com.oopsconsultancy.xmltask;

import com.oopsconsultancy.xmltask.ant.Param;
import com.oopsconsultancy.xmltask.ant.XmlTask;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;
import org.w3c.dom.Node;

import java.util.List;

/**
 * The defined macro is called for each matched node
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class AnonymousCallAction extends Action implements XPathAnalyserClient {

  private final XmlTask task;
  private final String buffer;
  private final List<Param> params;

  private MacroDef macro;

  public AnonymousCallAction(final MacroDef macro, final XmlTask task,
                             final String buffer, final List<Param> params) {
    this.macro = macro;
    this.task = task;
    this.buffer = buffer;
    this.params = params;

    init();
  }

  /**
   * init this task.
   */
  private void init() {
    for (Param param : params) {
      MacroDef.Attribute attribute = new MacroDef.Attribute();
      attribute.setName(param.getName());
      macro.addConfiguredAttribute(attribute);
    }
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
   * engine where necessary and creating new attributes
   * in the macro instance, then calls on that.
   *
   * @param node Node
   * @return success
   * @throws Exception if something goes wrong
   */
  public boolean apply(Node node) throws Exception {
    resetParams();

    log("Calling internal macro for " + node
        + (buffer != null ? " (in buffer " + buffer : ""), Project.MSG_VERBOSE);

    if (buffer != null) {
      // record the complete (sub)node in the nominated buffer
      BufferStore.set(buffer, node, false, task);
    }

    MacroInstance instance = new MacroInstance();
    instance.setProject(task.getProject());
    instance.setOwningTarget(task.getOwningTarget());
    instance.setMacroDef(macro);

    if (params != null) {
      for (Param param : params) {
        if (param.getPath() != null) {
          XPathAnalyser xpa = XPathAnalyserFactory.getAnalyser();
          xpa.registerClient(this, param);
          xpa.analyse(node, param.getPath());
        }

        // now set the value
        instance.setDynamicAttribute(param.getName().toLowerCase(), param.getValue());
      }
    }

    instance.execute();

    return true;
  }

  private void log(String msg, int level) {
    if (task != null) {
      task.log(msg, level);
    } else {
      System.out.println(msg);
    }
  }
}
