package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;

public interface XPathAnalyser {

  public void registerClient(XPathAnalyserClient client, Object callback);

  public int analyse(Node node, String xpath) throws Exception;
}
