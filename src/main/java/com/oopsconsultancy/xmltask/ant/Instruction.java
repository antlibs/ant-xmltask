package com.oopsconsultancy.xmltask.ant;

/**
 * @author brian implements all instruction common methods
 */
public interface Instruction {

  void process(XmlTask xmltask);

  /**
   * sets a property determining execution
   *
   * @param ifProperty String
   */
  void setIf(String ifProperty);

  /**
   * sets a property determining execution
   *
   * @param unlessProperty String
   */
  void setUnless(String unlessProperty);

}
