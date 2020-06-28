package jcrystal.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Block implements IBlock{
	List<IBlock> content = new ArrayList<>();
	Block parent;
	public Block(Block parent) {
		this.parent = parent;
	}
	public IBlock parent() {
		return parent;
	}
	@Override
	public void print(PrintWriter pw) throws IOException {
		if(parent != null)
			pw.print("{");
		for(IBlock b : content)
			b.print(pw);
		if(parent != null)
			pw.print("}");
	}
	@Override
	public String toString() {
		String ret = "";
		for(IBlock b : content) {
			if(b instanceof Block) {
				ret += "-{" + b + "}";
			}else
				ret += b;
		}
		return ret;
	}
}
