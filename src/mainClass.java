/**
 * Driver Class for Wikipedia Search Engine
 * Calls IndexCreator when 2 arguments ( XML file path and Output dir to store index) are given
 * Calls Searcher when 1 argument ( Output dir with index in it) is given, Searcher takes queries from user Input
 * @author sushant
 */
public class mainClass {
	
	public static void main ( String args[] ) throws Exception{
		if( args.length == 2 ) {		
			try {
				/*	for example
				 * args[0] = "XMLCorpus/sample.xml";
				 * args[1] = "IndexDirectory/"; 
				 */
				// Call index generator
				ReadXML indexCreator = new ReadXML();
				indexCreator.initiate(args);
			} catch (Exception e) {}
		}
		else if( args.length==1 ){		
			try {
				/*	for example
				 * args[0] = "IndexDirectory/"; 
				 */
				// Enter queries to Search in index
				Searcher seacher = new Searcher();
				seacher.search(args);
			} catch (Exception e) {	}
		}
		else {
			System.out.println("Please provide command line arguments");
			System.out.println("Format\nFor Index Generation : 2 arguments (XML file path, Directory to store index)");
			System.out.println("For Search : 1 argument (Directory with Index files in it)");
		}
	}
}
