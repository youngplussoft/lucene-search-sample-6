package zoe.youngplussoft.lucene.search;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery.Builder;

public class SearchIndex2 {

	public static void main(String[] argv) {

		try {

			Scanner in = new Scanner(System.in);
			in.useLocale(Locale.KOREAN);
			String source = "";
			String dir = "./index";

			Path path = FileSystems.getDefault().getPath(dir);
			Directory directory = new NIOFSDirectory(path);
			IndexReader reader = DirectoryReader.open(directory);
			IndexSearcher searcher = new IndexSearcher(reader);

			KoreanAnalyzer analyzer = new KoreanAnalyzer();
			analyzer.setBigrammable(false);

			// while(true)
			{

				/*
				 * System.out.println("Input Line :") ; source = in.nextLine() ;
				 * System.out.println(source) ;
				 * 
				 * if( source.trim().length() == 0 ) continue ;
				 * 
				 * 
				 * if( source.equals("exit") ) break ;
				 */

				// source = "하세요";

				/*
				 * QueryParser qp = new QueryParser("content", analyzer);
				 * qp.setDefaultOperator(Operator.AND) ; Query query =
				 * qp.parse(source) ;
				 * 
				 * 
				 * //Query query = new TermQuery(new Term("id", "1")) ;
				 * 
				 * QueryParser qp = new QueryParser("content", analyzer); Query
				 * pquery = qp.parse("감기");
				 */

				QueryParser qp = new QueryParser("content", analyzer);
				Query pquery = qp.parse("감기");
				Query nquery = IntPoint.newRangeQuery("members", 1, 1000);

				Builder builder = new Builder();
				// FILTER : AND(Not Scoring), MUST : AND, SHOULD : OR, MUST_NOT
				// : NOT - Boolean Query
				builder.add(pquery, BooleanClause.Occur.MUST);
				builder.add(nquery, BooleanClause.Occur.MUST);
				Query query = builder.build();

				TopDocs tops = null;

				tops = searcher.search(query, 10);

				int total = tops.scoreDocs.length;
				for (int i = 0; tops != null && i < total && i < tops.totalHits; i++) {

					Document doc = searcher.doc(tops.scoreDocs[i].doc);

					String id = doc.get("id");
					String content = doc.get("content");

					IndexableField membersF = doc.getField("members");
					Number members = membersF.numericValue();
					int m = members.intValue();
					// sorting 용 변수는 값을 가져오지 못함.

					System.out.println(id + ":" + content + ":" + m);
				}
			}

			reader.close();
			directory.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
