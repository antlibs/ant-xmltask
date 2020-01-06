package com.oopsconsultancy.xmltask.test;

import com.oopsconsultancy.xmltask.Action;
import com.oopsconsultancy.xmltask.AttrAction;
import com.oopsconsultancy.xmltask.InsertAction;
import com.oopsconsultancy.xmltask.RemovalAction;
import com.oopsconsultancy.xmltask.TextAction;
import com.oopsconsultancy.xmltask.XmlAction;
import com.oopsconsultancy.xmltask.XmlReplace;
import com.oopsconsultancy.xmltask.XmlReplacement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * JUnit tests
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
@RunWith(Parameterized.class)
public class TestXmlReplacement {
    // Default is for running tests from IDE
    public static final String TEST_DIRECTORY = System.getProperty("project.test.workingDirectory",
            "src/test/resources") + "/current";

    @Parameterized.Parameters(name = "{0}: xpath({1}) action({2})")
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                { "test1.xml", "/a/b",  new TextAction("x"), "<a>x</a>" },
                { "test1.xml", "/a", new TextAction("x"), "<a><b>Replace me</b></a>" },
                { "test1.xml", "/a/b", new TextAction("x"), "<a>x</a>" },
                { "test1.xml", "/a/b/text()", new TextAction("x"), "<a><b>x</b></a>" },
                { "test1.xml", "/a/b/text()", new TextAction(">>"), "<a><b>&gt;&gt;</b></a>" },
                { "test1.xml", "/a/b", XmlAction.xmlActionfromString("<c><d>e</d></c>", null),
                        "<a><c><d>e</d></c></a>" },
                { "test1.xml", "/a/b/text()", XmlAction.xmlActionfromString("<c><d>e</d></c>", null),
                        "<a><b><c><d>e</d></c></b></a>" },
                { "test1.xml", "/a/b", new XmlAction(), "<a/>" },
                { "test1.xml", "/a", new XmlAction(), "" },
                { "test1.xml", "/a/b", XmlAction.xmlActionfromFile(new File(TEST_DIRECTORY, "substitute1.xml"), null),
                        "<a><p><q>RRR</q></p></a>" },
                { "test1.xml", "/a/b/text()",XmlAction.xmlActionfromFile(new File(TEST_DIRECTORY, "substitute1.xml"), null),
                        "<a><b><p><q>RRR</q></p></b></a>" },
                { "test1.xml", "/a/b", new AttrAction("attr", "val", Boolean.FALSE, null),
                        "<a><b attr=\"val\">Replace me</b></a>" },
                { "test1.xml", "/a/b/text()", new AttrAction("attr", "val", Boolean.FALSE, null),
                        "<a><b>Replace me</b></a>" },
                { "test1.xml", "/a/b/text()", new RemovalAction(), "<a><b/></a>" },
                { "test1.xml", "/a/b", new RemovalAction(), "<a/>" },
                { "test1.xml", "/a/b", InsertAction.fromString("<c>Z</c>", null), "<a><b>Replace me<c>Z</c></b></a>" },
                { "test1.xml", "/a", InsertAction.fromString("<c>Z</c>", null), "<a><b>Replace me</b><c>Z</c></a>" },

                { "test2.xml", "//z", new TextAction("x"), "<x attr=\"1\"><y id=\"2\"/>xx</x>" },
                { "test2.xml", "//z[@id = '3']", new TextAction("x"), "<x attr=\"1\"><y id=\"2\"/>x<z id=\"4\"/></x>" },
                { "test2.xml", "//z[@id = '4']", XmlAction.xmlActionfromString("<test>ABC</test>", null),
                        "<x attr=\"1\"><y id=\"2\"/><z id=\"3\"/><test>ABC</test></x>" },
                { "test2.xml", "//x", new AttrAction("attr", "val", Boolean.FALSE, null),
                        "<x attr=\"val\"><y id=\"2\"/><z id=\"3\"/><z id=\"4\"/></x>" },
                { "test2.xml", "//x/descendant-or-self::*", new AttrAction("id", "8", Boolean.FALSE, null),
                        "<x attr=\"1\" id=\"8\"><y id=\"8\"/><z id=\"8\"/><z id=\"8\"/></x>" },
                { "test2.xml", "//*", new AttrAction("id", "8", Boolean.FALSE, null),
                        "<x attr=\"1\" id=\"8\"><y id=\"8\"/><z id=\"8\"/><z id=\"8\"/></x>" },
                { "test2.xml", "/x/*", XmlAction.xmlActionfromString("<test>ABC</test>", null),
                        "<x attr=\"1\"><test>ABC</test><test>ABC</test><test>ABC</test></x>" },
                { "test2.xml", "//z[2]", XmlAction.xmlActionfromString("<test>ABC</test>", null),
                        "<x attr=\"1\"><y id=\"2\"/><z id=\"3\"/><test>ABC</test></x>" },
                { "test2.xml", "//z[last()]", XmlAction.xmlActionfromString("<test>ABC</test>", null),
                        "<x attr=\"1\"><y id=\"2\"/><z id=\"3\"/><test>ABC</test></x>" },

                { "test3.xml", "//s/parent::*", XmlAction.xmlActionfromString("<test>ABC</test>", null),
                        "<p><q><test>ABC</test></q></p>" }
        });
    }
    @Parameterized.Parameter // first data value (0) is default
    public String filename;

    @Parameterized.Parameter(1)
    public String xpath;

    @Parameterized.Parameter(2)
    public Action action;

    @Parameterized.Parameter(3)
    public String result;

    private Document doc;
    private Transformer serializer;

    @Before
    public void setUp() throws Exception {
        InputSource in = new InputSource(new FileInputStream(new File(TEST_DIRECTORY, filename)));
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true);
        doc = dfactory.newDocumentBuilder().parse(in);
        doc.getDocumentElement().normalize();

        // Set up an identity transformer to use as serializer.
        serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty(OutputKeys.INDENT, "no");
    }

    @Test
    public void test() throws Exception {
        XmlReplacement xmlr = new XmlReplacement(doc, null);
        xmlr.add(new XmlReplace(xpath, action));
        doc = xmlr.apply();
        Writer pw = new StringWriter();
        serializer.transform(new DOMSource(doc), new StreamResult(pw));
        assertEquals(result, pw.toString());
    }
}
