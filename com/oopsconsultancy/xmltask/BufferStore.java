package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;
import org.apache.tools.ant.*;

/**
 * stores a list of nodes vs. buffer name. We clone the given node on
 * storage, and then clone on each retrieval (otherwise we can
 * only insert the stored nodes once into a document)
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public class BufferStore {

  private final static Map buffers = new HashMap();

  /**
   * standard singleton-type approach
   */
  private BufferStore() {
  }

  /**
   * returns an array of nodes stored in a buffer
   * or null if nothing recorded in the buffer
   *
   * @param name the buffer name
   * @return the array of nodes (elements/text/attributes whatever)
   */
  public static Node[] get(String name) {
    List res = (List)buffers.get(name);
    if (res == null) {
      return null;
    }

    Node[] nodes = (Node[])res.toArray(new Node[]{});
    for (int n = 0; n < nodes.length; n++) {
      nodes[n] = nodes[n].cloneNode(true);
    }
    return nodes;
  }

  /**
   * records the given node against the nominated buffer
   *
   * @param name the buffer name
   * @param xml the node to record
   * @param append set to true if appending required
   */
  public static void set(String name, Node xml, boolean append, Task task) {
    // create a deep copy of this...
    Node newnode = xml.cloneNode(true);
    log("Storing " + newnode + " against buffer (" + name + ")", task);
    List list = (List)buffers.get(name);
    if (list != null) {
      if (!append) {
        log(" (overwriting existing entry)", task);
        list = new ArrayList();
        buffers.put(name, list);
      }
      else {
        log(" (appending to existing entry)", task);
      }
    }
    else {
      list = new ArrayList();
      buffers.put(name, list);
    }
    log("", task);
    list.add(newnode);
  }

  public static void log(String msg, Task task) {
    if (task != null) {
      task.log(msg);
    }
    else {
      System.out.println(msg);
    }
  }
}
