package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;
import java.util.*;
import org.w3c.dom.traversal.*;
import org.apache.xpath.*;
import org.apache.tools.ant.*;

/**
 * performs the basic task of identifying the qualifying
 * XML nodes via XPath, and then performing an action
 * (e.g. replacement, removal, insertion) on that node
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class XmlReplace {

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

  private void log(String msg) {
    // task may not be set sometimes (e.g. during unit tests)
    if (task != null) {
      task.log(msg);
    }
    else {
      System.out.println(msg);
    }
  }

  public int apply(Document doc) throws Exception {
    log("Applying " + action + " to " + path);

    action.setDocument(doc);

    List removals = new ArrayList();
    NodeIterator nl = XPathAPI.selectNodeIterator(doc, path);
    Node n;
    int count = 0;
    while ((n = nl.nextNode()) != null) {
      action.apply(n);
      count++;
    }
    log("Applied " + action + " - " + count + " match(es)");
    action.complete();
    return count;
  }

  public String toString() {
    return action.toString() + " (" + path + ")";
  }
}

