package com.oopsconsultancy.xmltask;

import java.util.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.apache.tools.ant.*;

/**
 * performs the actual work of iterating through
 * the sets of tasks, applying them and outputting
 * diagnostics along the way
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class XmlReplacement {

  private final List replacements = new ArrayList();
  private final Task task;
  private final Document doc;
  private boolean report = false;
  private int failures = 0;

  /**
   * records the document to work on
   *
   * @param doc
   */
  public XmlReplacement(final Document doc, final Task task) {
    this.doc = doc;
    this.task = task;
  }

  /**
   * records each replacement/modification to perform
   *
   * @param x
   */
  public void add(final XmlReplace x) {
    replacements.add(x);
  }

  /**
   * iterate through the tasks, apply each one, remove the redundant nodes
   * and report progress
   *
   * @return the resultant document
   */
  public Document apply() {
    Iterator ireplacements = replacements.iterator();
    boolean success = true;
    while (ireplacements.hasNext() && success) {
      XmlReplace xr = (XmlReplace)ireplacements.next();
      try {
        int matches = xr.apply(doc);
        if (matches == 0) {
          failures++;
          task.log(xr + " failed to match");
        }
        doc.getDocumentElement().normalize();
        if (report) {
          output();
        }
      }
      catch (BuildException e) {
        // this catches build exceptions from subtasks called
        // by <call>. We rethrow since the build has to fail
        throw e;
      }
      catch (Exception e) {
        e.printStackTrace();
        success = false;
        failures++;
      }
    }
    return doc;
  }

  /**
   * outputs the intermediate document results to standard out
   *
   * @throws Exception
   */
  private void output() throws Exception {
    // Set up an identity transformer to use as serializer.
    task.log("Document -->");
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    // and output
    serializer.transform(new DOMSource(doc), new StreamResult(System.out));
    task.log("");
    task.log("Document <--");
  }

  /**
   * enables diagnostics
   *
   * @param val
   */
  public void setReport(final boolean val) {
    report = val;
  }

  public int getFailures() {
    return failures;
  }
}
