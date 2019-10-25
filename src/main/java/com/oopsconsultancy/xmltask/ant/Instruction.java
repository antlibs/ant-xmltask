package com.oopsconsultancy.xmltask.ant;

/**
 * @author brian implements all instruction common methods
 */
public interface Instruction {

  void process(XmlTask xmltask) throws Exception;

  /**
   * sets a property determining execution
   * 
   * @param ifProperty
   */
  void setIf(String ifProperty);

  /**
   * sets a property determining execution
   * 
   * @param unlessProperty
   */
  void setUnless(String unlessProperty);

}
