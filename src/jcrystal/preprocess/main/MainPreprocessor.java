package jcrystal.preprocess.main;

import jcrystal.types.JPackage;
import jcrystal.types.loaders.IJClassLoader;
import jcrystal.types.loaders.JClassLoader;


public class MainPreprocessor {
	public IJClassLoader jClassLoader = new JClassLoader();
	public ClassProcesor classProcessor = new ClassProcesor(jClassLoader);
	public void loadFile(String packagName, String className) {
		if(className == null || className.endsWith("package-info")) {
			Package p = Package.getPackage(packagName);
			if(p != null)
				classProcessor.loadPackageInfo(p);
			else 
				classProcessor.loadPackageInfo(new JPackage(jClassLoader, packagName));
		}else {
			try {
				classProcessor.loadClassInfo(Class.forName(className));
			}catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
}
