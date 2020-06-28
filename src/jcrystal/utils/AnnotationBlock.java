package jcrystal.utils;

import java.io.IOException;
import java.io.PrintWriter;

public class AnnotationBlock implements IBlock{

	Block parent;
	String text;
	public AnnotationBlock(Block parent, String text) {
		this.parent = parent;
		this.text = text;
	}
	@Override
	public IBlock parent() {
		return parent;
	}

	@Override
	public void print(PrintWriter pw) throws IOException {
		pw.print(text);
	}
	@Override
	public String toString() {
		return text;
	}
}