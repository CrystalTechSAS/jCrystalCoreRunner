package jcrystal.process;

import jcrystal.local.JCrystalMetaConfig;

public interface ICoreRunner {
	boolean dojCrystalProcess(JCrystalMetaConfig config)throws Exception;
}
