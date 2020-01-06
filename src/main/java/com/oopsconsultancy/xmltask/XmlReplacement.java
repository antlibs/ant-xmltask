package com.oopsconsultancy.xmltask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * performs the actual work of iterating through
 * the sets of tasks, applying them and outputting
 * diagnostics along the way
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class XmlReplacement {

  private final List<XmlReplace> replacements = new ArrayList<XmlReplace>();
  private final Task task;
  private final Document doc;
  private boolean report = false;
  private int failures = 0;

  /**
   * records the document to work on
   *
   * @param doc Document
   * @param task Task
   */
  public XmlReplacement(final Document doc, final Task task) {
    this.doc = doc;
    this.task = task;
  }

  /**
   * records each replacement/modification to perform
   *
   * @param x XmlReplace
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
    Iterator<XmlReplace> ireplacements = replacements.iterator();
    boolean success = true;
    while (ireplacements.hasNext() && success) {
      XmlReplace xr = ireplacements.next();
      try {
        int matches = xr.apply(doc);
        if (matches == 0) {
          failures++;
          task.log(xr + " failed to match", Project.MSG_VERBOSE);
        }
        if (doc.getDocumentElement() != null) {
          doc.getDocumentElement().normalize();
        }
        if (report) {
          output();
        }
      } catch (BuildException e) {
        // this catches build exceptions from subtasks called
        // by <call>. We rethrow since the build has to fail
        throw e;
      } catch (Exception e) {
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
   * @throws Exception if something goes wrong
   */
  private void output() throws Exception {
    output("Document", doc);
  }

  /**
   * outputs the given document (with label) to standard out
   *
   * @param label String
   * @param dc String
   * @throws Exception if something goes wrong
   */
  private void output(final String label, final Document dc) throws Exception {
    // Set up an identity transformer to use as serializer.
    task.log(label + " -->");
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    // and output
    serializer.transform(new DOMSource(dc), new StreamResult(System.out));
    task.log("");
    task.log(label + " <--");
  }

  /**
   * enables diagnostics
   *
   * @param val boolean
   */
  public void setReport(final boolean val) {
    report = val;
  }

  public int getFailures() {
    return failures;
  }
}
