package com.oopsconsultancy.xmltask.ant;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.xml.sax.helpers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import com.oopsconsultancy.xmltask.*;
import com.oopsconsultancy.xmltask.output.*;

/**
 * the basic Ant xml task. Records a set of actions to
 * perform, then iterates through each one, actioning
 * each one then removing the redundant nodes
 *
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 */
public class XmlTask extends Task {

  private final static String FMT_NONE = "default";
  private final static String FMT_SIMPLE = "simple";

  private boolean todir = false;
  private boolean reporting = false;
  private String doctype_public = null;
  private String doctype_system = null;
  private String dir = null;
  private LocalEntityResolver resolver = new LocalEntityResolver();
  private XMLCatalog xmlCatalog = new XMLCatalog();
  private boolean normalize = true;
  private boolean indent = true;
  private String encoding = null;
  private String outputEncoding = null;
  private String outputter = FMT_NONE;
  private boolean preservetype = false;
  private boolean failWithoutMatch = false;

  /**
   * the file to output
   */
  private String dest =  null;

  /**
   * the XML document to work on (from the source file)
   */
  private List docs = new ArrayList();

  public XmlTask() {
    super();
  }

  public Project getProject() {
    return project;
  }

  public void init() throws BuildException {
    super.init();
    xmlCatalog.setProject(project);
  }

  protected EntityResolver getEntityResolver() {
    return xmlCatalog;
  }

  /**
   * the list of replacements to build
   */
  private List replacements = new ArrayList();

  public void setPublic(String p) {
    doctype_public = p;
  }

  public void setSystem(String s) {
    doctype_system = s;
  }

  public void setPreserveType(String p) {
    if ("on".equals(p) || "true".equals(p)) {
      preservetype = true;
    }
  }

  private String getPathPrefix() {
    if (dir == null) {
      File f = getProject().getBaseDir();
      dir = f.getAbsolutePath();
      if (!dir.endsWith("" +File.separator)) {
        dir = dir + File.separator;
      }
    }
    return dir;
  }

  /**
   * records the source and generates a DOM document
   *
   * @param source
   * @throws Exception
   */
  public void setSource(String source) throws Exception {

    if (source.indexOf("*") != -1) {

      String basedir = null;
      DirectoryScanner ds = new DirectoryScanner();
      String includes = null;
      boolean absolute = false;
      if ((new File(source)).isAbsolute()) {
        absolute = true;
        basedir = source.substring(0, source.lastIndexOf(File.separator));
        includes = source.substring(source.lastIndexOf(File.separator) + 1);
        ds.setIncludes(new String[]{includes});
      }
      else {
        basedir = getPathPrefix();
        includes = source;
      }
      ds.setIncludes(new String[]{includes});
      ds.setBasedir(basedir);
      log("Scanning for " + includes + " from " + basedir);
      ds.scan();
      for (int d = 0; d < ds.getIncludedFiles().length; d++) {
        String included = basedir + File.separator +  ds.getIncludedFiles()[d];
        log("Adding " + included);
        docs.add(new FileSpec(included, absolute, basedir));
      }
    }
    else {
      File sf = new File(source);
      docs = new ArrayList();
      String file = source;
      boolean absolute = true;
      if (!sf.isAbsolute()) {
        file = getPathPrefix() + source;
        absolute = false;
        docs.add(new FileSpec(file, absolute, getPathPrefix()));
      }
      else {
        docs.add(new FileSpec(file, absolute));
      }
      log("Reading " + file);
    }
  }

  /**
   * defines the source filename and whether it was specified
   * as absolute or not
   */
  public class FileSpec {
    public String name = null;
    public boolean absolute = false; // was the path specified as absolute ?
    public String base = null;       // what to remove to make it relative again
    public FileSpec(String name, boolean absolute) {
      this.name = name;
      this.absolute = absolute;
    }
    public FileSpec(String name, boolean absolute, String base) {
      this.name = name;
      this.absolute = absolute;
      this.base = base;
    }
    public String toString() {
      return name + (absolute ? " (specced as absolute)" : "(base used = " + base + ")");
    }
  }

  /**
   * holds a map of remote to local entity mappings
   */
  public static class LocalEntityResolver implements EntityResolver {

    private Map entities = new HashMap();

    public InputSource resolveEntity(String publicId, String systemId) {
      String local = null;
      if (entities.containsKey(publicId)) {
        local = (String)entities.get(publicId);
      }
      else if (entities.containsKey(systemId)) {
        local = (String)entities.get(systemId);
      }
      if (local != null) {
        if (local.equals("")) {
          return new InputSource(new StringReader(""));
        }
        else {
          return new InputSource(local);
        }
      }

      // default behaviour
      return null;
    }

    public void registerEntity(XmlTask task, String remote, String local) {
      // I need to determine if abc is a url or an absolute path, and if not,
      // prepend the absolute directory
      if (!local.equals("")) {
        // so it's not blank
        if ((new File(local)).isAbsolute() ||
            local.indexOf("://") != -1) {
          // don't do anything
        }
        else {
          local = task.getPathPrefix() + local;
        }
      }
      entities.put(remote, local);
    }

