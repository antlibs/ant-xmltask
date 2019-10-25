package com.oopsconsultancy.xmltask.output;

import java.io.*;
import javax.xml.transform.*;
import org.xml.sax.*;

/**
 * the interface that xmltask output mechanisms have to
 * adhere to... See FormattedDataWriter for an example of
 * usage
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id$
 */
public interface Outputter extends ContentHandler {

  /**
   * this is the writer that the implementing class
   * must write to
   *
   * @param w
   */
  public void setWriter(Writer w);

  /**
   * the transformer will contain definitions for the public and
   * system ids, encoding etc. See the appropriate Javadoc for
   * more info
   *
   * @param transformer
   */
  public void setTransformer(Transformer transformer);

  /**
   * sets the header omission
   */
  public void setOmitHeader(boolean omitHeader);
}
