package jcrystal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jcrystal.preprocess.responses.ClassOperation;
import jcrystal.preprocess.responses.ClassOperationType;
import jcrystal.preprocess.responses.OutputSection;

public class JavaBlockChain{

	Block root;
	TreeMap<String, OutputSection> sections = new TreeMap<>();
	
	public JavaBlockChain(File f) throws FileNotFoundException, IOException {
		BlockBuilder builder = new BlockBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(f), 4*1024)){
			for(String line;(line=br.readLine())!=null;) {
				builder.process(this, line);
			}
			builder.process((char)0);
		}
		this.root = builder.actual;
		getClassDefStatement().normalizeHeader();
		getFileHeaderStatement().normalizeFileHeader();
	}
	private Statement getFileHeaderStatement() {
		for(int e  = 0; e < root.content.size(); e++) {
			if(root.content.get(e) instanceof Block || root.content.get(e) instanceof AnnotationBlock)
				return (Statement)root.content.get(e-1);
		}
		return null;
	}
	private Statement getClassDefStatement() {
		for(int e  = 0; e < root.content.size(); e++) {
			if(root.content.get(e) instanceof Block)
				return (Statement)root.content.get(e-1);
		}
		return null;
	}
	public void merge(List<ClassOperation> sections) {
		Block last = getLastBlock();
		sections.forEach(s->{
			switch (s.type) {
				case ADD_SECTION:
					OutputSection newS = (OutputSection)s;
					OutputSection prev = this.sections.get(newS.id);
					if(prev != null)
						prev.contentList = s.contentList;
					else {
						if(newS.global)
							root.content.add(new SectionBlock(newS));
						else if(last != null)
							last.content.add(new SectionBlock(newS));
					}					
					break;
				case ADD_IMPORT:
					getFileHeaderStatement().addImport(s.content);
					break;
				case REMOVE_IMPORT:
					getFileHeaderStatement().removeImport(s.content);
					break;
				case ADD_IMPLEMENTS:
					getClassDefStatement().addImplements(s.content);
					break;
				case REMOVE_IMPLEMENTS:
					getClassDefStatement().removeImplements(s.content);
					break;
				default:
					break;
			}
		});
	}
	private Block getLastBlock() {
		Block last = null;
		for(IBlock b : root.content)
			if(b instanceof Block)
				last = (Block)b;
		return last;
	}
	public static void main(String[] args) throws Exception{
		File f = new File("/Users/gasotelo/Documents/repos/jcrystal/jCrystal/src/jcrystal/utils/BlockBasedCodeSource.java");
		//File f = new File("/Users/gasotelo/Documents/repos/together/Torrenegra-Together-back/src/main/java/entities/VideoMailsCount.java");
		JavaBlockChain j = new JavaBlockChain(f);
		j.getClassDefStatement().addImplements("Hola");
		j.getClassDefStatement().removeImplements("Comparable<BlockBasedCodeSource>");
		j.getClassDefStatement().removeImplements("Runnable");
		j.getClassDefStatement().removeImplements("Serializable");
		j.getFileHeaderStatement().addImport("jcrystal.utils.Entity");
		j.getFileHeaderStatement().removeImport("java.nio.file.Files");
		System.out.println(j.root);
	}
	
	public void save(File out)throws IOException{
		try(PrintWriter pw = new PrintWriter(out)){
			root.print(pw);		
		}
	}
}
class BlockBuilder{
	Block actual = new Block(null);
	List<String> buffer = new ArrayList<>();
	StringBuffer sb = new StringBuffer(1024);
	char last = 0;
	State state = State.PLAIN;
	OutputSection actualSection;
	public void process(JavaBlockChain parent,String line) {
		if(OutputSection.isEndTag(line)) {
			actualSection = null;
		}else if(OutputSection.isStartTag(line)) {
	            addToBuffer(sb.toString());
	            addStatement();
			final String tagName = OutputSection.getTagName(line.trim());
			actualSection = new OutputSection(null, tagName, new ArrayList<>());
			actual.content.add(new SectionBlock(actualSection));
	            parent.sections.put(tagName, actualSection);
		}else if(actualSection != null) {
			actualSection.contentList.add(line);
		}else {
			for(char c : line.toCharArray())
				process(c);
			for(char c : System.lineSeparator().toCharArray())
				process(c);
		}
	}
	int parentesisAnnotation = -1;
	public void process(char x) {
		if(x == 0) {
			buffer.add(sb.toString());
			actual.content.add(new Statement(actual, buffer));
			buffer = new ArrayList<>();
			return;
		}
		switch (state) {
			case PLAIN:
				if(x == '"') {
					addToBuffer(sb.toString());
					sb.append(x);
					state = State.STRING;
				}else if(x == '/' && last == '/') {
					addToBuffer(sb.substring(0, sb.length()-1));
					sb.append(x);
					sb.append(x);
					state = State.LINE_COMMENT;
				}else if(x == '*' && last == '/') {
					addToBuffer(sb.substring(0, sb.length()-1));
					sb.append('/');
					sb.append(x);
					state = State.COMMENT;
				}else if(x=='{') {
					addToBuffer(sb.toString());
					addStatement();
					actual = new Block(actual);
					actual.parent.content.add(actual);
				}else if(x=='}') {
					addToBuffer(sb.toString());
					addStatement();
					actual = actual.parent;
				}else if(x == '@') {
					parentesisAnnotation = -1;
					addToBuffer(sb.toString());
					addStatement();
					sb.append(x);
					state = State.ANNOTATION;
				}else if(x==';') {
					sb.append(x);
					addToBuffer(sb.toString());
					//addStatement();
				}
				else 
					sb.append(x);
				break;
			case ANNOTATION_STRING:
				sb.append(x);
				if(last != '\\' && x == '"')
					state = State.ANNOTATION;
				break;
			case ANNOTATION:
				if(x == '"') {
					sb.append(x);
					state = State.ANNOTATION_STRING;
				}else if(parentesisAnnotation == -1 && x == '(') {
					sb.append(x);
					parentesisAnnotation = 1;
				}else if(parentesisAnnotation == -1 && Character.isWhitespace(last) && !Character.isWhitespace(x) && x != '(') {
					addAnnotation();
					state = State.PLAIN;
					process(x);
				}else {
					sb.append(x);
					if(x == '(')
						parentesisAnnotation++;
					if(x == ')')
						parentesisAnnotation--;
					if(parentesisAnnotation == 0) {
						addAnnotation();
						state = State.PLAIN;
					}
				}
				break;
			case COMMENT:
				sb.append(x);
				if(last == '*' && x == '/') {
					addToBuffer(sb.toString());
					state = State.PLAIN;
				}
				break;
			case LINE_COMMENT:
				sb.append(x);
				if(x == '\n' || x == '\r') {
					addToBuffer(sb.toString());
					state = State.PLAIN;
				}
				break;
			case STRING:
				sb.append(x);
				if(last != '\\' && x == '"') {
					state = State.PLAIN;
					addToBuffer(sb.toString());
				}
				break;
			default:
				break;
		}
		last = x;
	}
	private void addToBuffer(String val) {
		if(val.length() > 0)
			buffer.add(val);
		sb.setLength(0);
	}
	private void addAnnotation() {
		actual.content.add(new AnnotationBlock(actual,sb.toString()));
		sb.setLength(0);
		
	}
	private void addStatement() {
		if(!buffer.isEmpty())
			actual.content.add(new Statement(actual, buffer));
		buffer = new ArrayList<>();
	}
}
enum State{
	COMMENT, LINE_COMMENT, STRING, PLAIN, ANNOTATION, ANNOTATION_STRING
}