package jcrystal.utils;

import java.io.IOException;
import java.io.PrintWriter;

public interface IBlock {
	IBlock parent();
	void print(PrintWriter pw)throws IOException;
}
