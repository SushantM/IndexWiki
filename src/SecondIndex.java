

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeSet;

public class SecondIndex {
	long[] offset=new long[27];
	int i;
	String outDirName;
	public void setOutDir( String outFile ) {
		outDirName=outFile;
	}
	public SecondIndex() {
		
	}
	public void getWordList() throws IOException {
		File fileHandle=new File(outDirName+"/WordList");
		if(fileHandle.exists())
			fileHandle.delete();
		fileHandle.createNewFile();
		BufferedWriter out=new BufferedWriter(new FileWriter(fileHandle,true));
		
		File reader=new File(outDirName+"/indexDataFile");
		BufferedReader br = new BufferedReader(new FileReader(reader));
		String line;
		StringBuilder word=new StringBuilder();
		
		while ((line = br.readLine()) != null) {
			
			i=line.indexOf('=');
			word.setLength(0);
			word.append(line.toCharArray(),0,i);
			out.write(word.toString());
			out.newLine();
		}
		br.close();
		out.close();
	}
	public void readOffsets() throws NumberFormatException, IOException {
		try {
			File fileHandle=new File(outDirName+"/secondaryIndex");
			BufferedReader br = new BufferedReader(new FileReader(fileHandle));

		String line;

		while ((line = br.readLine()) != null) {
		   offset[i]=Long.parseLong(line);
		   i++;
		}
		br.close();
		}
		catch( Exception e ){} 
	}
	public void searchInIndex( String str ) throws IOException {

		String line;
		try {
			File fileHandle = new File(outDirName+"/indexDataFile");
			RandomAccessFile primaryIndex=new RandomAccessFile(fileHandle, "r");

		primaryIndex.seek(offset[ str.charAt(0)-'a' ]);
		boolean found=false;
		StringBuilder word=new StringBuilder();
		while( (line=primaryIndex.readLine()) !=null ) {
			
			if( line.charAt(0)!= str.charAt(0) )
				break;
			
			i=line.indexOf('=');
			word.setLength(0);
			word.append(line.toCharArray(),0,i);
			if( str.compareTo(word.toString()) ==0 ) {
				found=true;
				break;
			}
		}
		if( found==true ) {
			System.out.println(line);
		}
		else
			System.out.println("No results");
		primaryIndex.close();
		}
		catch( Exception e ){} 
		
	}
	public void searchBinaryInIndex( String str ) throws IOException {

		
		try {
			File fileHandle = new File(outDirName+"/indexDataFile");
			RandomAccessFile primaryIndex=new RandomAccessFile(fileHandle, "r");
			i =  str.charAt(0)-'a';
			long start=offset[i];
			i++;
			long end = offset[i];
			long mid,t=0;
			long ps=0,pe=0;
			boolean found=false;
			StringBuilder word=new StringBuilder();
			StringBuilder line=new StringBuilder();
			while( start<end ) {
				mid = (start+end)/2;
				primaryIndex.seek(mid);
				char c=(char)primaryIndex.readByte();
				
				while( c !='\n') {
					mid--;
					if(mid<0) {
						c='#';
						break;
					}
					primaryIndex.seek(mid);
					c=(char)primaryIndex.readByte();
				}
				if( c=='#' )
					break;
				else {
					if( t==mid ) 
						mid=mid+line.length();
					t=mid;
					line.setLength(0);
					String st=primaryIndex.readLine();
					if(st==null)
						break;
					line.append(st);
					i=line.toString().indexOf('=');
					word.setLength(0);
					word.append(line.toString().toCharArray(),0,i);
					if( (i = str.compareTo( word.toString() ) ) ==0 ) {
						found=true;
						break;
					}
					if( i<0 ) {
						end=mid+1;
					}
					else {
						start=mid+1;
					}
				}
				if( ps==start && pe==end )
					break;
				ps=start;
				pe=end;
			}
			if( found==false ) {
				primaryIndex.seek(offset[ str.charAt(0)-'a' ]);
				String second;
				while( (second=primaryIndex.readLine()) !=null ) {
				
					if( second.charAt(0)!= str.charAt(0) )
						break;
				
					i=second.indexOf('=');
					word.setLength(0);
					word.append(second.toCharArray(),0,i);
					if( str.compareTo(word.toString()) ==0 ) {
						line.setLength(0);
						line.append(second);
						found=true;
						break;
					}
				}
			}
			if( found==true ) {
				//System.out.println(line);
				TreeSet<Integer> display = new TreeSet<Integer>();
				int i=line.toString().indexOf('=');
				i++;
	        	int len=line.length()-i;
	        	while( i<len) {
	        		int j = line.toString().indexOf(',', i);
	        		word.setLength(0);
	        		word.append(line.toString().toCharArray(),i,j-i);
	        		i=j+1;
	        		display.add(Integer.parseInt(word.toString())) ;
	        	}
	        	i = display.pollLast();
	        	for( Integer k : display ) {
	        		System.out.print(k.toString()+',');
	        	}
	        	System.out.println(i);
			}
			else
				System.out.println("");
			primaryIndex.close();
		}
		catch( Exception e ){} 		
	}
	public void buildSecIndex() throws IOException {
		
		
		File fileHandle=new File(outDirName+"/indexDataFile");
		RandomAccessFile primaryIndex=new RandomAccessFile(fileHandle, "r");
		
		File tempfileHandle = new File(outDirName+"/secondaryIndex");
    	if( tempfileHandle.exists() ) {
    		tempfileHandle.delete();
    	}
    	tempfileHandle.createNewFile();
		FileWriter writer = new FileWriter(tempfileHandle,true);
		BufferedWriter secIndex=new BufferedWriter(writer);
		Integer offset;
		
        
		try {
        	String str;
        	offset=0;
        	char start='a';
        	while( (str=primaryIndex.readLine()) != null ) {
        		if( str.charAt(0)==start ) {
        			//word.setLength(0);
        			//word.append( str.toCharArray(),0,str.indexOf('=') );
        			secIndex.write((offset.toString()));
        			secIndex.newLine();
        			start++;
        		}
        		offset+= str.length()+1;
        	}
    		secIndex.write((offset.toString()));
    		secIndex.newLine();
        }

		catch (IOException e) {
			e.printStackTrace();
		}
        finally {
        	try {
            	primaryIndex.close();
            	secIndex.close();
            }
            catch (IOException e) {
            	e.printStackTrace();
            }
        }
	}
}
