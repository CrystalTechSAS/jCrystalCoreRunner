package jcrystal.reflection;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import jcrystal.JCrystalConfig;
import jcrystal.configs.clients.Client;
import jcrystal.configs.clients.ClientType;
import jcrystal.configs.clients.ResourceType;
import jcrystal.configs.server.Backend;
import jcrystal.local.LocalPaths;
import jcrystal.preprocess.responses.OutputFile;
import jcrystal.utils.BlockBasedCodeSource;

public class TareaEscrituraArchivo  implements Runnable {
	
	OutputFile output;

	public TareaEscrituraArchivo(OutputFile output) {
		this.output = output;
	}

	@Override
	public void run() {
		try {
			if (output.clientId == null) {
				File f = null;
				if (output.resourceType == null)
					f = new File(LocalPaths.getSrcUtils(), output.destPath);
				else if (output.resourceType == ResourceType.WEB_INF)
					f = new File(LocalPaths.getWebSrcfile(), "WEB-INF/" + output.destPath);
				f.getParentFile().mkdirs();
				Files.write(f.toPath(), output.content,
						StandardOpenOption.WRITE, StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			}
			else if(output.type == null) {
				Backend back = JCrystalConfig.SERVER.BACKENDS.get(output.clientId);
				if(back != null) {
					File f = null;
					if (back.output != null) {
						f = new File(back.output, output.destPath);
						f.getParentFile().mkdirs();
						Files.write(f.toPath(), output.content,
								StandardOpenOption.WRITE, StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING);
					}else
						System.out.println("#red:Internal error: unrecognized client id ("+output.clientId+").");
				}else
					System.out.println("#red:Internal error: unrecognized client id ("+output.clientId+").");
			} else {
				Client c = GeneradorRutas.MAP_CLIENTES.get(output.clientId + " " + output.type);
				if (c != null) {
					if (c.type == ClientType.ADMIN) {
						Path f = getClientPath(c).resolve(output.destPath);
						Files.createDirectories(f.getParent());
						Files.write(f, output.content,
								StandardOpenOption.WRITE, StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING);
					} else if(c.output != null) {
						Path f = getClientPath(c).resolve(output.destPath);
						Files.createDirectories(f.getParent());
						if(Files.notExists(f.getParent()))
							System.out.println("#red:Can't write to " + f);
						else if(Files.exists(f)) {
							BlockBasedCodeSource old = new BlockBasedCodeSource(f.toFile());
							old.mergeInto(output);
							Files.write(f, output.content,
								StandardOpenOption.WRITE, StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING);
						}else {
							Files.write(f, output.content,
									StandardOpenOption.WRITE, StandardOpenOption.CREATE,
									StandardOpenOption.TRUNCATE_EXISTING);
						}
					}
				}
			}

		} catch (Throwable e) {
			e.printStackTrace(System.out);
		}
	}
	private Path getClientPath(Client c){
		if(c.type == ClientType.ADMIN)
			return LocalPaths.getWebSrcfile().toPath().resolve("admin");
		return LocalPaths.ROOT.toPath().resolve(c.output);
	}
}
