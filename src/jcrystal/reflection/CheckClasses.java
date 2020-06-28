package jcrystal.reflection;

import java.util.TreeSet;

public class CheckClasses {
	static String[] CLASSES = {
		"com.google.appengine.tools.cloudstorage.GcsFileOptions",
		"com.google.appengine.tools.cloudstorage.GcsService",
		"javax.servlet.http.HttpServlet"
	}; 
	public static TreeSet<String> load() {
		TreeSet<String> CHECKED_CLASSES = new TreeSet<>();
		for(String h : CLASSES) {
			try {
				if(Class.forName(h) != null)
					CHECKED_CLASSES.add(h);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return CHECKED_CLASSES;
	}
}
