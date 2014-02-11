import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Comparator;
import java.util.HashMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;


/**
 * Searcher class
 * Takes inputs from User and searches in Index
 * Index file directory is specified in arguments
 * @author sushant
 *
 */
public class Searcher {
	
	private HashMap<String, Long> dictionary;
	private long totalWord;
	private static String outDirName;
	public void setOutDir( String outFile ) {
		outDirName=outFile;
	}
	public Searcher() {
		outDirName = "";
		dictionary= new HashMap<String, Long>();
		totalWord = 116644; 
	}
	/**
	 * Read the third level index into memory
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void readOffsets() throws NumberFormatException, IOException {
		
 		try {
			BufferedReader recR = new BufferedReader( new FileReader( new String( outDirName+"/noOfRecs" ) )  );
			String l;
			l=recR.readLine();
			totalWord = Long.parseLong(l);
		} catch (Exception e1) {}
		
		int index;
		StringBuilder word  = new StringBuilder();
		StringBuilder offSet = new StringBuilder();
		Long longValue;
		try {
			BufferedReader thirdReader = new BufferedReader( new FileReader( outDirName+"/thirdIndex" )  );

			String line;
	
			while ((line = thirdReader.readLine()) != null) {
				index = line.indexOf('=');
				word.setLength(0);
				word.append(line.toCharArray(), 0, index);
				offSet.setLength(0);
				index++;
				offSet.append(line.toCharArray(), index, line.length()-index);
				longValue = Long.parseLong(offSet.toString());
				dictionary.put(word.toString(), longValue);
			}
			thirdReader.close();
			
		}
		catch( Exception e ){} 
		

	}
	/**
	 * Main Search function
	 * Gets input query , process the query, generates tokens and searches them
	 * Stores posting list into hashmap and finally takes the intersection of all posting lists
	 * @param args
	 * @throws Exception
	 */
	public void search( String args[] ) throws Exception {
		int i,j;
		char c='a';		StringBuilder word=new StringBuilder();
		int len,q=0;
		String queries[]=new String[10];
		Map<Long, Double> tfIntersection = new HashMap<Long, Double>();
		try {
			setOutDir(args[0]);
			readOffsets();
		
			StopWords stpWrd = new StopWords();
			Stemmer stem = new Stemmer();
			StringBuilder lower = new StringBuilder();
			StringBuilder readQ = new StringBuilder();
			Scanner intScanner = new Scanner(System.in);
			System.out.print("Enter number of Queries : ");
			int query=intScanner.nextInt();
			while( query-- > 0 ) {
				q=0;
				Scanner userInputScanner = new Scanner(System.in);
				readQ.setLength(0);
				System.out.print("Enter Query : ");
				readQ.append( userInputScanner.nextLine().toString().toLowerCase() );
				long time = System.currentTimeMillis();
				String []ip=readQ.toString().split("\\s+");
				for( i=0; i<ip.length;i++) {
					int code = 5;
					lower.setLength(0);
					if( ip[i].indexOf(':')==-1)
						lower.append(ip[i]);
					else {
						c = ip[i].charAt(0);
						switch(c) {
						case 't': code=0; break;
						case 'i': code=1; break;
						case 'r': code=2; break;
						case 'l': code=3; break;
						case 'c': code=4; break;
						default : code=5; break;
						}
						lower.append(ip[i].toCharArray(),2,ip[i].length()-2);
					}
					len=lower.length();
			
			
					word.setLength(0);
					j=0;
					while( j<len ){
						c = lower.charAt(j);
						j++;
						if( (c >='a'&& c<='z') || (c>='0' && c<='9') )
							word.append(c);
						else break;
					}		
					//query tokenised into word
					
					if( stpWrd.isStopWord(word) == false ) {
						stem.stemDriver(word);
						queries[q++]=word.toString();
						readQ.setLength(0);
						readQ.append( startSearch(word) );
						
						if( readQ.length()>0) {
							
							if( tfIntersection.size()==0 )
								tfIntersection = getTfIDF(readQ,code);
							else {
								//intersection using the retainAll function
								tfIntersection.keySet().retainAll( getTfIDF(readQ,code).keySet() );
							}
						}
					}
				}
				//sorting on TFIDF values
				valueComparator mycomp = new valueComparator(tfIntersection);
				TreeMap<Long,Double> sorted = new TreeMap<Long,Double>(mycomp);
				sorted.putAll( tfIntersection );
				//System.out.println(sorted);
				//System.out.println(tfIntersection);
				int k=0;
				for(Entry<Long, Double> entry : sorted.entrySet()) {
					Long id=entry.getKey();
					//System.out.println(id);
					String title=getTitleName( id.intValue() );
					String lowercase = title.toLowerCase();
					if( title.contains("Wikipedia") || title.contains("WikiProject")||lowercase.contains("file"))
						continue;
					if( title!="") {
						System.out.println(title);
						k++;
					}
					if( k==10)
						break;
				}
				System.out.println((System.currentTimeMillis() - time) / 1000f + " sec");
			}
		}
		catch( Exception e) {e.printStackTrace();}
	}
	/**
	 * Comparator to sort the intersected hashmap on basis of Values
	 * @author sushant
	 *
	 */
	class valueComparator implements Comparator<Long> {
		Map<Long,Double> pair;
		public valueComparator(Map<Long,Double> pair) { this.pair=pair;}
		public int compare(Long a,Long b) {
			if( pair.get(a) >= pair.get(b) ) 
				return -1;
			else
				return 1;
		}		
	}
	/**
	 * Return the TFIDF hash map for particular String posting list
	 * DocId is long and TFIDF is double
	 * @param line : The posting list in the form of Stringbuilder
	 * @param code : code of tag to search in (to implement in future : For Field Queries) 
	 * @return
	 */
	private HashMap<Long, Double> getTfIDF(StringBuilder line, int code) {
		HashMap<Long, Double> tfidf = new HashMap<Long, Double>();
		
		int off=0,len=line.length();
		long docId=0;
		double idf=0, tf=0;
		int df=0;
		char c;
		StringBuilder key = new StringBuilder();
		while( off<len ) {
			if( line.charAt(off)==',')
				df++;
			off++;
		}
		double valueLog = Math.log10( totalWord/df );
		off=0;
		while( off<len ) {
			c=line.charAt(off);

			if( c=='!') {
				
				docId = Long.parseLong(key.toString());
				key.setLength(0);
			}
			else if( c==',' ){
				
				tf = Long.parseLong(key.toString());
				key.setLength(0);
				idf = tf*valueLog;
				
				tfidf.put(docId,idf);
				key.setLength(0);
			}
			else
				key.append(c);
			off++;
		}
		return tfidf;
	}

