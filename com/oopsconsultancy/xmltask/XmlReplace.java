package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.apache.tools.ant.*;

/**
 * performs the basic task of identifying the qualifying
 * XML nodes via XPath, and then performing an action
 * (e.g. replacement, removal, insertion) on that node
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XmlReplace implements XPathAnalyserClient {

  private String path = null;
  private Action action = null;
  private Task task = null;

  public XmlReplace(String path, Action action) {
    this.path = path;
    this.action = action;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  private void log(String msg, int level) {
    // task may not be set sometimes (e.g. during unit tests)
    if (task != null) {
      task.log(msg, level);
    }
    else {
      System.out.println(msg);
    }
  }

  public int apply(Document doc) throws Exception {
    log("Applying " + action + " to " + path, Project.MSG_VERBOSE);

    action.setDocument(doc);

    List removals = new ArrayList();

    XPathAnalyser xpa = XPathAnalyserFactory.getAnalyser();
    xpa.registerClient(this, null);
    int count = xpa.analyse(doc, path);

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
   */
  public void applyNode(Node n, Object callback) throws Exception {
    action.apply(n);
  }

  /**
   * called by the XPathAnalyser implementations
   *
   * @param n
   */
  public void applyNode(String str, Object callback) throws Exception {
    action.apply(action.getDocument().createTextNode(str));
  }

}

