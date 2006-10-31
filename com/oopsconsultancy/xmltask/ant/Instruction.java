package com.oopsconsultancy.xmltask.ant;

/**
 * @author brian
 * implements all instruction common methods
 */
public interface Instruction {
  
    void process(XmlTask xmltask) throws Exception;

}
