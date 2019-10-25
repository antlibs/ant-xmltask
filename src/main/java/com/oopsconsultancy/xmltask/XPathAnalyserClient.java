package com.oopsconsultancy.xmltask;

import org.w3c.dom.Node;

public interface XPathAnalyserClient {

  public void applyNode(Node n, Object callback) throws Exception;
  public void applyNode(String str, Object callback) throws Exception;
}

