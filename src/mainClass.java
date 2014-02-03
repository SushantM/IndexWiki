
public class mainClass {
	
	public static void main ( String args[] ) throws Exception{
		if( args.length == 2 ) {
			
			try {
				ReadXML indexCreator = new ReadXML();
				indexCreator.initiate(args);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		else {		
			try {
				Searcher seacher = new Searcher();
				seacher.search(args);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
