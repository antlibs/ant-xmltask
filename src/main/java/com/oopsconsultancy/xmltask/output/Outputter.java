package com.oopsconsultancy.xmltask.output;

import org.xml.sax.ContentHandler;

import javax.xml.transform.Transformer;
import java.io.Writer;

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
   * @param w Writer
   */
  public void setWriter(Writer w);

  /**
   * the transformer will contain definitions for the public and
   * system ids, encoding etc. See the appropriate Javadoc for
   * more info
   *
   * @param transformer Transformer
   */
  public void setTransformer(Transformer transformer);

  /**
   * sets the header omission
   *
   * @param omitHeader boolean
   */
  public void setOmitHeader(boolean omitHeader);
}
