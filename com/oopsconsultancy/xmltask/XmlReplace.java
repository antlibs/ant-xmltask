package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.apache.tools.ant.*;

// enable the below for JDK 1.3, 1.4
//import org.apache.xpath.*;
//import org.apache.xpath.objects.*;

// enable the below for JDK 1.5
//import com.sun.org.apache.xpath.internal.objects.*;
//import com.sun.org.apache.xpath.internal.*;

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

    if (System.getProperty("java.vm.version").indexOf("1.5") != -1) {
      // running jdk 1.5
      com.sun.org.apache.xpath.internal.objects.XObject result = com.sun.org.apache.xpath.internal.XPathAPI.eval(doc, path);
      if (result instanceof com.sun.org.apache.xpath.internal.objects.XNodeSet) {
        NodeIterator nl = result.nodeset();
        Node n;
        while ((n = nl.nextNode()) != null) {
          action.apply(n);
          count++;
        }
      }
      else if (result instanceof com.sun.org.apache.xpath.internal.objects.XBoolean ||
          result instanceof com.sun.org.apache.xpath.internal.objects.XNumber ||
          result instanceof com.sun.org.apache.xpath.internal.objects.XString) {
        String str = result.str();
        action.apply(doc.createTextNode(str));
        count++;
      }
    }
    else {
      // running jdk 1.4.x and below
      org.apache.xpath.objects.XObject result = org.apache.xpath.XPathAPI.eval(doc, path);
      if (result instanceof org.apache.xpath.objects.XNodeSet) {
        NodeIterator nl = result.nodeset();
        Node n;
        while ((n = nl.nextNode()) != null) {
          action.apply(n);
          count++;
        }
      }
      else if (result instanceof org.apache.xpath.objects.XBoolean ||
          result instanceof org.apache.xpath.objects.XNumber ||
          result instanceof org.apache.xpath.objects.XString) {
        String str = result.str();
        action.apply(doc.createTextNode(str));
        count++;
      }
    }

    log("Applied " + action + " - " + count + " match(es)", Project.MSG_VERBOSE);
    action.complete();
    return count;
  }

  public String toString() {
    return action.toString() + " (" + path + ")";
  }
}

