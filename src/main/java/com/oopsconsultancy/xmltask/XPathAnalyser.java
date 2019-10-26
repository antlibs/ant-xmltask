package com.oopsconsultancy.xmltask;

import org.w3c.dom.Node;

public interface XPathAnalyser {

  void registerClient(XPathAnalyserClient client, Object callback);

  int analyse(Node node, String xpath) throws Exception;
}
