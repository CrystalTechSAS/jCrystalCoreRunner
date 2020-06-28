package jcrystal.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Statement implements IBlock{
	List<String> content = new ArrayList<>();
	IBlock parent;
	public Statement(IBlock parent, List<String> text) {
		this.content = text;
		this.parent = parent;
	}
	public IBlock parent() {
		return parent;
	}
	@Override
	public String toString() {
		return "+"+content.stream().collect(Collectors.joining());
	}
	@Override
	public void print(PrintWriter pw) throws IOException {
		for(String h : content)
			pw.print(h);
	}
	public void normalizeHeader() {
		int impPos = -1;
		for(int e = 0; e < content.size(); e++) {
			String text = content.get(e);
			if(text.startsWith("//") || text.startsWith("/*")) {
				if(impPos != -1)
					content.remove(e--);
			}else if(impPos != -1) {
				content.set(impPos, content.get(impPos)+" " + text);
				content.remove(e--);
			}else if(text.contains("implements")) {
				impPos = e;
			}else {
				content.set(e, text);
			}
		}
		if(impPos >= 0) {
			String text = content.get(impPos);
			int p = text.indexOf("implements") + "implements".length();
			content.set(impPos, text.substring(0, p) +" "+ normalizeParameters(text.substring(p)));
		}
	}
	public void normalizeFileHeader() {
		for(int e = 0; e < content.size(); e++) {
			String text = content.get(e);
			if(text.startsWith("//") || text.startsWith("/*"));
			else {
				text = text.replaceAll("import\\s+", "import ");
				text = text.replaceAll("\n[\\s&&[^\n]]+import\\s+", "\nimport ");
				text = text.replaceAll("\\s+;", ";");
				content.set(e, text);
			}
		}
	}
	public void removeImport(String classname) {
		for(int e = 0; e < content.size(); e++) {
			String text = content.get(e);
			String textN = text.replace("import " + classname+";", "");
			if(!text.equals(textN)) {
				if(textN.trim().isEmpty())
					content.remove(e--);
				else
					content.set(e, textN);
			}
		}
	}
	public void addImport(String classname) {
		int posPackage = -1;
		int indPackage = -1;
		int posImport = -1;
		int indImport = -1;
		for(int e = 0; e < content.size(); e++) {
			String text = content.get(e);
			if(text.contains("import " + classname))
				return;
			int posPac = text.indexOf("package");
			int posImp = text.lastIndexOf("import");
			if(posPac >= 0) {
				posPackage = text.indexOf(';', posPac);
				indPackage = e;
			}
			if(posImp >= 0) {
				posImport = text.indexOf(';', posPac);
				indImport = e;
			}
		}
		if(posPackage == -1 && posImport == -1)
			content.set(0, "import " + classname+";" + System.lineSeparator() + content.get(0));
		else if(posImport != -1)
			content.set(indImport, content.get(indImport).substring(0, posImport+1) + System.lineSeparator() + "import " + classname+";" + content.get(indImport).substring(posImport+1));
		else
			content.set(indPackage, content.get(indPackage).substring(0, posPackage+1) + System.lineSeparator() + "import " + classname+";" + content.get(indPackage).substring(posPackage+1));
	}
	public void addImplements(String classname) {
		int lastUtil = -1;
		for(int e = 0; e < content.size(); e++) {
			String text = content.get(e); 
			if(text.startsWith("//") || text.startsWith("/*"))
				continue;
			else if(text.contains("implements")) {
				if(!text.contains(classname))
					content.set(e, text.replace("implements", "implements " + classname+","));
				return;
			}
			lastUtil = e;
		}
		if(content.get(lastUtil).endsWith(" "))
			content.set(lastUtil, content.get(lastUtil) + "implements " + classname);
		else
			content.set(lastUtil, content.get(lastUtil) + " implements " + classname);
	}
	public void removeImplements(String classname) {
		for(int e = 0; e < content.size(); e++) {
			String text = content.get(e); 
			if(text.startsWith("//") || text.startsWith("/*"))
				continue;
			else if(text.contains("implements")) {
				if(text.contains(classname+","))
					text = text.replace(classname+",", "");
				else if(text.endsWith(classname))
					text = text.substring(0, text.length()-classname.length());
				int p = text.indexOf("implements") + "implements".length();
				String temp = normalizeParameters(text.substring(p));
				if(temp.length() == 0)
					content.set(e, text.substring(0, p - "implements".length()));
				else
					content.set(e, text.substring(0, p) +" "+ temp);
				return;
			}
		}
	}
	private String normalizeParameters(String text){
		String temp = text.replace("\n", " ").replaceAll("\r", " ").replaceAll("[\\s]", "");
		temp = temp.replace(",", " ").trim().replace(" ", ",");
		return temp.replace(",,", ",").replace(",", ", ").trim();
	}
}
