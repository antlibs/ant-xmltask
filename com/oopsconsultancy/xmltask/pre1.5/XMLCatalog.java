/**
 * dummy xmlcatalog implementation for xmltask
 * running in Ant prior to version 1.5
 * OOPS Consultancy 2003
 * xmltask@oopsconsultancy.com
 * @version $Id$
 */
package org.apache.tools.ant.types;

import java.io.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.util.FileUtils;
import org.xml.sax.*;

public class XMLCatalog extends DataType implements Cloneable, EntityResolver
{

    public XMLCatalog()
    {
      System.out.println("Dummy pre-1.5 xml catalog");
    }

    public Path createClasspath()
    {
      return null;
    }

    public void setClasspath(Path classpath)
    {
    }

    public void setClasspathRef(Reference r)
    {

    }

    public void addDTD(DTDLocation dtd)
        throws BuildException
    {

    }

    public void addEntity(DTDLocation dtd)
        throws BuildException
    {

    }

    public void addConfiguredXMLCatalog(XMLCatalog catalog)
    {

    }

    public void setRefid(Reference r)
        throws BuildException
    {

    }

    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException
    {
      return null;

    }
}
