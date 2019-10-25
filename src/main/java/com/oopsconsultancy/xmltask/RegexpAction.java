package com.oopsconsultancy.xmltask;

import com.oopsconsultancy.xmltask.ant.XmlTask;
import org.apache.tools.ant.Project;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * performs a regular expression action. Only on text nodes
 *
 * @author brianagnew
 */
public class RegexpAction extends Action {

	private final String ptrn;
	private final String replace;
	private final String property;
	private final String buffer;
	private XmlTask task;
	private boolean caseInsensitive;
	private boolean unicodeCase;

	/**
	 * ctor
	 *
	 * @param task XmlTask
	 * @param pattern String
	 * @param replace String
	 * @param property String
	 * @param buffer String
	 */
	private RegexpAction(final XmlTask task, final String pattern,
			final String replace, final String property, final String buffer) {
		this.task = task;
		if (pattern == null) {
			throw new IllegalArgumentException("Must specify a pattern");
		}
		this.ptrn = pattern;
		this.replace = replace;
		this.property = property;
		this.buffer = buffer;
	}

	/**
	 * does the work. Matches, writes to properties/buffers or performs a
	 * replacement
	 *
	 * @param node Node
	 * @return boolean
	 */
	public boolean apply(final Node node) throws Exception {
		// performs a match on the node and replaces, or writes to
		// a property or buffer

		// works on text and attributes...
		if (node instanceof Element) {
			Element e = (Element) node;
			NodeList nl = e.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				apply(nl.item(i));
			}
		} else if (TextAction.isTextNode(node)) {
			String str = performRegexp(node);
			if (str != null) {
				node.setNodeValue(str);
			}
		} else if (node instanceof Attr) {
			String str = performRegexp(node);
			if (str != null) {
				((Attr) node).setValue(str);
			}
		}
		return false;
	}

	/**
	 * performs the regexp and returns a replacement string if required,
	 * otherwise null. Handles property and buffer updating
	 *
	 * @param node Node
	 * @return String
	 */
	private String performRegexp(final Node node) {

		int flags = 0;
		if (caseInsensitive) {
			flags |= Pattern.CASE_INSENSITIVE;
		}
		if (unicodeCase) {
			flags |= Pattern.UNICODE_CASE;
		}
		Pattern pattern = Pattern.compile(ptrn, flags);
		String str = node.getNodeValue();
		if (str == null) {
			return null;
		}
		if (replace != null) {
			Matcher m = pattern.matcher(str);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(sb, replace);
			}
			m.appendTail(sb);

			return sb.toString();
		} else if (property != null) {
			Matcher m = pattern.matcher(str);
			if (m.matches()) {
				String value = getGroupedOrMatched(m);
				task.log("Setting property " + property + "=" + value
						+ " using '" + pattern.pattern() + "'",
						Project.MSG_VERBOSE);
				task.getProject().setNewProperty(property, value);
			} else {
				throw new IllegalStateException(
						"Failed to match property value in '" + str
								+ "' using '" + pattern.pattern() + "'");
			}
		} else if (buffer != null) {
			Matcher m = pattern.matcher(str);
			if (m.matches()) {
				String value = getGroupedOrMatched(m);
				task.log("Setting buffer " + buffer + "=" + value + " using '"
						+ pattern.pattern() + "'", Project.MSG_VERBOSE);
				Text newnode = node.getOwnerDocument().createTextNode(value);
				BufferStore.set(buffer, newnode, false, task);
			} else {
				throw new IllegalStateException(
						"Failed to match buffer value in '" + str + "' using '"
								+ pattern.pattern() + "'");
			}
		}
		return null;
	}

	private String getGroupedOrMatched(final Matcher m) {
		if (m.groupCount() > 0) {
			return m.group(1);
		}
		return m.group();
	}

	public String toString() {
		return "RegexpAction()";
	}

	/**
	 * builds a regexp action to copy to a property
	 *
	 * @param task XmlTask
	 * @param pattern String
	 * @param property String
	 * @return RegexpAction
	 */
	public static RegexpAction createCopyToProperty(final XmlTask task,
			final String pattern, final String property) {
		return new RegexpAction(task, pattern, null, property, null);
	}

	/**
	 * factory for replacement method
	 *
	 * @param task XmlTask
	 * @param pattern String
	 * @param replace String
	 * @return RegexpAction
	 */
	public static RegexpAction createReplacement(final XmlTask task,
			final String pattern, final String replace) {
		return new RegexpAction(task, pattern, replace, null, null);
	}

	/**
	 * builds a regexp action to copy to a property
	 *
	 * @param task XmlTask
	 * @param pattern String
	 * @param buffer String
	 * @return RegexpAction
	 */
	public static RegexpAction createCopyToBuffer(final XmlTask task,
			final String pattern, final String buffer) {
		return new RegexpAction(task, pattern, null, null, buffer);
	}

	public void setCaseInsensitive(final boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	public void setUnicodeCase(final boolean unicodeCase) {
		this.unicodeCase = unicodeCase;
	}
}