    public int registeredEntities() {
      return entities.keySet().size();
    }
  }

  public void addConfiguredXMLCatalog(XMLCatalog catalog) {
    xmlCatalog.addConfiguredXMLCatalog(catalog);
  }

  /**
   * creates a fresh empty document
   *
   * @throws Exception
   */
  private Document createDocument() throws Exception {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    dfactory.setNamespaceAware(true);
    DocumentBuilder builder = dfactory.newDocumentBuilder();
    return builder.newDocument();
  }

  /**
   * builds the input document given the filename
   * as a source
   *
   * @param filename
   * @throws Exception
   */
  private Document documentFromFile(String filename) throws Exception {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

    dfactory.setNamespaceAware(true);
    dfactory.setExpandEntityReferences(false);

    DocumentBuilder builder = dfactory.newDocumentBuilder();
    if (resolver.registeredEntities() > 0) {
      log("Using local entity references");
      builder.setEntityResolver(resolver);
    }
    else {
      log("Using predefined xml catalog");
      builder.setEntityResolver(xmlCatalog);
    }

    InputSource in = new InputSource(new FileInputStream(filename));
    Document doc = builder.parse(in);

    // mmm. always get null here. Must investigate sometime
    encoding = in.getEncoding();

    doc.getDocumentElement().normalize();
    return doc;
  }

  /**
   * records the output file
   *
   * @param dest
   */
  public void setDest(String dest) {
    this.dest = dest;
    todir = false;
  }

  /**
   * sets the mechanism for outputting the XML
   *
   * @param outputter
   */
  public void setOutputter(String outputter) {
    this.outputter = outputter;
  }

  /**
   * records the output directory
   *
   * @param dest
   */
  public void setTodir(String dest) {
    this.dest = dest;
    todir = true;
  }

  /**
   * allows setting of the output encoding
   *
   * @param enc
   */
  public void setEncoding(String enc) {
    outputEncoding = enc;
  }

  /**
   * records an XmlReplace object to perform later
   *
   * @param xmlr
   */
  public void add(XmlReplace xmlr) {
    xmlr.setTask(this);
    replacements.add(xmlr);
  }

  /**
   * switches on reporting. Reporting means that each
   * iteration of the modified Xml document is reported
   *
   * @param report
   */
  public void setReport(String report) {
    if ("on".equals(report) || "true".equals(report)) {
      reporting = true;
    }
  }

  /**
   * determines whether the ouput document is munged to
   * remove redundant text nodes, whitespace etc. By
   * default it is
   *
   * @param norm
   */
  public void setNormalize(String norm) {
    if ("on".equals(norm) || "true".equals(norm)) {
      normalize = true;
    }
    if ("off".equals(norm) || "false".equals(norm)) {
      normalize = false;
    }
  }

