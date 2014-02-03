import java.util.Scanner;


public class Searcher {
	
	//public static void main(String args[]) throws Exception {
	public void search( String args[] ) throws Exception {
		
		try {
			
			SecondIndex indexer = new SecondIndex();
			indexer.setOutDir(args[0]);
			indexer.readOffsets();
			
			StopWords stpWrd = new StopWords();
			Stemmer stem = new Stemmer();
			StringBuilder lower = new StringBuilder();
	
			Scanner intScanner = new Scanner(System.in);
			int query=intScanner.nextInt();
			while( query-- > 0 ) {
				
				Scanner userInputScanner = new Scanner(System.in);
				lower.setLength(0);
				lower.append( userInputScanner.nextLine().toString().toLowerCase() );
				long time = System.currentTimeMillis();
				int i;
				char c='a';
				int len=lower.length();
				for( i=0;i<len;i++ ) {
					c = lower.charAt(i);
					if( c >='a'&& c<='z' )
						break;
				}
				if( i==len ) {
					System.out.println();
					return;
				}
				StringBuilder word=new StringBuilder();
				do {
					word.append(c);
					i++;
					if( i==len )
						break;
					c = lower.charAt(i);
				}while( (c >='a'&& c<='z') || (c>='0' && c<='9') );
				if( stpWrd.isStopWord(word) == false ) {
					stem.stemDriver(word);
					indexer.searchBinaryInIndex(word.toString());
				}
				else {
					System.out.println();
				}
					
				System.out.println((System.currentTimeMillis() - time) / 1000f + " sec");
			}
		}
		catch( Exception e) {}
	}

}
