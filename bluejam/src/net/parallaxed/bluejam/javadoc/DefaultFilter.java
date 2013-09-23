package net.parallaxed.bluejam.javadoc;

import com.sun.javadoc.ClassDoc;

public class DefaultFilter implements org.wonderly.doclets.ClassFilter {
	public boolean includeClass(ClassDoc cd)
	{
		if (cd.isException())
			return false;
		return true;
	}

}
