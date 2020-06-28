package jcrystal.utils;

import jcrystal.preprocess.responses.OutputFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.TreeMap;
import jcrystal.preprocess.responses.OutputSection;


public class BlockBasedCodeSource {
		
    public ArrayList<String> _source = new ArrayList<>();
    public TreeMap<String, OutputSection> sections = new TreeMap<>();
    private int ultimoCierre = -1, level;
    public BlockBasedCodeSource(File file)throws IOException{
        ArrayList<String> code = new ArrayList<>(Files.readAllLines(file.toPath()));
        for(int e = 0; e < code.size(); e++){
      	  level += count(code.get(e), '{');
      	  level -= count(code.get(e), '}');
            if(OutputSection.isStartTag(code.get(e))){
                final String tagName = OutputSection.getTagName(code.get(e).trim());
                ArrayList<String> sec = new ArrayList<>();
                int i = e + 1;
                for(;i < code.size() && !OutputSection.isEndTag(code.get(i)); i++) {
                    if(OutputSection.isStartTag(code.get(i)))
                        throw new NullPointerException("Invalid code tag ");
                    sec.add(code.get(i));
                }
                if(i == code.size() || sections.containsKey(tagName))
                    throw new NullPointerException("Invalid code tag " + tagName);
                sections.put(tagName, new OutputSection(null, tagName, sec).setGlobal(level == 0));
                e = i;
            }else {
                if (level == 0 && ultimoCierre == -1 && code.get(e).trim().equals("}"))
                    ultimoCierre = _source.size();
                _source.add(code.get(e));
            }
            if(code.get(e).equals("//========================================== GENERATED =========================================="))
            	break;
        }
    }
    private int count(String line, char c) {
	    int ret = 0;
	    for(char x : line.toCharArray())
		    if(c == x)
			    ret++;
	    return ret;
    }
    public void save(File out)throws IOException{
	    if(ultimoCierre == -1) {
		    System.out.println(level+" "+ultimoCierre);
		    for(String h : _source)
			    System.out.println(h);
	    }
	    try(PrintWriter pw = new PrintWriter(out)){
		  for(int e = 0; e < ultimoCierre; e++)
		      pw.println(_source.get(e));
		  
		  sections.values().stream().filter(d->!d.global).forEach(f->{
			  pw.println("/* " + f.id + " */");
			  for(String line : f.contentList)
				  pw.println(line);
			  pw.println("/* END */");
		  });
		  for(int e = ultimoCierre; e < _source.size(); e++)
		      pw.println(_source.get(e));
		  sections.values().stream().filter(d->d.global).forEach(f->{
			  pw.println("/* " + f.id + " */");
			  for(String line : f.contentList)
				  pw.println(line);
			  pw.println("/* END */");
		  });
	    }
    }
    public void mergeInto(OutputFile nuevo) {
		for(int e = 0; e < nuevo.content.size(); e++) {
			if(OutputSection.isStartTag(nuevo.content.get(e))) {
				final String tagName = OutputSection.getTagName(nuevo.content.get(e));
				if(sections.containsKey(tagName)) {
					nuevo.content.addAll(e+1, sections.get(tagName).contentList);
					e += sections.get(tagName).contentList.size() + 1;
				}
			}
		}
	}

}
