package com.oopsconsultancy.xmltask.test;

import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import junit.framework.*;
import javax.xml.parsers.*;
import com.oopsconsultancy.xmltask.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * JUnit tests
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version 1.0
 */
public class TestXmlReplacement extends TestCase {

  private static String filename = null;
  private Document doc = null;
  private XmlReplace replace = null;
  private String result = null;
  private String file = null;

  public TestXmlReplacement(String name, String filename, XmlReplace replace, String result) {
    super(name);
    this.filename = filename;
    this.replace = replace;
    this.result = result;
  }

  public TestXmlReplacement(XmlReplace replace, String result) {
    super("test1");
    this.file = filename;
    this.replace = replace;
    this.result = result;
  }

  public void setUp() throws Exception {
    if (file != null) {
      InputSource in = new InputSource(new FileInputStream(file));
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      doc = dfactory.newDocumentBuilder().parse(in);

      doc.getDocumentElement().normalize();
    }
  }

  public void tearDown() throws Exception {
    System.out.println("Tearing down " + file);
    // Set up an identity transformer to use as serializer.
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    serializer.setOutputProperty(OutputKeys.INDENT, "no");

    // and output
    Writer pw = new StringWriter();
    serializer.transform(new DOMSource(doc), new StreamResult(pw));
    if (!pw.toString().equals(result)) {
      System.out.println("Expected :\n" + result);
      System.out.println("     Got :\n" + pw);
      throw new Exception("Fails");
    }
  }

  public void test1() throws Exception {
    XmlReplacement xmlr = new XmlReplacement(doc, null);
    xmlr.add(replace);
    doc = xmlr.apply();
  }

  public static Test suite() throws Exception {

    TestSuite suite = new TestSuite();

    try {
      filename = "test/test1.xml";
      Action ta1 = new TextAction("x");
      Action ta2 = new TextAction(">>");
      Action ta3 = XmlAction.xmlActionfromString("<c><d>e</d></c>", null);
      Action ta4 = new XmlAction();
      Action ta5 = XmlAction.xmlActionfromFile(new File("test/substitute1.xml"), null);
      Action ta6 = new AttrAction("attr", "val", Boolean.FALSE, null);
      Action ta7 = new RemovalAction();
      Action ta8 = InsertAction.fromString("<c>Z</c>", null);

      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta1), "<a>x</a>"));
      // hierarchy error
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a", ta1), "<a><b>Replace me</b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta1), "<a>x</a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b/text()", ta1), "<a><b>x</b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b/text()", ta2), "<a><b>&gt;&gt;</b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta3), "<a><c><d>e</d></c></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b/text()", ta3), "<a><b><c><d>e</d></c></b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta4), "<a/>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a", ta4), ""));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta5), "<a><p><q>RRR</q></p></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b/text()", ta5), "<a><b><p><q>RRR</q></p></b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta6), "<a><b attr=\"val\">Replace me</b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b/text()", ta6), "<a><b>Replace me</b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b/text()", ta7), "<a><b/></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta7), "<a/>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a/b", ta8), "<a><b>Replace me<c>Z</c></b></a>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/a", ta8), "<a><b>Replace me</b><c>Z</c></a>"));

      // now test attributes etc...
      filename = "test/test2.xml";
      Action ta9 = new TextAction("x");
      Action ta10 = XmlAction.xmlActionfromString("<test>ABC</test>", null);
      Action ta11 = new AttrAction("attr", "val", Boolean.FALSE, null);
      Action ta12 = new AttrAction("id", "8", Boolean.FALSE, null);
      suite.addTest(new TestXmlReplacement(new XmlReplace("//z", ta9), "<x attr=\"1\"><y id=\"2\"/>xx</x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//z[@id = '3']", ta9), "<x attr=\"1\"><y id=\"2\"/>x<z id=\"4\"/></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//z[@id = '4']", ta10), "<x attr=\"1\"><y id=\"2\"/><z id=\"3\"/><test>ABC</test></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//x", ta11), "<x attr=\"val\"><y id=\"2\"/><z id=\"3\"/><z id=\"4\"/></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//x/descendant-or-self::*", ta12), "<x attr=\"1\" id=\"8\"><y id=\"8\"/><z id=\"8\"/><z id=\"8\"/></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//*", ta12), "<x attr=\"1\" id=\"8\"><y id=\"8\"/><z id=\"8\"/><z id=\"8\"/></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("/x/*", ta10), "<x attr=\"1\"><test>ABC</test><test>ABC</test><test>ABC</test></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//z[2]", ta10), "<x attr=\"1\"><y id=\"2\"/><z id=\"3\"/><test>ABC</test></x>"));
      suite.addTest(new TestXmlReplacement(new XmlReplace("//z[last()]", ta10), "<x attr=\"1\"><y id=\"2\"/><z id=\"3\"/><test>ABC</test></x>"));
      filename = "test/test3.xml";
      suite.addTest(new TestXmlReplacement(new XmlReplace("//s/parent::*", ta10), "<p><q><test>ABC</test></q></p>"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return suite;
  }
}

