

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class LineProcess {
	StringBuilder word = new StringBuilder();
	StopWords stpWrd = new StopWords();
	Stemmer stem = new Stemmer();
	TreeMap<String, HashMap<Integer,PageScore> > posting = new TreeMap<String, HashMap<Integer,PageScore> >();
	int fileNo,noWords,fileNumber;
	String outFileName;
	
	public LineProcess() {
		word.setLength(0);
		fileNo=0;
		noWords=0;
		fileNumber = 0;
	}
	public void setOutDir( String outFile ) {
		//outFileName=outFile+"/indexDataFile";
		outFileName = outFile;
	}
	public void parseLine( StringBuilder line,int pageId , boolean isTitle ) throws IOException {
	
		int lenght = line.length();
		int i;
		char c;
		word.setLength(0);
		for( i=0; i<lenght; i++ ) {
			c = line.charAt(i);
			if( ( c >='a'&& c<='z' ) || (c >='A'&& c<='Z') )
				word.append(c);
			else {
				processWord( word, pageId, isTitle );
			}
		}
		processWord( word, pageId ,isTitle );
	}
	public void processWord( StringBuilder word,int pageId , boolean isTitle ) throws IOException {
		StringBuilder lower = new StringBuilder();
		
		lower.append(word.toString().toLowerCase());
		if( stpWrd.isStopWord(lower) == false ) {

			stem.stemDriver(lower);
			//System.out.println("After stem:"+lower);
			if( posting.containsKey(lower.toString()) ) {
				PageScore scorer = posting.get(lower.toString()).get(pageId);
				if( scorer==null ) 
					scorer = new PageScore();
					//System.out.println("Score is null");
				if( isTitle )
					scorer.t++;
				else 
					scorer.b++;
				posting.get(lower.toString()).put(pageId, scorer);

			}
			else {
				noWords++;
				PageScore scorer = new PageScore();
				if( isTitle )
					scorer.t++;
				else 
					scorer.b++;
				HashMap<Integer, PageScore > newWord = new HashMap<Integer, PageScore>();
				newWord.put(pageId,scorer);
				posting.put(lower.toString(), newWord);
			}
		}
		word.setLength(0);
	}
	public void displayPosting( ) {
		System.out.println("no of words : "+noWords);
	}

	public void dumpOnDisk() throws IOException {
		
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
            	for( Integer docId : content.keySet() ) {
                     PageScore pgr = content.get(docId);
                     line.append(docId.toString()+"t");
                     line.append(pgr.t );
                     line.append('b');
                     line.append(pgr.b );
                     line.append(',');
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
		/*
		File fileHandle = new File(outFileName);
		
        FileOutputStream fileOut = null;
        BufferedOutputStream byteOut = null;
        StringBuilder line = new StringBuilder();
        Iterator<Integer> iterator;
        
		if(!fileHandle.exists()) {
			fileHandle.createNewFile();		
	        try {
	        	fileOut = new FileOutputStream(fileHandle);
	            byteOut = new BufferedOutputStream(fileOut);
	            for( Map.Entry< String, TreeSet< Integer > > entry : posting.entrySet() ) {

	            	line.setLength(0);
	            	line.append(entry.getKey()+"=");
	            	TreeSet< Integer > content = entry.getValue();
	            	iterator = content.iterator(); 
	            	while (iterator.hasNext()){
	                     line.append( iterator.next().toString() + ",");
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
		else {
			StringBuilder word = new StringBuilder();
			StringBuilder getWord = new StringBuilder();
			StringBuilder pList = new StringBuilder();

			try {
				
				File tempfileHandle = new File(outFileName+"_temp");
				
	        	fileOut = new FileOutputStream(tempfileHandle);
	            byteOut = new BufferedOutputStream(fileOut);
				
				//FileWriter fw = new FileWriter(tempfileHandle.getAbsoluteFile());
				//BufferedWriter bw = new BufferedWriter(fw);
			
				FileReader fr = new FileReader(fileHandle.getAbsoluteFile());
				BufferedReader br = new BufferedReader(fr);
			
				String fLine = br.readLine();
				if( fLine==null )
					return;
				int index=fLine.indexOf('=');
				getWord.append(fLine.toCharArray(),0,index);
				
				Set eSet = posting.entrySet();
				Iterator itr = eSet.iterator();
				boolean nex = false;
				while( itr.hasNext() ) {
					if( nex==false ) {
						Map.Entry mapEntry = (Map.Entry)itr.next();
						word.setLength(0);
						word.append( mapEntry.getKey().toString() );
					}
					int c;
					if( (c=word.toString().compareTo(getWord.toString()) ) <0 ) {
						nex=false;
						TreeSet< Integer > content = posting.get(word.toString());
						word.append('=');
		            	iterator = content.iterator(); 
		            	while (iterator.hasNext()){
		                     word.append( iterator.next().toString() + ",");
		                }
		            	word.append('\n');
		            	byteOut.write(word.toString().getBytes());
		            	byteOut.flush();
		            	//bw.write(word.toString());
						continue;
					}
					else if(c>0)
					{
						nex=true;
						if(fLine==null)
							break;
						line.setLength(0);
						line.append(fLine);
						line.append('\n');
		            	byteOut.write(line.toString().getBytes());
		            	byteOut.flush();
						//bw.write(fLine);
						//bw.write("\n");
						fLine = br.readLine();
						
						if(fLine==null)
							break;
						index=fLine.indexOf('=');
						getWord.setLength(0);
						getWord.append(fLine.toCharArray(),0,index);
					}
					else {
						nex=false;
						TreeSet< Integer > content = posting.get(word.toString());
						iterator = content.iterator();
						pList.setLength(0);
		            	while (iterator.hasNext()){
		                     pList.append( iterator.next().toString() + ",");
		                }
		            	pList.append('\n');
						line.setLength(0);
						line.append(fLine+pList.toString());
		            	byteOut.write(line.toString().getBytes());
		            	byteOut.flush();
		            	//bw.write(fLine+pList.toString());
		            	fLine = br.readLine();
						
						if(fLine==null)
							break;
						index=fLine.indexOf('=');
						getWord.setLength(0);
						getWord.append(fLine.toCharArray(),0,index);
						continue;
					}
						
				}
				while(fLine!=null) {
					line.setLength(0);
					line.append(fLine);
					line.append('\n');
	            	byteOut.write(line.toString().getBytes());
	            	byteOut.flush();
					fLine=br.readLine();
				}
				while( itr.hasNext() ) {
					Map.Entry mapEntry = (Map.Entry)itr.next();
					word.setLength(0);
					word.append( mapEntry.getKey().toString() );
					TreeSet< Integer > content = posting.get(word.toString());
					word.append('=');
	            	iterator = content.iterator(); 
	            	while (iterator.hasNext()){
	                     word.append( iterator.next().toString() + ",");
	                }
	            	byteOut.write(word.toString().getBytes());
	            	byteOut.flush();
				}
				br.close();
				fileHandle.delete();
				tempfileHandle.renameTo(fileHandle);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
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
		}*/
	}
	public int getFileCount() {
		return fileNumber;
	}
}
