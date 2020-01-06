package com.oopsconsultancy.xmltask;

import org.w3c.dom.Node;

public interface XPathAnalyserClient {
  void applyNode(Node n, Object callback);
  void applyNode(String str, Object callback);
}
