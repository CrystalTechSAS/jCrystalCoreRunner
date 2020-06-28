package jcrystal.preprocess.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jcrystal.types.IJType;
import jcrystal.types.JClass;
import jcrystal.types.JPackage;
import jcrystal.types.loaders.IJClassLoader;

public class ClassProcesor {
	private final IJClassLoader jClassLoader;
	public ClassProcesor(IJClassLoader jClassLoader) {
		this.jClassLoader = jClassLoader;
	}
	public JClass loadClassInfo(Class<?> clase) {
		try {
			JClass ret = new JClass(jClassLoader, clase);
			jClassLoader.load(ret);
			Stream.concat(Arrays.stream(clase.getDeclaredClasses()), Arrays.stream(clase.getClasses())).distinct()
				.forEach(c->{
					loadClassInfo(c);
				});
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public void loadPackageInfo(Package paquete) {
		JPackage ret = new JPackage(jClassLoader, paquete);
		jClassLoader.load(ret);
	}
	public void loadPackageInfo(JPackage paquete) {
		jClassLoader.load(paquete);
	}
	public void compleateLoading() {
		List<IJType> clases = new ArrayList<>(jClassLoader.getLoadedClasses().values());
		clases.stream().map(f->(JClass)f).forEach(c->{
			try {
				c.load(Class.forName(c.name()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
	}
}
 