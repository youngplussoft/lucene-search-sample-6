package zoe.youngplussoft.lucene.search;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.apache.lucene.search.PointRangeQuery;

public class IntPointRangeQuery extends PointRangeQuery {
	
	int[] lower = null ;
	int[] upper = null ;

	protected static byte[] toBytes(int[] intArr) {
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(intArr.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(intArr);
        return byteBuffer.array() ;
	}
	
		
	public IntPointRangeQuery(String name, int[] lower, int[] upper, int ndim) {
		
		super(name, toBytes(lower), toBytes(upper), ndim) ;
		this.lower = lower ;
		this.upper = upper ;
	}

	@Override
	protected String toString(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
		return this.getField() +":" + lower + ":" + upper + ":" + this.getNumDims();
	}
}
