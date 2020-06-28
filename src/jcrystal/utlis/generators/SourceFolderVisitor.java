package jcrystal.utlis.generators;

import java.io.File;
import java.util.function.BiConsumer;

public class SourceFolderVisitor {

	public static void preCargarClases(File srcFolder, BiConsumer<String, String> consumer){
		for (File h : srcFolder.listFiles())
			preCargarClases(h, null, consumer);
	}
	private static void preCargarClases(File f, String paquete, BiConsumer<String, String> consumer){
		if(f.getName().equals("gen") || f.getName().startsWith("AbsManager"))
			return;
		else if (f.isDirectory()) {
			String packageName = (paquete == null?"":(paquete+".")) + f.getName();
			consumer.accept(packageName, null);
			for (File h : f.listFiles())
				preCargarClases(h, packageName, consumer);
		} else if (f.getName().endsWith(".java")) {
			String className = (paquete != null ? paquete + "." : "") + f.getName().replace(".java", "");
			consumer.accept(paquete, className);
		}
	}
	
	
	
}