  /**
   * determines whether the ouput document is indented
   * By default it is
   *
   * @param i
   */
  public void setIndent(String in) {
    if ("on".equals(in) || "true".equals(in)) {
      indent = true;
    }
    if ("off".equals(in) || "false".equals(in)) {
      indent = false;
    }
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
    }
    catch (Exception e) {
      // can't find properties file ?
    }
    String version = props.getProperty("com.oopsconsultancy.xmltask.version");
    return version == null ? "[no version info]" : version;
  }

  /**
   * executes the Ant task
   *
   * @throws BuildException
   */
  public void execute() throws BuildException {
    log("Executing xmltask " + getVersion());
    if (docs.size() == 0 && todir) {
      throw new BuildException("No input documents");
    }
    else if (docs.size() == 0) {
      // no input document, so we'll create a dummy one...
      docs.add(null);
    }
    if (docs.size() > 1 && !todir) {
      throw new BuildException("Multiple inputs (" + docs.size() + ") but only one output file");
    }
    if (dest == null && todir) {
      throw new BuildException("No output directory");
    }

    // now make the destination directory absolute...
    if (dest != null) {
      File fdest = new File(dest);
      if (!fdest.isAbsolute()) {
        dest = getPathPrefix() + dest;
      }
    }

    for (int d = 0; d < docs.size(); d++) {
      FileSpec fspec = (FileSpec)docs.get(d);
      String doc = null;
      if (fspec != null) {
        doc = fspec.name;
      }
      log("Processing " + (doc == null ? "" : doc) + (dest == null ? " [no output document]" : (" into " + dest)));
      Document document = null;
      try {
        if (doc != null) {
          document = documentFromFile(doc);
        }
        else {
          document = createDocument();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new BuildException(e.getMessage());
      }
      String destfile = doc;
      log("Writing " + destfile + " to " + dest);

      if (fspec != null) {
        // we strip down to the original filename to write out
        // to the destination (only if a dir)
        if (todir) {
          destfile = destfile.substring(fspec.base.length());
        }
      }
      processDoc(document, destfile);
    }
  }

  /**
   * process the document using the set of xml replacements
   * created for this task, and then output. Output depends
   * on whether directory or file output has been selected
   *
   * @param doc the document to work on
   * @param name the destination file
   * @throws BuildException
   */
  private void processDoc(Document doc, String name) throws BuildException {
    try {
      // get doctype info if required...
      DocumentType dt = doc.getDoctype();
      if (dt != null && preservetype) {
        log("Pub = " + dt.getPublicId());
        log("Sys = " + dt.getSystemId());
      }

      // now process...
      XmlReplacement replacement = new XmlReplacement(doc, this);
      for (int r = 0; r < replacements.size(); r++) {
        replacement.add((XmlReplace)replacements.get(r));
      }
      replacement.setReport(reporting);
      doc = replacement.apply();

      if (replacement.getFailures() > 0 && failWithoutMatch) {
        throw new BuildException("<xmltask> subtasks failed to find matches");
      }

      if (dest != null) {
        // and then write out...
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        if (preservetype) {
          // use the document's
          if (dt != null) {
            // but I don't want to set PUBLIC and SYSTEM = "" i.e. both blank
            if (dt.getPublicId() != null) {
              serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dt.getPublicId());
            }
            else {
              serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");
            }
            if (dt != null && dt.getSystemId() != null) {
              serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            else {
              serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "");
            }
          }
        }
        else {
          // use the configured... (if configured!)
          if (doctype_public != null) {
            serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype_public);
          }
          if (doctype_system != null) {
            serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype_system);
          }
        }

        if (normalize) {
          log("Normalizing resultant document");
          doc.getDocumentElement().normalize();
        }
        if (indent) {
          log("Indenting resultant document");
          serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        }
        else {
          serializer.setOutputProperty(OutputKeys.INDENT, "no");
        }

        // and output
        Writer w = null;
        if (todir == false) {
          w = getWriter(dest, serializer);
        }
        else {
          String destname = dest + File.separator + name;
          log("Writing " + destname);
          File dir = (new File(destname)).getParentFile();
          if (!dir.exists()) {
            if (!dir.mkdirs()) {
              throw new Exception("Failed to make destination directory " + dest);
            }
          }
          w = getWriter(destname, serializer);
        }

        Result res = null;
        if (FMT_NONE.equals(outputter)) {
          res = new StreamResult(w);
        }
        else if (outputter.startsWith(FMT_SIMPLE)) {
          FormattedDataWriter dw = new FormattedDataWriter();
          dw.setWriter(w);
          dw.setIndentStep(2);
          if (outputter.indexOf(":") != -1) {
            // looks like it's formatted as simple:{indent}...
            String fmt = outputter.substring(outputter.indexOf(":") + 1);
            dw.setIndentStep(Integer.parseInt(fmt));
          }
          dw.setTransformer(serializer);
          res = new SAXResult(dw);
        }
        else {
          // try and load this as a custom task...
          log("Loading custom result writer " + outputter);
          Outputter op = (Outputter)(Class.forName(outputter).newInstance());
          op.setWriter(w);
          op.setTransformer(serializer);
          res = new SAXResult(op);
        }
        serializer.transform(new DOMSource(doc), res);
        w.close();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new BuildException("Can't create " + dest);
    }
    catch (Exception e) {
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
   * @param filename
   * @param serializer
   * @return the resultant writer
   * @throws IOException
   */
  private Writer getWriter(String filename, Transformer serializer) throws IOException {
    String enc = outputEncoding;
    if (enc == null) {
      enc = encoding;
    }
    if (enc != null) {
      log("Using output character encoding " + enc);
      serializer.setOutputProperty(OutputKeys.ENCODING, enc);
      return new OutputStreamWriter(new FileOutputStream(filename), enc);
    }
    else {
      return new FileWriter(filename);
    }
  }

  // create the task elements below

  public Replace createReplace() {
    return new Replace(this);
  }

  public Remove createRemove() {
    return new Remove(this);
  }

  public Attr createAttr() {
    return new Attr(this);
  }

  public Insert createInsert() {
    return new Insert(this);
  }

  public void addConfiguredCopy(Copy copy) {
    copy.process(this);
  }

  public void addConfiguredCut(Cut cut) {
    cut.process(this);
  }

  public Insert createPaste() {
    return new Insert(this);
  }

  public Rename createRename() {
    return new Rename(this);
  }

  public Entity createEntity() {
    return new Entity(this);
  }

  public void registerEntity(String remote, String local) {
    resolver.registerEntity(this, remote, local);
  }

  public void setFailWithoutMatch(String f) {
    if ("on".equals(f) || "true".equals(f)) {
      failWithoutMatch = true;
    }
  }

  public boolean isFailWithoutMatch() {
    return failWithoutMatch;
  }
}
