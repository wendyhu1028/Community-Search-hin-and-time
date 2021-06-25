package dblp;

/*
 * Created on 2021.06.25 by Wenyi Hu
 */

public class Config {
	
	//edge type
	public static final int P2T = 0;
	public static final int P2A = 1;
	public static final int A2P = 2;
	public static final int T2P = 3;
	
	//vertex type
	public static final int AUTHOR = 0;
	public static final int PAPER = 1;
	public static final int TERM = 2;
	
	//file path
	public static String stopFile = "E:\\data\\stoplist.txt";
	public static String outputPath = "E:\\data\\dblp\\vldb";

}
