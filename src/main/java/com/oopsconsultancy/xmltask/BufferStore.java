package com.oopsconsultancy.xmltask;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stores a list of nodes vs. buffer name. We clone the given node on storage,
 * and then clone on each retrieval (otherwise we can only insert the stored
 * nodes once into a document)
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class BufferStore {

  /** The key used to store the buffers as a reference in the project. */
  public static final String BUFFERS_PROJECT_REF = "xmltask.buffers";

  /**
   * indicates that a buffer will be a file
   */
  private static final String FILE_PREFIX = "file://";

  /**
   * standard singleton-type approach
   */
  private BufferStore() {
  }

  /**
   * returns the map containing all the buffers
   *
   * @param task
   *          the task for which the buffers are needed
   * @return the buffers
   */
  private static Map<String, List<Node>> getBuffers(final Task task) {
    if (task == null) {
      throw new IllegalArgumentException("Can't get buffers for a null task");
    }
    if (task.getProject() == null) {
      throw new IllegalArgumentException("Can't get buffers for a task with no associated project");
    }
    Map<String, List<Node>> buffers = task.getProject().getReference(BUFFERS_PROJECT_REF);
    if (buffers == null) {
      buffers = new HashMap<String, List<Node>>();
      task.getProject().addReference(BUFFERS_PROJECT_REF, buffers);
    }
    return buffers;
  }

  /**
   * returns an array of nodes stored in a buffer or null if nothing recorded in
   * the buffer
   *
   * @param name the buffer name
   * @param task Task
   * @return the array of nodes (elements/text/attributes whatever)
   */
  public static Node[] get(final String name, final Task task) {
    List<Node> res = getBuffer(name, task);
    if (res == null) {
      return null;
    }

    Node[] nodes = res.toArray(new Node[]{});
    for (int n = 0; n < nodes.length; n++) {
      nodes[n] = nodes[n].cloneNode(true);
    }
    return nodes;
  }

  /**
   * is the buffer name a file ?
   *
   * @param name String
   * @return true if it's a file
   */
  private static boolean isFileBuffer(final String name) {
    return name.startsWith(FILE_PREFIX);
  }

  /**
   * given a buffer name, returns a filename
   * @param name String
   * @return the filename
   */
  private static String getFilenameFromBuffer(final String name) {
    if (!name.startsWith(FILE_PREFIX)) {
      throw new IllegalArgumentException("Attempt to create a file buffer using '" + name + "'");
    }
    return name.substring(FILE_PREFIX.length());
  }

  /**
   * @param name String
   * @param task Task
   * @return the contents of the given buffer
   */
  private static List<Node> getBuffer(final String name, final Task task) {

    if (isFileBuffer(name)) {
      File file = new File(getFilenameFromBuffer(name));
      if (!file.exists()) {
        return null;
      }
      ObjectInputStream in;
      try {
        in = new ObjectInputStream(new FileInputStream(file));
        // Deserialize the object
        @SuppressWarnings("unchecked")
        List<Node> buffer = (List<Node>) in.readObject();
        in.close();
        return buffer;
      } catch (Exception e) {
        e.printStackTrace();
        throw new IllegalStateException("Problem during deserialization of "
            + file + " : " + e.getMessage());
      }
    }

    Map<String, List<Node>> buffers = getBuffers(task);
    return buffers.get(name);
  }


  /**
   * saves the given buffer
   *
   * @param name String
   * @param list List of Node
   * @param task Task
   */
  private static void setBuffer(final String name, final List<Node> list, final Task task) {
    if (isFileBuffer(name)) {
      File f = new File(getFilenameFromBuffer(name));
      try {
        // Serialize to a file
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(list);
        out.close();
        return;
      } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalStateException("Problem during serialization of "
            + f + " : " + e.getMessage());
      }
    }
    Map<String, List<Node>> buffers = getBuffers(task);
    buffers.put(name, list);
  }

  /**
   * records the given node against the nominated buffer
   *
   * @param name the buffer name
   * @param xml the node to record
   * @param append set to true if appending required
   * @param task Task
   */
  public static void set(final String name, final Node xml,
                         final boolean append, final Task task) {

    if (xml == null) {
      log("No XML to store in buffer '" + name + "'", task);
      return;
    }
    // create a deep copy of this...
    Node newnode = xml.cloneNode(true);
    log("Storing " + newnode + " against buffer (" + name + ")", task);
    List<Node> list = getBuffer(name, task);
    if (list != null) {
      if (!append) {
        log(" (overwriting existing entry)", task);
        list = new ArrayList<Node>();
        setBuffer(name, list, task);
      } else {
        log(" (appending to existing entry)", task);
      }
    } else {
      list = new ArrayList<Node>();
      setBuffer(name, list, task);
    }
    log("", task);
    list.add(newnode);
    setBuffer(name, list, task);

    // some buffer debugging
    /*
     * try { System.out.println("--> Buffer '" + name + "'"); Transformer
     * serializer = TransformerFactory.newInstance().newTransformer(); for
     * (Iterator i = list.iterator(); i.hasNext(); ) { Node node =
     * (Node)i.next();
     * serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
     * serializer.transform(new DOMSource(node), new StreamResult(System.out)); }
     * System.out.println("<-- Buffer '" + name + "'"); } catch (Exception e) {
     * log("Problem during buffer output", task); e.printStackTrace(); }
     */
  }

  /**
   * clears the named buffer
   * @param name String
   * @param task Task
   */
  public static void clear(final String name, final Task task) {
    log("Clearing buffer (" + name + ")", task);
    setBuffer(name, new ArrayList<Node>(), task);
  }

  /**
   * logs messages
   * @param msg String
   * @param task Task
   */
  public static void log(final String msg, final Task task) {
    if (task != null) {
      task.log(msg, Project.MSG_VERBOSE);
    } else {
      System.out.println(msg);
    }
  }
}
