package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.BufferStore;
import com.oopsconsultancy.xmltask.XmlReplace;
import com.oopsconsultancy.xmltask.XmlReplacement;
import com.oopsconsultancy.xmltask.output.FormattedDataWriter;
import com.oopsconsultancy.xmltask.output.Outputter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.StringInputStream;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.XMLCatalog;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * the basic Ant xml task. Records a set of actions to
 * perform, then iterates through each one, actioning
 * each one then removing the redundant nodes
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class XmlTask extends Task {

  private static final String FMT_NONE = "default";
  private static final String FMT_SIMPLE = "simple";

  private final boolean settingVersion = false;
  private final String xmlVersion = "1.0";
  private boolean settingStandalone = false;
  private boolean standalone = false;
  private boolean omitHeader = false;
  private boolean todir = false;
  private boolean tobuffer = false;
  private boolean reporting = false;
  private boolean expandEntityReferences = true;
  private String doctype_public = null;
  private String doctype_system = null;
  private String dir = null;
  private final LocalEntityResolver resolver = new LocalEntityResolver();
  private final XMLCatalog xmlCatalog = new XMLCatalog();
  private boolean normalize = true;
  private boolean indent = true;
  private String encoding = null;
  private String outputEncoding = null;
  private String outputter = FMT_NONE;
  private boolean preservetype = false;
  private boolean failWithoutMatch = false;
  private String[] buffers = new String[]{};
  private String xpathFactory = XPathFactory.class.getName();
  private String xpathObjectModelUri = XPathFactory.DEFAULT_OBJECT_MODEL_URI;

  private final List<FileSet> filesets = new ArrayList<FileSet>();

  /**
   * the file/buffer to output to
   */
  private String dest =  null;

  /**
   * the XML document to work on (from the source file)
   */
  private List<InputSpec> docs = new ArrayList<InputSpec>();

  public XmlTask() {
    super();
  }

  public void init() throws BuildException {
    super.init();
    xmlCatalog.setProject(getProject());
  }

  protected EntityResolver getEntityResolver() {
    return xmlCatalog;
  }

  /**
   * the list of replacements to build
   */
  private final List<XmlReplace> replacements = new ArrayList<XmlReplace>();

  @Deprecated
  public void setFactory(final String p) {
    log("'factory' attribute is deprecated, please use 'defaultXpathFactory' instead", Project.MSG_WARN);
    setDefaultXpathFactory(p);
  }

  public void setDefaultXpathFactory(final String p) {
    log("XPath Factory = " + p, Project.MSG_VERBOSE);
    System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, p);
  }

  public void setPublic(final String p) {
    doctype_public = p;
  }

  public void setSystem(String s) {
    doctype_system = s;
  }

  public void setPreserveType(final boolean p) {
    this.preservetype = p;
  }

  private String getPathPrefix() {
    if (dir == null) {
      File f = getProject().getBaseDir();
      dir = f.getAbsolutePath();
      if (!dir.endsWith("" + File.separator)) {
        dir += File.separator;
      }
    }
    return dir;
  }

  /**
   * records the source buffer
   *
   * @param buffer String
   */
  public void setSourceBuffer(final String buffer) {
    docs.add(new InputBuffer(buffer));
  }

  public void setSourceProperty(final String prop) {
    docs.add(new InputProperty(prop));
  }

  public void setExpandEntityReferences(final boolean expandEntityReferences) {
    this.expandEntityReferences = expandEntityReferences;
  }

  /**
   * records the source file(s). These can be wildcarded
   *
   * @param source String
   */
  public void setSource(final String source) {

    if (source.contains("*")) {
      log("Wildcarded source now deprecated in favour of <fileset> usage", Project.MSG_WARN);
      String basedir;
      DirectoryScanner ds = new DirectoryScanner();
      String includes;
      if ((new File(source)).isAbsolute()) {
        int wildcard = source.indexOf("*");
        basedir = source.substring(0, source.lastIndexOf(File.separator, wildcard));
        includes = source.substring(source.lastIndexOf(File.separator, wildcard) + 1);
        ds.setIncludes(new String[]{includes});
      } else {
        basedir = getPathPrefix();
        includes = source;
      }
      ds.setIncludes(new String[]{includes});
      ds.setBasedir(basedir);
      log("Scanning for " + includes + " from " + basedir, Project.MSG_VERBOSE);
      ds.scan();
      for (int d = 0; d < ds.getIncludedFiles().length; d++) {
        String included = basedir + File.separator +  ds.getIncludedFiles()[d];
        log("Adding " + included, Project.MSG_VERBOSE);
        docs.add(new InputFile(included, basedir));
      }
    } else {
      File sf = new File(source);
      docs = new ArrayList<InputSpec>();
      String file = source;
      if (!sf.isAbsolute()) {
        file = getPathPrefix() + source;
        docs.add(new InputFile(file, getPathPrefix()));
      } else {
        docs.add(new InputFile(file));
      }
      log("Reading " + file, Project.MSG_VERBOSE);
    }
  }

  /**
   * defines the source input
   */
  public abstract class InputSpec {
    protected String name;
    public InputSpec(final String name) {
      this.name = name;
    }
    public String toString() {
      return name;
    }
    public String getName() {
      return name;
    }
    public abstract Document getDocument() throws Exception;
  }

  /**
   * defines the input as a file (absolute or relative paths)
   */
  public class InputFile extends InputSpec {
    protected String base = null;       // what to remove to make it relative again
    public InputFile(final String name) {
      super(name);
    }
    public InputFile(final String name, final String base) {
      super(name);
      this.base = base;
    }
    public String getBase() {
      return base;
    }
    public Document getDocument() throws Exception {
      return documentFromFile(getName());
    }
  }

  /**
   * defines the input as a property. This doesn't
   * currently work since I only allow copying of
   * attributes and text to properties
   */
  public class InputProperty extends InputSpec {
    protected String base = null;       // what to remove to make it relative again
    public InputProperty(final String name) {
      super(name);
    }
    public Document getDocument() throws Exception {
      return documentFromStr(getProject().getProperty(name));
    }
  }


  /**
   * defines the input as an xmltask buffer
   */
  public class InputBuffer extends InputSpec {
    public InputBuffer(final String name) {
      super(name);
    }
    public Document getDocument() throws Exception {
      Node[] nodes = BufferStore.get(getName(), XmlTask.this);
      if (nodes == null) {
        return createDocument();
      }
      if (nodes.length != 1) {
        throw new BuildException("Cannot use multiple buffer nodes as an input source");
      }
      Document document = createDocument();
      Node orig = nodes[0];
      if (orig instanceof Document) {
        orig = ((Document) orig).getDocumentElement();
      }
      Node newnode = document.importNode(orig, true);
      document.appendChild(newnode);
      return document;
    }
  }

  /**
   * holds a map of remote to local entity mappings
   */
  public static class LocalEntityResolver implements EntityResolver {

    private final Map<String, String> entities = new HashMap<String, String>();

    public InputSource resolveEntity(String publicId, String systemId) {
      String local = null;
      if (entities.containsKey(publicId)) {
        local = entities.get(publicId);
      } else if (entities.containsKey(systemId)) {
        local = entities.get(systemId);
      }
      if (local != null) {
        if (local.equals("")) {
          return new InputSource(new StringReader(""));
        }
        return new InputSource(local);
      }

      // default behaviour
      return null;
    }

    public void registerEntity(final XmlTask task, final String remote, String local) {
      // I need to determine if abc is a url or an absolute path, and if not,
      // prepend the absolute directory
      // so it's not blank
      if (!local.equals("") && !(new File(local)).isAbsolute() && !local.contains("://")) {
        local = task.getPathPrefix() + local;
      }
      entities.put(remote, local);
    }

    public int registeredEntities() {
      return entities.keySet().size();
    }
  }

  public void addConfiguredXMLCatalog(final XMLCatalog catalog) {
    xmlCatalog.addConfiguredXMLCatalog(catalog);
  }

  /**
   * creates a fresh empty document
   *
   * @return Document
   * @throws Exception if something goes wrong
   */
  private Document createDocument() throws Exception {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    dfactory.setNamespaceAware(true);
    DocumentBuilder builder = dfactory.newDocumentBuilder();
    return builder.newDocument();
  }

  /**
   * builds the input document given a stream of chars
   * as a source
   *
   * @param is InputStream
   * @return Document
   * @throws Exception if something goes wrong
   */
  private Document documentFromStream(final InputStream is) throws Exception {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

    dfactory.setNamespaceAware(true);
    dfactory.setExpandEntityReferences(expandEntityReferences);

    DocumentBuilder builder = dfactory.newDocumentBuilder();
    if (resolver.registeredEntities() > 0) {
      log("Using local entity references", Project.MSG_VERBOSE);
      builder.setEntityResolver(resolver);
    } else {
      log("Using predefined xml catalog", Project.MSG_VERBOSE);
      builder.setEntityResolver(xmlCatalog);
    }

    InputSource in = new InputSource(is);
    try {
      Document doc = builder.parse(in);

      // mmm. always get null here. Must investigate sometime
      encoding = in.getEncoding();

      doc.getDocumentElement().normalize();
      return doc;
    } catch (UnknownHostException e) {
      // this is quite common
      reportNetworkError();
      throw new BuildException(e.getMessage(), e);
    } catch (ConnectException e) {
      // this is quite common
      reportNetworkError();
      throw new BuildException(e.getMessage(), e);
    }

  }

  /**
   * report a common mistake
   */
  private void reportNetworkError() {
    log("It looks like you've got a network error. The probable cause", Project.MSG_ERR);
    log("is that you're trying to resolve a DTD on the internet although", Project.MSG_ERR);
    log("you don't know it! Check your XML for DTDs external to your network", Project.MSG_ERR);
    log("and read the Ant documentation for <xmlcatalog>. XMLTask will support", Project.MSG_ERR);
    log("usage of <xmlcatalog>. See the following:", Project.MSG_ERR);
    log("http://ant.apache.org/manual/CoreTypes/xmlcatalog.html", Project.MSG_ERR);
    log("http://www.oopsconsultancy.com/software/xmltask", Project.MSG_ERR);
    log("If this isn't the problem, then please report this error to the support", Project.MSG_ERR);
    log("mailing list. Thanks!", Project.MSG_ERR);
  }

  /**
   * builds the input document given the filename
   * as a source
   *
   * @param filename String
   * @return Document
   * @throws Exception if something goes wrong
   */
  private Document documentFromFile(final String filename) throws Exception {
    return documentFromStream(new FileInputStream(filename));
  }

  /**
   * builds the input document given a raw document string
   * as a source. Note that encoding is assumed as ISO-Latin1
   * and there will be data loss
   *
   * @param str String
   * @return Document
   * @throws Exception if something goes wrong
   */
  private Document documentFromStr(final String str) throws Exception {
    return documentFromStream(new StringInputStream(str));
  }

  /**
   * records the output file
   *
   * @param dest String
   */
  public void setDest(final String dest) {
    this.dest = dest;
    todir = false;
    tobuffer = false;
  }

  /**
   * records the output buffer
   *
   * @param dest String
   */
  public void setDestBuffer(final String dest) {
    this.dest = dest;
    todir = false;
    tobuffer = true;
  }


  /**
   * sets the mechanism for outputting the XML
   *
   * @param outputter String
   */
  public void setOutputter(final String outputter) {
    this.outputter = outputter;
  }

  /**
   * records the output directory
   *
   * @param dest String
   */
  public void setTodir(final String dest) {
    this.dest = dest;
    todir = true;
    tobuffer = false;
  }

  /**
   * allows setting of the output encoding
   *
   * @param enc String
   */
  public void setEncoding(final String enc) {
    outputEncoding = enc;
  }

  /**
   * records an XmlReplace object to perform later
   *
   * @param xmlr XmlReplace
   */
  public void add(final XmlReplace xmlr) {
    xmlr.setTask(this);
    replacements.add(xmlr);
  }

  /**
   * switches on reporting. Reporting means that each
   * iteration of the modified Xml document is reported
   *
   * @param report boolean
   */
  public void setReport(final boolean report) {
    this.reporting = report;
  }

  /**
   * determines whether the header should be omitted
   *
   * @param omitHeader boolean
   */
  public void setOmitHeader(final boolean omitHeader) {
    this.omitHeader = omitHeader;
  }

  /**
   * determines whether the document is standalone
   *
   * @param standalone boolean
   */
  public void setStandAlone(final boolean standalone) {
    this.standalone = standalone;
    settingStandalone = true;
  }
  /* *
   * determines the document version
   *
   * From the javadoc for OutputKeys
   * <pre>
   * When the output method is "xml", the version value specifies the version of
   * XML to be used for outputting the result tree. The default value for the xml
   * output method is 1.0. When the output method is "html", the version value
   * indicates the version of the HTML. The default value for the html output
   * method is 4.0, which specifies that the result should be output as HTML
   * conforming to the HTML 4.0 Recommendation [HTML]. If the output method is
   * "text", the version property is ignored.
   * </pre>
   * @param version
   */
  /*
  private void setVersion(final String xmlVersion) {
    this.xmlVersion = xmlVersion;
    settingVersion = true;
  }
  */



  /**
   * determines whether the ouput document is munged to
   * remove redundant text nodes, whitespace etc. By
   * default it is
   *
   * @param norm boolean
   */
  public void setNormalize(final boolean norm) {
    this.normalize = norm;
  }

  /**
   * lists the set of buffers to be cleared
   *
   * @param bufferset String
   */
  public void setClearBuffers(final String bufferset) {
    StringTokenizer st = new StringTokenizer(bufferset, ",");
    List<String> res = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      res.add(st.nextToken());
    }
    buffers = res.toArray(new String[]{});
  }

  /**
   * determines whether the output document is indented
   * By default it is
   *
   * @param in boolean
   */
  public void setIndent(final boolean in) {
    this.indent = in;
  }

  public String getXpathObjectModelUri() {
    return xpathObjectModelUri;
  }

  public void setXpathObjectModelUri(final String xpathObjectModelUri) {
    this.xpathObjectModelUri = xpathObjectModelUri;
  }

  public String getXpathFactory() {
    return xpathFactory;
  }

  public void setXpathFactory(String xpathFactory) {
    this.xpathFactory = xpathFactory;
  }

  /**
   * derives and returns the version number
   *
   * @return a string with the version info
   */
  private String getVersion() {
    Properties props = new Properties();
    try {
      props.load(this.getClass().getClassLoader().getResourceAsStream("xmltask.properties"));
    } catch (Exception e) {
      // can't find properties file ?
    }
    String version = props.getProperty("com.oopsconsultancy.xmltask.version");
    return version == null ? "[no version info]" : version;
  }

  /**
   * executes the Ant task
   *
   * @throws BuildException if something goes wrong
   */
  public void execute() throws BuildException {
    log("Executing xmltask " + getVersion(), Project.MSG_VERBOSE);

    if (!filesets.isEmpty()) {
      if (docs.size() > 0) {
        throw new BuildException("Can't use filesets together with source inputs");
      }
      Iterator<FileSet> iter = filesets.iterator();
      int count = 0;
      FileSet fs;
      while (iter.hasNext()) {
        fs = iter.next();
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        File srcDir = fs.getDir(getProject());
        String[] srcFiles = ds.getIncludedFiles();
        for (String path : srcFiles) {
          docs.add(new InputFile(srcDir.getAbsolutePath() + File.separator + path, srcDir.getAbsolutePath()));
        }
        count += srcFiles.length;
      }
      if (count == 0) {
        // we have a fileset, but nothing is matching, so we'll return
        return;
      }
    }
    if (docs.size() == 0 && todir) {
      throw new BuildException("No input documents");
    } else if (docs.size() == 0) {
      // no input document, so we'll create a dummy one...
      docs.add(null);
    }
    if (docs.size() > 1 && !todir && dest != null) {
      throw new BuildException("Multiple inputs (" + docs.size() + ") but only one output file");
    }
    if (dest == null && todir) {
      throw new BuildException("No output directory");
    }

    // now make the destination directory absolute...
    if (dest != null && !tobuffer) {
      File fdest = new File(dest);
      if (!fdest.isAbsolute()) {
        dest = getPathPrefix() + dest;
      }
    }

    // first clear any buffers requested
    for (String buffer : buffers) {
      BufferStore.clear(buffer, this);
    }

    // now process each document
    for (InputSpec spec : docs) {
      log("Processing " + (spec == null ? "" : spec.getName()) + (dest == null ? " [no output document]" : (" into " + dest)), Project.MSG_VERBOSE);

      Document document;
      try {
        document = spec == null ? createDocument() : spec.getDocument();
      } catch (Exception e) {
        e.printStackTrace();
        throw new BuildException(e.getMessage());
      }

      String destfile = spec == null ? null : spec.getName();

      if (todir) {
        if (!(spec instanceof InputFile)) {
          throw new BuildException("Can't write to a directory with a non-file input");
        }
        // we strip down to the original filename to write out
        // to the destination (only if a dir)
        destfile = destfile.substring(((InputFile) spec).getBase().length());
      }
      processDoc(document, destfile);
    }

    // and clear the doc list for the next invocation
    docs.clear();

    // and further clearup...
    replacements.clear();
  }

  /**
   * process the document using the set of xml replacements
   * created for this task, and then output. Output depends
   * on whether directory or file output has been selected
   *
   * @param doc the document to work on
   * @param name the destination file
   * @throws BuildException if something goes wrong
   */
  private void processDoc(Document doc, final String name) throws BuildException {
    try {
      // get doctype info if required...
      DocumentType dt = doc.getDoctype();
      if (dt != null && preservetype) {
        log("Pub = " + dt.getPublicId(), Project.MSG_VERBOSE);
        log("Sys = " + dt.getSystemId(), Project.MSG_VERBOSE);
      }

      // now process...
      XmlReplacement replacement = new XmlReplacement(doc, this);
      for (XmlReplace xmlReplace : replacements) {
        replacement.add(xmlReplace);
      }
      replacement.setReport(reporting);
      doc = replacement.apply();

      if (replacement.getFailures() > 0 && failWithoutMatch) {
        throw new BuildException("<xmltask> subtasks failed to find matches");
      }

      if (dest != null) {
        // and output
        if (tobuffer) {
          // write to a buffer
          BufferStore.set(dest, doc.getDocumentElement(), false, this);
        } else {

          // and then write out...
          Transformer serializer = TransformerFactory.newInstance().newTransformer();
          serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, (omitHeader ? "yes" : "no"));

          if (settingStandalone) {
            System.out.println("Setting standalone");
            serializer.setOutputProperty(OutputKeys.STANDALONE, (standalone ? "yes" : "no"));
          }
          if (settingVersion) {
            serializer.setOutputProperty(OutputKeys.VERSION, xmlVersion);
          }

          if (preservetype) {

            // use the document's
            if (dt != null) {
              // but I don't want to set PUBLIC and SYSTEM = "" i.e. both blank
              if (dt.getPublicId() != null) {
                serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dt.getPublicId());
              }
              // "Private" External DTDs - see http://xmlwriter.net/xml_guide/doctype_declaration.shtml

              if (dt.getSystemId() != null) {
                serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
              } else {
                serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "");
              }
            }
          } else {
            // use the configured... (if configured!)
            if (doctype_public != null) {
              serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype_public);
            }
            if (doctype_system != null) {
              serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype_system);
            }
          }

          if (normalize && doc != null && doc.getDocumentElement() != null) {
            log("Normalizing resultant document", Project.MSG_VERBOSE);
            doc.getDocumentElement().normalize();
          }
          if (indent) {
            log("Indenting resultant document", Project.MSG_VERBOSE);
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
          } else {
            serializer.setOutputProperty(OutputKeys.INDENT, "no");
          }

          // write to a file or directory
          Writer w;
          if (!todir) {
            w = getWriter(dest, serializer);
          } else {
            String destname = dest + File.separator + name;
            log("Writing " + destname, Project.MSG_VERBOSE);
            File dir = (new File(destname)).getParentFile();
            if (!dir.exists()) {
              if (!dir.mkdirs()) {
                throw new Exception("Failed to make destination directory " + dest);
              }
            }
            w = getWriter(destname, serializer);
          }

          Result res;
          if (FMT_NONE.equals(outputter)) {
            res = new StreamResult(w);
          } else if (outputter.startsWith(FMT_SIMPLE)) {
            FormattedDataWriter dw = new FormattedDataWriter();

            dw.setWriter(w);
            dw.setIndentStep(2);
            dw.setOmitHeader(omitHeader);
            if (outputter.contains(":")) {
              // looks like it's formatted as simple:{indent}...
              String fmt = outputter.substring(outputter.indexOf(":") + 1);
              dw.setIndentStep(Integer.parseInt(fmt));
            }

            /*
               System.out.println("SET PREFIX");
               dw.forceNSDecl("http://exist.sourceforge.net/NS/exist", "exist");
               dw.setPrefix("http://www.accountz.com/xmlbeans/model", "");
             */

            dw.setTransformer(serializer);
            res = new SAXResult(dw);
          } else {
            // try and load this as a custom task...
            log("Loading custom result writer " + outputter, Project.MSG_VERBOSE);
            Outputter op = (Outputter) Class.forName(outputter).getDeclaredConstructor().newInstance();
            op.setWriter(w);
            op.setTransformer(serializer);
            res = new SAXResult(op);
          }
          serializer.transform(new DOMSource(doc), res);
          w.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new BuildException("Can't create " + dest);
    } catch (BuildException e) {
      // this is thrown by Ant earlier. I don't want to print this
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException(e.getMessage());
    }
  }

  /**
   * returns a writer built using a character encoding
   * (if available). The character encoding can be set
   * by the user, or the document's will be used if we can
   * determine it
   *
   * @param filename String
   * @param serializer Transformer
   * @return the resultant writer
   * @throws IOException if something goes wrong
   */
  private Writer getWriter(final String filename, final Transformer serializer) throws IOException {
    String enc = outputEncoding;
    if (enc == null) {
      enc = encoding;
    }
    if (enc != null) {
      log("Using output character encoding " + enc, Project.MSG_VERBOSE);
      serializer.setOutputProperty(OutputKeys.ENCODING, enc);
      return new OutputStreamWriter(new FileOutputStream(filename), enc);
    }
    return new FileWriter(filename);
  }

  // create the task elements below

  public void addConfiguredReplace(final Replace replace) {
    replace.process(this);
  }


  public void addConfiguredRemove(final Remove remove) {
    remove.process(this);
  }


  public void addConfiguredAttr(final Attr attr) {
    attr.process(this);
  }


  public void addConfiguredInsert(final Insert insert) {
    insert.process(this);
  }

  public void addConfiguredRegexp(final Regexp regexp) {
    regexp.process(this);
  }

  public void addConfiguredPaste(final Paste paste) {
    paste.process(this);
  }

  public void addConfiguredUncomment(final Uncomment uncomment) {
    uncomment.process(this);
  }

  public void addConfiguredCopy(final Copy copy) {
    copy.process(this);
  }

  public void addConfiguredCall(final Call call) {
    call.process(this);
  }

  public void addConfiguredCut(final Cut cut) {
    cut.process(this);
  }

  public void addConfiguredRename(final Rename rename) {
    rename.process(this);
  }

  public Entity createEntity() {
    return new Entity(this);
  }

  public void addConfiguredPrint(final Print print) {
    print.process(this);
  }

  public void registerEntity(final String remote, final String local) {
    resolver.registerEntity(this, remote, local);
  }

  public void setFailWithoutMatch(final boolean f) {
    this.failWithoutMatch = f;
  }

  public boolean isFailWithoutMatch() {
    return failWithoutMatch;
  }

  /**
   * Adds a set of files as source.
   *
   * @param set FileSet
   */
  public void addFileset(final FileSet set) {
    filesets.add(set);
  }
}
