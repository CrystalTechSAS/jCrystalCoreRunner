package jcrystal.reflection;

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

import jcrystal.JCrystalConfig;
import jcrystal.configs.clients.Client;
import jcrystal.local.ClientPaths;
import jcrystal.local.JCrystalMetaConfig;
import jcrystal.local.LocalPaths;
import jcrystal.preprocess.main.MainPreprocessor;
import jcrystal.process.ICoreRunner;
import jcrystal.utlis.generators.SourceFolderVisitor;

public class GeneradorRutas {
	static TreeMap<String, Client> MAP_CLIENTES = new TreeMap<>();
	TreeSet<String> CHECKED_CLASSES;
	JCrystalMetaConfig config = JCrystalMetaConfig.getConfigFor(LocalPaths.ROOT);
	MainPreprocessor preprocessor = new MainPreprocessor();
	ICoreRunner coreRunner;
	public GeneradorRutas() {
	}
	public void generar() throws Exception{
		SourceFolderVisitor.preCargarClases(LocalPaths.getSrcFile(), preprocessor::loadFile);
		SourceFolderVisitor.preCargarClases(LocalPaths.getSrcUtils(), (paquete, f) -> {
			if(paquete.startsWith("jcrystal.clients."))
				preprocessor.loadFile(paquete, f);
		});
		preprocessor.classProcessor.compleateLoading();

		ClientPaths paths = ClientPaths.getConfig(LocalPaths.ROOT); 
		JCrystalConfig.CLIENT.list.forEach(f ->{
			if(f.output == null) {
				String path = paths.getClientPath(f.id);
				if(path != null)
					f.setOutput(path);
				else
					System.out.println("#blue:You have not path defined to client ("+f.id+"). Put it on your JCrystalConfig.java by using method setOutput.");
			}else {
				paths.setClientPath(f.id, f.output);
				System.out.println("#green:Path stored for client ("+f.id+").");
			}
			MAP_CLIENTES.put(f.id + " " + f.type, f);
		});
		paths.save();

		CHECKED_CLASSES = CheckClasses.load();
		if (config.getDeleteOnRun()) {
			int numberDeleted = deleteOldSrcUtilsFolder();
			System.out.println("jCrystal deleted "+ numberDeleted+" .java files in the src/main/utils folder.");
		}
		try {
			coreRunner.dojCrystalProcess(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		config.deleteRunProperties();
	}
	
	
	private int deleteOldSrcUtilsFolder() {
		File srcUtils = LocalPaths.getSrcUtils();
		return deleteJavaFiles(srcUtils);
	}
	private int deleteJavaFiles(File f) {
		int resp = 0;
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				resp += deleteJavaFiles(c);
		}
		if (f.getName().endsWith(".java")) {
			f.delete();
			resp++;
		}
		return resp;
	}
}