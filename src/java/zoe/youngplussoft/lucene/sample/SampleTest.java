package zoe.youngplussoft.lucene.sample;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.NumericDocValuesField ;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.BooleanQuery.Builder ;

public class SampleTest {
	

	
	public static void MakeIndex() {
		
		try {
			
			long start = System.currentTimeMillis();
			
			String dataFileName = "./data/news.csv" ;
			BufferedReader in = new BufferedReader(
                				new InputStreamReader(new FileInputStream(dataFileName)));
			
			String question;
			String indexDirPath = "./index2" ;
			
      	    Analyzer analyzer = new KoreanAnalyzer() ;
      	    ((KoreanAnalyzer)analyzer).setBigrammable(false) ;
      	    
      	    
        	Path path = FileSystems.getDefault().getPath(indexDirPath);
            Directory directory = new NIOFSDirectory(path) ;           
            IndexWriterConfig config = new IndexWriterConfig(analyzer) ;
            IndexWriter writer = new IndexWriter(directory, config) ;           
            writer.deleteAll() ;
      	  
      	    int id = 1 ;
	        while ((question = in.readLine()) != null) {	        	  		
	  			
	  			    String fields[] = question.split("@#@") ;
	        	  
	               Document doc = new Document();

	   				
	                doc.add(new IntPoint("id", Integer.parseInt(fields[0])));
	                doc.add(new StoredField("id", Integer.parseInt(fields[0])));
	                doc.add(new StringField("magazine", fields[1],Field.Store.YES));
	                doc.add(new StringField("type", fields[2],Field.Store.YES));
	                doc.add(new StringField("category", fields[3],Field.Store.YES));	                
	                doc.add(new TextField("title", fields[4], Field.Store.YES));
	                doc.add(new StringField("url", fields[5], Field.Store.YES));	 
	                doc.add(new StringField("pubdate", fields[6], Field.Store.YES));	
	                doc.add(new NumericDocValuesField("order", Integer.parseInt(fields[0])));

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
	
	public static String hilight(Query query, IndexReader reader, 
			String fieldName, Analyzer analyzer, String fieldStr) {
		
		QueryScorer scorer = new QueryScorer(query, reader, fieldName);
		Highlighter highlighter = new Highlighter(scorer);
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
		highlighter.setTextFragmenter(fragmenter);

		String fragment = null;
		try {
		  fragment = highlighter.getBestFragment(analyzer, fieldName, fieldStr) ;
		} catch (Exception e) {
		   e.printStackTrace();
		   fragment = null;
		}
		
		return fragment ;
	}
	
	public static int toInt(byte[] bytes, int offset) {
		  int ret = 0;
		  for (int i=0; i<4 && i+offset<bytes.length; i++) {
		    ret <<= 8;
		    ret |= (int)bytes[i] & 0xFF;
		  }
		  return ret;
		}
	
	public static void searchByQueryParser() {
		
		try {
			

            String dir = "./index2";
                    
            Path path = FileSystems.getDefault().getPath(dir);
            Directory directory = new NIOFSDirectory(path);
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);         
            
            KoreanAnalyzer analyzer = new KoreanAnalyzer() ;
            analyzer.setBigrammable(false) ;
            

            String source = "청목회 소환통보" ;
            QueryParser qp = new QueryParser("title", analyzer);
            qp.setDefaultOperator(Operator.OR) ;
            Query query = qp.parse(source) ;
                
         
            TopDocs tops = null ;
            
            try {

            	SortField sf = new SortField("order",SortField.Type.INT,false);
            	Sort sort = new Sort(sf);
            	//tops = searcher.search(query, 10, sort) ;
            	tops = searcher.search(query, 10) ;
            }
            catch(Exception e) {
            	e.printStackTrace() ;
            	
            }
            
            int total = tops.scoreDocs.length ;
            for(int i=0 ; tops != null && i<total && i<tops.totalHits ; i++) {
            	
            	Document doc = searcher.doc(tops.scoreDocs[i].doc); 

            	IndexableField idF = doc.getField("id");
				Number ids = idF.numericValue();
				int id = ids.intValue();
            	// sorting 용 변수는 값을 가져오지 못함. 
            	
            	String magazine = doc.get("magazine") ;
            	String type = doc.get("type") ;
            	String category = doc.get("category") ;
            	String title = doc.get("title") ;
            	String url = doc.get("url") ;
            	String pubdate = doc.get("pubdate") ;
            	
            	String hi = hilight(query, reader, "title", analyzer, "청목회 소환통보" ) ;
            	
            	System.out.println(id + ":" + magazine + ":" + type + ":" + category + ":" + hi + ":" + url + ":" + pubdate) ;
            }
            
            reader.close();
            directory.close();

		}
        catch(Exception e) {
        	e.printStackTrace() ;
        }
	}
	
	
	
	
	public static void searchByBooleanQuery() {
		
		try {
			

            String dir = "./index2";
                    
            Path path = FileSystems.getDefault().getPath(dir);
            Directory directory = new NIOFSDirectory(path);
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);         
            
            KoreanAnalyzer analyzer = new KoreanAnalyzer() ;
            analyzer.setBigrammable(false) ;
            

            String source = "청목회 소환통보" ;
            QueryParser qp = new QueryParser("title", analyzer);
            qp.setDefaultOperator(Operator.OR) ;
            Query query0 = qp.parse(source) ;
            
            Query query1 = new TermQuery(new Term("magazine", "SBS")) ;
            Query query2 = new TermQuery(new Term("type", "종합신문")) ;
            Query query3 = new TermQuery(new Term("category", "속보")) ;
            
            Builder builder = new Builder() ;
            builder.add(query0, BooleanClause.Occur.MUST) ;
            builder.add(query1, BooleanClause.Occur.MUST) ;
            builder.add(query2, BooleanClause.Occur.MUST) ;
            builder.add(query3, BooleanClause.Occur.MUST) ;
            
            Query query = builder.build();
               
         
            TopDocs tops = null ;
            
            try {

            	tops = searcher.search(query, 10) ;
            }
            catch(Exception e) {
            	e.printStackTrace() ;
            	
            }
            
            int total = tops.scoreDocs.length ;
            for(int i=0 ; tops != null && i<total && i<tops.totalHits ; i++) {
            	
            	Document doc = searcher.doc(tops.scoreDocs[i].doc); 
            	
            	IndexableField membersF = doc.getField("id") ;
            	Number members = membersF.numericValue() ;
            	int id = members.intValue() ;
            	// sorting 용 변수는 값을 가져오지 못함. 
            	
            	String magazine = doc.get("magazine") ;
            	String type = doc.get("type") ;
            	String category = doc.get("category") ;
            	String title = doc.get("title") ;
            	String url = doc.get("url") ;
            	String pubdate = doc.get("pubdate") ;
            	
            	String hi = hilight(query, reader, "title", analyzer, "청목회 소환통보" ) ;
            	
            	System.out.println(id + ":" + magazine + ":" + type + ":" + category + ":" + hi + ":" + url + ":" +  pubdate) ;
          
            }
            
            reader.close();
            directory.close();

		}
        catch(Exception e) {
        	e.printStackTrace() ;
        }
	}
	
	public static void main(String[] argv) {
		
		
		MakeIndex() ;
		
		System.out.println("======================================================================");
		
		searchByQueryParser() ;
		
		System.out.println("======================================================================");
		
		searchByBooleanQuery() ;
		
	}

}
