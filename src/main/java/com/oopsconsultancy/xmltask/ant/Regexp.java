package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.RegexpAction;
import com.oopsconsultancy.xmltask.XmlReplace;

/**
 * performs regular expression work
 *
 * @author brianagnew
 *
 */
public class Regexp implements Instruction {

	private String ifProperty;
	private String unlessProperty;
	private XmlTask task;
	private String path;
	private String pattern;
	private String property;
	private String buffer;
	private String replace;
	private boolean caseSensitive = true;
	private boolean unicodeCase = false;

	public void process(final XmlTask xmltask) {
		this.task = xmltask;
		register();
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}

	public void setProperty(final String property) {
		this.property = property;
	}

	public void setBuffer(final String buffer) {
		this.buffer = buffer;
	}

	public void setReplace(final String replace) {
		this.replace = replace;
	}

	public void setCaseSensitive(final boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public void setUnicodeCase(final boolean unicodeCase) {
		this.unicodeCase = unicodeCase;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	/**
	 * builds the appropriate action
	 */
	private void register() {
		RegexpAction action = null;
		if (replace != null) {
			if (property != null || buffer != null) {
				throw new IllegalArgumentException("Can only specify one of replace/property/buffer for a regexp");
			}
			action = RegexpAction.createReplacement(task, pattern, replace);
		}
		if (property != null) {
			if (replace != null || buffer != null) {
				throw new IllegalArgumentException("Can only specify one of replace/property/buffer for a regexp");
			}
			action = RegexpAction.createCopyToProperty(task, pattern, property);
		}
		if (buffer != null) {
			if (replace != null || property != null) {
				throw new IllegalArgumentException("Can only specify one of replace/property/buffer for a regexp");
			}
			action = RegexpAction.createCopyToBuffer(task, pattern, buffer);
		}
		if (action == null) {
			throw new IllegalStateException("Failed to build a regexp action from inputs");
		}
		action.setCaseInsensitive(!caseSensitive);
		action.setUnicodeCase(unicodeCase);
		XmlReplace xmlReplace = new XmlReplace(path, action);
		xmlReplace.setIf(ifProperty);
		xmlReplace.setUnless(unlessProperty);
		task.add(xmlReplace);
	}

	public void setIf(final String ifProperty) {
		this.ifProperty = ifProperty;

	}

	public void setUnless(final String unlessProperty) {
		this.unlessProperty = unlessProperty;
	}
}
