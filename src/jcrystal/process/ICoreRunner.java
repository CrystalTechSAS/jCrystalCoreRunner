package jcrystal.process;

import java.util.TreeSet;

import jcrystal.local.JCrystalMetaConfig;
import jcrystal.preprocess.main.MainPreprocessor;

public interface ICoreRunner {
	void setup(MainPreprocessor preprocessor, TreeSet<String> CHECKED_CLASSES);
	boolean dojCrystalProcess(JCrystalMetaConfig config)throws Exception;
}
