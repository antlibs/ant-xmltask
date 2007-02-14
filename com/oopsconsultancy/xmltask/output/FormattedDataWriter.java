package com.oopsconsultancy.xmltask.output;

import java.io.Writer;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import javax.xml.transform.*;
import org.xml.sax.ext.*;

/*
 * Note. This is heavily lifted from Dave Megginson's DataWriter class
 * See www.megginson.com
 */

/**
 * Write data- or field-oriented XML.
 *
 * <p>This filter pretty-prints field-oriented XML without mixed content.
 * all added indentation and newlines will be passed on down
 * the filter chain (if any).</p>
 *
 * <p>In general, all whitespace in an XML document is potentially
 * significant, so a general-purpose XML writing tool like the
 * {@link com.megginson.sax.XMLWriter XMLWriter} class cannot
 * add newlines or indentation.</p>
 *
 * <p>There is, however, a large class of XML documents where information
 * is strictly fielded: each element contains either character data
 * or other elements, but not both.  For this special case, it is possible
 * for a writing tool to provide automatic indentation and newlines
 * without requiring extra work from the user.  Note that this class
 * will likely not yield appropriate results for document-oriented
 * XML like XHTML pages, which mix character data and elements together.</p>
 *
 * <p>This writer will automatically place each start tag on a new line,
 * optionally indented if an indent step is provided (by default, there
 * is no indentation).  If an element contains other elements, the end
 * tag will also appear on a new line with leading indentation.
 * @version $Id$
 */
public class FormattedDataWriter extends XMLWriter implements LexicalHandler, Outputter {

  private boolean escaped = true;

  /**
   * Create a new data writer for the specified output.
   *
   * @param writer The character stream where the XML document
   *        will be written.
   */
  public FormattedDataWriter ()
  {
    super();
  }

  public void setWriter(Writer w) {
    init(w);
  }

  ////////////////////////////////////////////////////////////////////
  // Accessors and setters.
  ////////////////////////////////////////////////////////////////////


  /**
   * Return the current indent step.
   *
   * <p>Return the current indent step: each start tag will be
   * indented by this number of spaces times the number of
   * ancestors that the element has.</p>
   *
   * @return The number of spaces in each indentation step,
   *         or 0 or less for no indentation.
   * @see #setIndentStep
   */
  public int getIndentStep ()
  {
    return indentStep;
  }


  /**
   * Set the current indent step.
   *
   * @param indentStep The new indent step (0 or less for no
   *        indentation).
   * @see #getIndentStep
   */
  public void setIndentStep (int indentStep)
  {
    this.indentStep = indentStep;
  }


  private Transformer transformer = null;
  public void setTransformer(Transformer transformer) {
    this.transformer = transformer;
  }

  ////////////////////////////////////////////////////////////////////
  // Override methods from XMLWriter.
  ////////////////////////////////////////////////////////////////////


  /**
   * Reset the writer so that it can be reused.
   *
   * <p>This method is especially useful if the writer failed
   * with an exception the last time through.</p>
   *
   * @see com.megginson.sax.XMLWriter#reset
   */
  public void reset ()
  {
    depth = 0;
    state = SEEN_NOTHING;
    stateStack = new Stack();
    super.reset();
  }


  private boolean firsttime = true;
  /**
   * Write a start tag.
   *
   * <p>Each tag will begin on a new line, and will be
   * indented by the current indent step times the number
   * of ancestors that the element has.</p>
   *
   * <p>The newline and indentation will be passed on down
   * the filter chain through regular characters events.</p>
   *
   * @param uri The element's Namespace URI.
   * @param localName The element's local name.
   * @param qName The element's qualified (prefixed) name.
   * @param atts The element's attribute list.
   * @exception org.xml.sax.SAXException If there is an error
   *            writing the start tag, or if a filter further
   *            down the chain raises an exception.
   * @see XMLWriter#startElement(String, String, String, Attributes)
   */
  public void startElement (String uri, String localName,
      String qName, Attributes atts)
    throws SAXException
    {
      if (firsttime) {
        firsttime = false;
        // output the XML public / system stuff...
        String pub = transformer.getOutputProperty(OutputKeys.DOCTYPE_PUBLIC);
        String sys = transformer.getOutputProperty(OutputKeys.DOCTYPE_SYSTEM);
        if (pub != null && sys != null) {
          write("<!DOCTYPE " + qName + " PUBLIC \"" + pub + "\" \"" + sys + "\">\n");
        }
        else if (sys != null) {
          write("<!DOCTYPE " + qName + " SYSTEM \"" + sys + "\">\n");
        }
      }
      stateStack.push(SEEN_ELEMENT);
      state = SEEN_NOTHING;
      if (depth > 0) {
        super.characters("\n".toCharArray(),0,1);
      }
      doIndent();
      super.startElement(uri, localName, qName, atts);
      depth++;
    }


  /**
   * Write an end tag.
   *
   * <p>If the element has contained other elements, the tag
   * will appear indented on a new line; otherwise, it will
   * appear immediately following whatever came before.</p>
   *
   * <p>The newline and indentation will be passed on down
   * the filter chain through regular characters events.</p>
   *
   * @param uri The element's Namespace URI.
   * @param localName The element's local name.
   * @param qName The element's qualified (prefixed) name.
   * @exception org.xml.sax.SAXException If there is an error
   *            writing the end tag, or if a filter further
   *            down the chain raises an exception.
   * @see XMLWriter#endElement(String, String, String)
   */
  public void endElement (String uri, String localName, String qName)
    throws SAXException
    {
      depth--;
      if (state == SEEN_ELEMENT) {
        super.characters("\n".toCharArray(),0,1);
        doIndent();
      }
      super.endElement(uri, localName, qName);
      state = stateStack.pop();
    }


