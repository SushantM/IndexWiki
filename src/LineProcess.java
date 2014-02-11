import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class gets the text segments from SAX parser and
 * tokenises them calls stopword removal for them 
 * then stemms the valid tokens
 * Also, dumps the content of Posting Treemap on disk when dump fuction is called
 * @author sushant
 *
 */
public class LineProcess {
	StringBuilder word = new StringBuilder();
	StopWords stpWrd = new StopWords();
	Stemmer stem = new Stemmer();
	TreeMap<String, HashMap<Integer,PageScore> > posting = new TreeMap<String, HashMap<Integer,PageScore> >();
	int fileNo,noWords,fileNumber;
	String outFileName;
	private int bracketCount;
	byte code;
	
	boolean bodyFlag,infoFlag,catagoryFlag,refFlag,
			linkFlag,valid,eqFlag;
	public LineProcess() {
		word.setLength(0);
		fileNo=0;
		noWords=0;
		fileNumber = 0;
		bracketCount = 0;
		code = 0;
		bodyFlag=false;
		infoFlag=false;
		catagoryFlag=false;
		refFlag=false;
		linkFlag=false;
		valid=false;
		eqFlag=false;
	}
	/**
	 * Set Output directory to given parameter
	 * @param outFile
	 */
	public void setOutDir( String outFile ) {
		outFileName = outFile;
	}
	/**
	 * Tokeniser Function 
	 * @param line : buffer given by SAX parser
	 * @param pageId : current page Id
	 * @param isTitle : if the buffer is related to title of Page
	 * @throws IOException 
	 */
	public void parseLine( StringBuilder line,int pageId , boolean isTitle ) throws IOException {
	
		int lenght = line.length();
		int i;
		char c;
		word.setLength(0);
		if( isTitle ) {		//title part
			bracketCount = 0;
			bodyFlag=false;
			infoFlag=false;
			catagoryFlag=false;
			refFlag=false;
			linkFlag=false;
			valid=false;
			eqFlag=false;
			code = 0;
			for( i=0; i<lenght; i++ ) {
				c = line.charAt(i);
				if( ( c >='a'&& c<='z' ) || (c >='A'&& c<='Z') )
					word.append(c);
				else {
					processWord( word, pageId, code );
				}
			}
			processWord( word, pageId , code );
		}
		else {
			// all other tags than title
			if(line.toString().contains("{{Infobox")) {
				infoFlag=true;
			}
			
			for( i=0; i<lenght; i++ ) {
				c = line.charAt(i);
				if( c=='{') {
					bracketCount++;
					continue;
				}
				if( c=='}') {
					bracketCount--;
					continue;
				}
				if( ( c >='a'&& c<='z' ) || (c >='A'&& c<='Z') || c=='=' || c=='[' || c==']' )
					word.append(c);
				else {
					if( bracketCount<2)
						infoFlag=false;
					if( infoFlag )
						code = 1;
					
						if( word.toString().contains("References")){ refFlag=true; code = 2;}
						if( word.toString().contains("==External")){ refFlag=false; linkFlag=true; code =3; }
						if( word.toString().contains("[[Category")){ linkFlag=false; catagoryFlag=true; code = 4;}
						if( word.length()>1&&catagoryFlag&& word.toString().charAt(word.length()-1)==']')
							{catagoryFlag=false;code=5;}
	
					if( word.length()!=0)
						processWord( word, pageId, code );
				}
			}
		
			processWord( word, pageId , code );
		}
	}
	/**
	 * Gets a token from ParseLine and 
	 * case folds it, checks it for Stopwords
	 * Stemms it
	 * if valid , calculates its Term Frequencies in various tags and adds to Posting list
	 * @param word
	 * @param pageId
	 * @param code : tag code ( 0-title, 1-infobox, 2-references, 3-external links, 4-catagories, 5-body )
	 * @throws IOException
	 */
	public void processWord( StringBuilder word,int pageId , byte code ) throws IOException {
		StringBuilder lower = new StringBuilder();
		
		lower.append(word.toString().toLowerCase());
		if( word.length()<3) {
			word.setLength(0);
			return;
		}
		if( word.length()>100) {
			word.setLength(0);
			return;
		}
		if( word.indexOf("[")!=-1 || word.indexOf("]")!=-1 || word.indexOf("=")!=-1) {
			int l=lower.length();
			word.setLength(0);
			char c;
			for( int i=0;i<l;i++) {
				c=lower.charAt(i);
				if( ( c >='a'&& c<='z' ) || (c >='A'&& c<='Z') )
					word.append(c);	
			}
			lower.setLength(0);
			lower.append(word);
		}
		if( lower.length()<3) {
			word.setLength(0);
			return;
		}

		if( stpWrd.isStopWord(lower) == false ) {

			stem.stemDriver(lower);
			if( posting.containsKey(lower.toString()) ) {
				PageScore scorer = posting.get(lower.toString()).get(pageId);
				if( scorer==null ) 
					scorer = new PageScore();
				switch( code ) {
					case 0 :  scorer.t++; break;
					case 1 :  scorer.i++; break;
					case 2 :  scorer.r++; break;
					case 3 :  scorer.e++; break;
					case 4 :  scorer.c++; break;
					default : scorer.b++; break;
						
				}
				posting.get(lower.toString()).put(pageId, scorer);

			}
			else {
				noWords++;
				PageScore scorer = new PageScore();
				switch( code ) {
					case 0 :  scorer.t++; break;
					case 1 :  scorer.i++; break;
					case 2 :  scorer.r++; break;
					case 3 :  scorer.e++; break;
					case 4 :  scorer.c++; break;
					default : scorer.b++; break;
						
				}
				HashMap<Integer, PageScore > newWord = new HashMap<Integer, PageScore>();
				newWord.put(pageId,scorer);
				posting.put(lower.toString(), newWord);
			}
		}
		word.setLength(0);
	}
	/**
	 * Content of Postings list tree map are dumped to a Temporary file
	 * Filename is tempDump_ + <current file number>
	 * @throws IOException
	 */
	public void dumpOnDisk() throws IOException {
		
		int score=0;
		StringBuilder fileName =new StringBuilder();
		fileName.append(outFileName);
		fileName.append("/tempDump_");
		fileName.append(fileNumber);
		fileNumber++;
		File tempFiletoDumpWords = new File(fileName.toString());
		if( !tempFiletoDumpWords.exists() )
			tempFiletoDumpWords.createNewFile();
       
		FileOutputStream fileOut = null;
        BufferedOutputStream byteOut = null;
        StringBuilder line = new StringBuilder();
        try {
        	fileOut = new FileOutputStream(tempFiletoDumpWords,false);
            byteOut = new BufferedOutputStream(fileOut);
            for( Map.Entry< String, HashMap< Integer, PageScore > > entry : posting.entrySet() ) {

            	line.setLength(0);
            	line.append(entry.getKey()+"=");
            	HashMap< Integer, PageScore > content = entry.getValue();
            	//status=(byte)0;
            	score=0;
            	for( Integer docId : content.keySet() ) {
                     PageScore pgr = content.get(docId);
                     line.append(docId.toString());
                     //byteOut.write(docId.toString().getBytes());
                     //byteOut.write(excl);
                     line.append('!');
                     //score is term frequency
                     // title is given a weight of 64 and etc
                     score+=pgr.t*64+pgr.b+pgr.i*2+pgr.e*3+pgr.r*4+pgr.c*4;
                     line.append(score);
                     //if(pgr.t!=0) {
                    	//line.append('t');
                    	 //status = 1;
                     //}
                     /*if( pgr.b!=0 ) {
                    	 temp=2;
                    	 score+=pgr.b;
                    	 status = (byte) (status | temp);
                    	 line.append('b');
                     }
                     if(pgr.i!=0) {
                    	 temp=4;
                    	 score+=pgr.i;
                    	 status = (byte) (status | temp);
                    	 line.append('i');
                     }
                     if(pgr.r!=0){
                    	 temp=8;
                    	 score+=pgr.r;
                    	 status = (byte) (status | temp);
                    	 line.append('r');
                     }
                     if(pgr.e!=0){
                    	 temp=16;
                    	 score+=pgr.e;
                    	 status = (byte) (status | temp);
                    	 line.append('l');
                     }
                     if(pgr.c!=0){
                    	temp=32;
                    	 score+=pgr.c;
                    	 status = (byte) (status | temp);
                    	 line.append('c');
                     }*/
                     //byteOut.write(score);
                    // line.append(score);
                   //  line.append(hash);
                   //  line.append(status);
                     line.append(',');
                     //byteOut.write(hash);
                     //byteOut.write(status);
                     //byteOut.write(comma);
            	}
            	//byteOut.write(nl);
            	int i=line.indexOf(",",130000);
            	if( i!=-1) {
            		StringBuilder newLine = new StringBuilder();
            		newLine.append(line.toString().toCharArray(),0,i);
            		line.setLength(0);
            		line.append(newLine);
            	}
            	line.append('\n');
            	byteOut.write(line.toString().getBytes());
            	byteOut.flush();
            }	
        }
        catch (FileNotFoundException e) {
        	e.printStackTrace();
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        finally {
        	if(byteOut!=null) {
            	try {
                	byteOut.close();
                }
                catch (IOException e) {
                	e.printStackTrace();
                }
            }
        }
	}
	/**
	 * Returns total number of Temporary files generated
	 * @return
	 */
	public int getFileCount() {
		return fileNumber;
	}
}
