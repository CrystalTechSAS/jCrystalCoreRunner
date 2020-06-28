package jcrystal.utils;

import java.io.IOException;
import java.io.PrintWriter;

import jcrystal.preprocess.responses.OutputSection;

public class SectionBlock implements IBlock{
	OutputSection section;
	public SectionBlock(OutputSection section) {
		this.section = section;
	}
	@Override
	public IBlock parent() {
		return null;
	}
	@Override
	public void print(PrintWriter pw) throws IOException {
		pw.println("/* " + section.id + " */");
		for(String line : section.contentList)
			pw.println(line);
		pw.println("/* END */");
	}
}