	/**
	 * Searches the word and returns the Result posting list in the form of a StringBuilder
	 * @param word
	 * @return
	 * @throws FileNotFoundException
	 */
	private StringBuilder startSearch(StringBuilder word) throws FileNotFoundException {
		Long secondOffSetLow = (long) 0;
		Long secondOffSetHi = (long) 0;
		StringBuilder line  = new StringBuilder();
		StringBuilder key  = new StringBuilder();
		char first = word.charAt(0);
		char second = word.charAt(0);
		line.append(first);
		if( word.length() > 1 ) {
			second = word.charAt(1);
			line.append(second);
			second++;
		}
		secondOffSetLow = dictionary.get( line.toString() );
		if( word.length() == 1 ) {
			first++;
			line.setCharAt(0, first);
			second='a';
			line.append('a');
		}
		else
			line.setCharAt(1, second);
		secondOffSetHi = dictionary.get(line.toString());
		while( secondOffSetHi==null ) {
			second++;
			line.setCharAt(1, second);
			secondOffSetHi = dictionary.get(line.toString());
		}
		if( secondOffSetLow == null ) {
			//System.out.println();
			line.setLength(0);
			return line;
		}
		File fileHandle = new File(outDirName+"/secondaryIndex");
		RandomAccessFile secIndex=new RandomAccessFile(fileHandle, "r");
		Long primaryOffset = (long) -1;
		try {
			
			long mid;
			long h = secondOffSetHi, low= secondOffSetLow;
			secondOffSetLow=(long)0;
			secondOffSetHi=secIndex.length();
			while( secondOffSetLow<secondOffSetHi ){
				mid= secondOffSetLow+(secondOffSetHi-secondOffSetLow)/2;
				secIndex.seek(mid);
				line.setLength(0);
				long seek;
				char c=(char)secIndex.read();
					while( c!='\n' && c!='\r') {
						seek = secIndex.getFilePointer()-2;
						if( seek>=0 )
							secIndex.seek(seek);
						else {
							secIndex.seek(0);
							break;
						}
						c = (char)secIndex.read();
					}
					line.append( secIndex.readLine() );
					if( line.length()==0)
						break;
					int index = line.toString().indexOf('=');
					key.setLength(0);
					key.append(line, 0, index);
					int comp=0;
					//System.out.println("read "+line+" actual "+word);
					if( (comp=word.toString().compareTo(key.toString())) == 0 ) {
						key.setLength(0);
						index++;
						key.append(line.toString().toCharArray(), index, line.length()-index );
						primaryOffset = Long.parseLong(key.toString());
						break;
					}
					else if( comp>0)
						secondOffSetLow=mid+1;
					else
						secondOffSetHi=mid-1;
			}
			if (primaryOffset==-1) {
				secIndex.seek(low);
				 while( low<=h ) {
					
					line.setLength(0);
					line.append( secIndex.readLine() );
					if( line.length()==0)
						break;
					int index = line.toString().indexOf('=');
					key.setLength(0);
					key.append(line, 0, index);
					if( (word.toString().compareTo(key.toString())) == 0 ) {
						key.setLength(0);
						index++;
						key.append(line.toString().toCharArray(), index, line.length()-index );
						primaryOffset = Long.parseLong(key.toString());
						break;
					}
					low += line.length();
							
				}
			}
			secIndex.close();
			if( primaryOffset ==-1 ) {
			
				line.setLength(0);
			}
			else {
				try {
					File primaryFile = new File(outDirName+"/primaryIndex");
					RandomAccessFile primIndex=new RandomAccessFile(primaryFile, "r");
					primIndex.seek(primaryOffset);
					line.setLength(0);
					line.append(primIndex.readLine());
				} catch (Exception e) { }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	/**
	 * Returns title name for docId
	 * Binary Search Implementation
	 * @param docId
	 * @return
	 * @throws IOException
	 */
	String getTitleName( int docId)  throws IOException {
		long mid,high, low=(long)0;
		File fileHandle = new File(outDirName+"/titles");
		RandomAccessFile titleFile=new RandomAccessFile(fileHandle, "r");
		high=titleFile.length();
		String res;
		//checking if title is in first record
		res=titleFile.readLine();
		String[] arr2=res.split("=");
        try {
			if(Integer.parseInt(arr2[0])==docId)
			    return arr2[1];
		} catch (NumberFormatException e1) {
			//e1.printStackTrace();
		}
        
		try {
			while(low<=high) { 
				mid=(low+high)/2;
 		        titleFile.seek(mid);
                	titleFile.readLine();
               		if((res=titleFile.readLine())!=null) {
	                    String[] arr=res.split("=");
		
	                    int comp=Integer.parseInt(arr[0]);
	                    if(comp==docId)
	                        return arr[1];
 	                   else if(comp<docId)
                                low=mid+1;
                   	    else
	                        high=mid-1;
                
                	}
                	else
	                    high=mid-1;
           	     
            		}	
		}
		catch(Exception e) {e.printStackTrace();}
		return "";
	}
}
