package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.apache.xpath.*;
import org.apache.tools.ant.*;
import org.apache.xpath.objects.*;

/**
 * performs the basic task of identifying the qualifying
 * XML nodes via XPath, and then performing an action
 * (e.g. replacement, removal, insertion) on that node
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
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

  private void log(String msg, int level) {
    // task may not be set sometimes (e.g. during unit tests)
    if (task != null) {
      task.log(msg);
    }
    else {
      System.out.println(msg);
    }
  }

  public int apply(Document doc) throws Exception {
    log("Applying " + action + " to " + path, Project.MSG_VERBOSE);

    action.setDocument(doc);

    List removals = new ArrayList();

    int count = 0;
    XObject result = XPathAPI.eval(doc, path);
    if (result instanceof XNodeSet) {
      NodeIterator nl = result.nodeset();
      // NodeIterator nl = XPathAPI.selectNodeIterator(doc, path);
      Node n;
      while ((n = nl.nextNode()) != null) {
        action.apply(n);
        count++;
      }
    }
    else if (result instanceof XBoolean ||
             result instanceof XNumber ||
             result instanceof XString) {
      String str = result.str();
      action.apply(doc.createTextNode(str));
      count++;
    }

    log("Applied " + action + " - " + count + " match(es)", Project.MSG_VERBOSE);
    action.complete();
    return count;
  }

  public String toString() {
    return action.toString() + " (" + path + ")";
  }
}