  /**
   * Write a empty element tag.
   *
   * <p>Each tag will appear on a new line, and will be
   * indented by the current indent step times the number
   * of ancestors that the element has.</p>
   *
   * <p>The newline and indentation will be passed on down
   * the filter chain through regular characters events.</p>
   *
   * @param uri The element's Namespace URI.
   * @param localName The element's local name.
   * @param qName The element's qualified (prefixed) name.
   * @param atts The element's attribute list.
   * @exception org.xml.sax.SAXException If there is an error
   *            writing the empty tag, or if a filter further
   *            down the chain raises an exception.
   * @see XMLWriter#emptyElement(String, String, String, Attributes)
   */
  public void emptyElement (String uri, String localName,
      String qName, Attributes atts)
    throws SAXException
    {
      state = SEEN_ELEMENT;
      if (depth > 0) {
        super.characters("\n".toCharArray(),0,1);
      }
      doIndent();
      super.emptyElement(uri, localName, qName, atts);
    }


  /**
   * Write a sequence of characters.
   *
   * @param ch The characters to write.
   * @param start The starting position in the array.
   * @param length The number of characters to use.
   * @exception org.xml.sax.SAXException If there is an error
   *            writing the characters, or if a filter further
   *            down the chain raises an exception.
   * @see XMLWriter#characters(char[], int, int)
   */
  public void characters (char ch[], int start, int length)
    throws SAXException
    {
      if (escaped) {
        /*
           System.out.print("CH->");
           for (int c = start; c < start + length; c++) {
           System.out.print((int)ch[c] + ".");
           }
           System.out.println("");
         */

        // I need to trim this...
        int end = start + length - 1;
        while ((ch[start] == ' '|| ch[start] == '\t' || ch[start] == 10) && start < end) {
          start++;
          length--;
        }
        while (length > 0 && (ch[end] == ' '|| ch[end] == '\t' || ch[end] == 10)) {
          end--;
          length--;

          /*
             System.out.print("IN->");
             for (int c = start; c < start + length; c++) {
             System.out.print((int)ch[c] + ".");
             }
             System.out.println("");
           */
        }

        if (length > 0) {
          state = SEEN_DATA;
        }

        /*
           System.out.print("NW->");
           for (int c = start; c < start + length; c++) {
           System.out.print((int)ch[c] + ".");
           }
           System.out.println("");
         */
        super.characters(ch, start, length);
      }
      else {
        nonescapedcharacters(ch, start, length);
      }
    }



  ////////////////////////////////////////////////////////////////////
  // Internal methods.
  ////////////////////////////////////////////////////////////////////


  /**
   * Print indentation for the current level.
   *
   * @exception org.xml.sax.SAXException If there is an error
   *            writing the indentation characters, or if a filter
   *            further down the chain raises an exception.
   */
  private void doIndent ()
    throws SAXException
    {
      if (indentStep > 0 && depth > 0) {
        int n = indentStep * depth;
        char ch[] = new char[n];
        for (int i = 0; i < n; i++) {
          ch[i] = ' ';
        }
        super.characters(ch, 0, n);
      }
    }

  /**
   * Write the XML declaration at the beginning of the document.
   *
   * Pass the event on down the filter chain for further processing.
   *
   * @exception org.xml.sax.SAXException If there is an error
   *            writing the XML declaration, or if a handler further down
   *            the filter chain raises an exception.
   * @see org.xml.sax.ContentHandler#startDocument
   */
  public void startDocument() throws SAXException {
    reset();
    String encoding = transformer.getOutputProperty(OutputKeys.ENCODING);
    String standalone = transformer.getOutputProperty(OutputKeys.STANDALONE);
    write("<?xml version=\"1.0\" " + (encoding == null ? "UTF-8" : "encoding=\""
      +encoding+"\" ") + "standalone=\""+standalone+"\"" + "?>\n\n");
  }



  public void comment(char[] ch, int start, int length) throws SAXException {
    write("\n");
    doIndent();
    write("<!--");
    writeEsc(ch, start, length, false);
    write("-->\n");
  }
  public void startCDATA() throws SAXException {
    // boundary indicator
    doIndent();
    write("<![CDATA[");
    escaped = false;
  }
  public void endCDATA() throws SAXException {
    // boundary indicator
    write("]]>\n");
    escaped = true;
  }
  public void endDTD() throws SAXException {
  }
  public void startDTD(String name, String pub, String sys) throws SAXException {
  }
  public void startEntity(String name) throws SAXException {
  }
  public void endEntity(String name) throws SAXException {
  }

  ////////////////////////////////////////////////////////////////////
  // Constants.
  ////////////////////////////////////////////////////////////////////

  private final static Object SEEN_NOTHING = new Object();
  private final static Object SEEN_ELEMENT = new Object();
  private final static Object SEEN_DATA = new Object();



  ////////////////////////////////////////////////////////////////////
  // Internal state.
  ////////////////////////////////////////////////////////////////////

  private Object state = SEEN_NOTHING;
  private Stack stateStack = new Stack();

  private int indentStep = 0;
  private int depth = 0;

}
