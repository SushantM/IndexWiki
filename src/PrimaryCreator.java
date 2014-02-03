import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.swing.text.html.HTMLDocument.Iterator;


public class PrimaryCreator {
	protected static final RecordForMerge PrimaryCreator$RecordForMerge = null;
	private int fileCount;
	private String outDir;
	public PrimaryCreator(){
		fileCount = 0 ;
		outDir = "";
	}
	public PrimaryCreator( int n, String outD) {
		fileCount = n;
		outDir = outD;
	}
	class RecordForMerge {
		String key;
		String line;
		BufferedReader value;
		
	}
	public void createPrimaryIndex() throws IOException {
		int i;
		final PriorityQueue<RecordForMerge> q =
			new PriorityQueue<PrimaryCreator.RecordForMerge>(	fileCount,
			new Comparator<RecordForMerge>() {
				public int compare( RecordForMerge n1, RecordForMerge n2 ) {
					int k;
					if( ( k= n1.key.compareTo(n2.key) ) == 0 ) {
						/*StringBuilder newLine = new StringBuilder();
						newLine.append(n1.line.toCharArray(),0,n1.line.length()-1);
						newLine.append(n2.line);
						RecordForMerge newR = PrimaryCreator$RecordForMerge;
						newR.key = n1.key;
						newR.line = newLine.toString();
						newR.value = n1.value;
						q.add(newR);
						return 0;*/
						return 0;
					}
					else if ( k>0) {
						return 1;
					}
					else {
						return -1;
					}
				}
		});
		BufferedReader fileArray[] = new BufferedReader[fileCount+1];
		StringBuilder strArray[] = new StringBuilder[fileCount+1];
		StringBuilder word = new StringBuilder();
		boolean empty[] = new boolean[fileCount+1];
		boolean ptr[] = new boolean[fileCount+1];
 		for( i=0; i<fileCount; i++ ) {
			fileArray[i] = new BufferedReader( new FileReader( new String( outDir+"/tempDump_"+i ) )  );
			empty[i] = false;
			ptr[i] = true;
			RecordForMerge r = new RecordForMerge();
			String l= fileArray[i].readLine();
			word.setLength(0);
			int indexOfE=l.indexOf('=');
			word.append(l.toCharArray(),0, indexOfE) ;
			r.key = word.toString();
			word.setLength(0);
			indexOfE++;
			word.append(l.toCharArray(), indexOfE,l.length()-indexOfE);
			r.line = word.toString();
			r.value = fileArray[i];
			
			q.add(r);
		}
 		FileWriter fwr = new FileWriter( new File (outDir+"/primaryIndex") );
 		BufferedWriter wr = new BufferedWriter (fwr);
 		
 		while(!q.isEmpty()) {
 			RecordForMerge smallest = q.peek();
 			StringBuilder lineTo = new StringBuilder();
 			lineTo.append(smallest.key+'=') ;
 			do {
 				RecordForMerge sameRecord = q.poll();
 				
 				lineTo.append(sameRecord.line);
 				
 				String nextLine = sameRecord.value.readLine();
 	 			if( nextLine==null )
 	 				continue;
 				RecordForMerge r = new RecordForMerge();
 				word.setLength(0);
 				try {
 					int indexOfE=nextLine.indexOf('=');
 					word.append(nextLine.toCharArray(),0,indexOfE ) ;
 					r.key = word.toString();
 					word.setLength(0);
 					indexOfE++;
 					word.append(nextLine.toCharArray(), indexOfE, nextLine.length()-indexOfE );
 					r.line = word.toString();
 					r.value = sameRecord.value;
 					q.add(r);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					//e.printStackTrace();
 					System.out.println("here:" + nextLine);
 				}
 				
 			}while( !q.isEmpty() &&  smallest.key.compareTo(q.peek().key)==0 );

 			wr.write(lineTo.toString());
 			wr.write('\n');
 			
 		}
	}
}