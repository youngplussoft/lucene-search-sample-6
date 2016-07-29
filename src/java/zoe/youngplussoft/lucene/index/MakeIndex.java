package zoe.youngplussoft.lucene.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.NumericDocValuesField ;
import org.apache.lucene.document.StoredField;

public class MakeIndex {
	
	public static void main(String[] argv) {
		
		try {
			
			long start = System.currentTimeMillis();
			
			String dataFileName = "./data/sample_1000.txt" ;
			BufferedReader in = new BufferedReader(
                				new InputStreamReader(new FileInputStream(dataFileName)));
			
			String question;
			String indexDirPath = "./index" ;
			
            new File(indexDirPath).mkdir();
			
      	    Analyzer analyzer = new KoreanAnalyzer() ;
      	    ((KoreanAnalyzer)analyzer).setBigrammable(false) ;
      	    
      	    
        	Path path = FileSystems.getDefault().getPath(indexDirPath);
            Directory directory = new NIOFSDirectory(path) ;           
            IndexWriterConfig config = new IndexWriterConfig(analyzer) ;
            IndexWriter writer = new IndexWriter(directory, config) ;           
            writer.deleteAll() ;
      	  
      	    int id = 1 ;
	        while ((question = in.readLine()) != null) {	        	  		
	  			
	  			
	        	  
	               Document doc = new Document();

	   				
	                doc.add(new StringField("id", Integer.toString(id),Field.Store.YES));
	                
	                doc.add(new TextField("content", question, Field.Store.YES));
	                
	                doc.add(new IntPoint("members", id*10)) ;
	                doc.add(new StoredField("members", id*10)) ;
	                
	                writer.addDocument(doc);
	                
	                
	                if( id % 100 == 0 ) {
	                	System.out.println(id + "sentences !!!") ;
	                }
	                
	                id++ ;
	            
	         }
			 writer.commit();
			 writer.close() ;
			 System.out.println("Indexing completed.") ;
			 
			long end = System.currentTimeMillis();
			
			long hour = (end-start)/(1000*360*60) ;
			long min  = ((end-start)/(1000*60))%60 ;
			long sec  = ((end-start)/(1000))%60 ;
			long msec = (end-start)%(1000) ;
			
			System.out.println("Indexing elapsed time(hh:mm:ss.ms) : " + hour + ":" + min + ":" + sec + "." + msec) ;
			
		}
		catch(Exception e) {
			e.printStackTrace() ;
			System.out.println("Error on Indexing...") ;
		}
		
	}

}
