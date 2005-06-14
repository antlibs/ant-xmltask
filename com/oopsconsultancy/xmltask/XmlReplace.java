package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.apache.tools.ant.*;

/**
 * performs the basic task of identifying the qualifying
 * XML nodes via XPath, and then performing an action
 * (e.g. replacement, removal, insertion) on that node.
 * We use the callback mechanism to record the nodes of
 * interest, and once that's completed, we then invoke on
 * each node. This is to prevent infinite loops
 * (see test #95)
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XmlReplace implements XPathAnalyserClient {

  private final String path;
  private final Action action;
  private Task task = null;
  private final List nodes = new ArrayList();

  public XmlReplace(final String path, final Action action) {
    this.path = path;
    this.action = action;
  }

  public void setTask(final Task task) {
    this.task = task;
  }

  private void log(final String msg, final int level) {
    // task may not be set sometimes (e.g. during unit tests)
    if (task != null) {
      task.log(msg, level);
    }
    else {
      System.out.println(msg);
    }
  }

  public int apply(final Document doc) throws Exception {
    log("Applying " + action + " to " + path, Project.MSG_VERBOSE);

    action.setDocument(doc);

    List removals = new ArrayList();

    XPathAnalyser xpa = XPathAnalyserFactory.getAnalyser();
    xpa.registerClient(this, null);

    // clear the nodes from the last invocation
    nodes.clear();
    int count = xpa.analyse(doc, path);

    // and iterate through the nodes returned via the callbacks.
    // We do this otherwise we could get in nasty loop situations
    // with repeated matches, inserts and matches on *those* inserts
    for (Iterator i = nodes.iterator(); i.hasNext(); ) {
      action.apply((Node)i.next());
    }

    log("Applied " + action + " - " + count + " match(es)", Project.MSG_VERBOSE);
    action.complete();
    return count;
  }

  public String toString() {
    return action.toString() + " (" + path + ")";
  }


  /**
   * called by the XPathAnalyser implementations
   *
   * @param n
   * @param callback
   * @throws Exception
   */
  public void applyNode(final Node n, final Object callback) throws Exception {
    nodes.add(n);
  }

  /**
   * called by the XPathAnalyser implementations
   *
   * @param str
   * @param callback
   * @throws Exception
   */
  public void applyNode(final String str, final Object callback) throws Exception {
    nodes.add(action.getDocument().createTextNode(str));
  }

}

