package ntu.im.cource.adb.bpr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BPR{
	
	protected final static int K=100;
	protected final static double NOISE_LEVEL=1e-4;
	protected final static double LEARN_RATE=0.01;
	protected final static double LAMBDA=1e-6;
	protected static Random ran = new Random();
	
	protected Map<String,Vector> features = new HashMap<String,Vector>();
	protected Map<String,Integer> entity_types = new HashMap<String,Integer>();
	public Map<Integer,List<String>> type_entities = new HashMap<Integer,List<String>>();
	
	public BPR(){
	}
	
	public BPR(BPR bpr){
		this();
		addAll(bpr);
	}
	
	protected boolean containsEntity(String e){
		return entity_types.containsKey(e);
	}

	protected void addAll(BPR bpr){
		
		features.putAll(bpr.features);
		entity_types.putAll(bpr.entity_types);
		for(Integer type: bpr.type_entities.keySet()){
			Set<String> set = new HashSet<String>();
			if(type_entities.containsKey(type))set.addAll(type_entities.get(type));
			set.addAll(bpr.type_entities.get(type));
			type_entities.put(type,new ArrayList<String>(set));
		}
	}

	protected double logistic(double score){
		
		return 1.0/(1+Math.exp(-score));
	}
	
	protected String drawNegative( Collection<String> tuple, Integer type){ 
		
		String neg_example;
		List<String> list;
		do{
			list = type_entities.get(type); 
			neg_example = list.get( ran.nextInt(list.size()) );
			
		}while( tuple.contains(neg_example) );
		
		return neg_example;
	}
	
	//private int iteration=10000;
	private Vector[] posFeas;
	private Integer[] types;
	private Collection<String> occur;
	protected Vector qfea;
	protected void initCooccurFea( Collection<String> occur ){
		this.occur = occur;
		
		posFeas = new Vector[occur.size()];
		types = new Integer[occur.size()];
		int k=0;
		for(String id: occur){
			posFeas[k] = features.get(id);
			types[k] = entity_types.get(id);
			k++;
		}
		
		qfea = Vector.zeros(K);
	}
	
	protected void cooccurFeaIter(){
		for(int j=0;j<posFeas.length;j++){
			Vector negFea, posFea;
			double err;
			
			posFea = posFeas[j];
			negFea = features.get( drawNegative( occur, types[j] ) );
			err = 1-logistic(qfea.dot(posFea) - qfea.dot(negFea));
			
			qfea = qfea.add( posFea.sub(negFea).mul(err).sub( qfea.mul(LAMBDA) ).mul(LEARN_RATE) );
		}
	}
}
	
/*	
	double predict(Vector[] feas){
		
		double squareSum = 0.0;
		for(int i=0;i<feas.length;i++)
			squareSum+= feas[i].dot(feas[i]);

		Vector vsum = Vector.zeros(K);
		for(int i=0;i<feas.length;i++)
			vsum = vsum.add(feas[i]);

		double sumSquare = vsum.dot(vsum);
		
		return sumSquare - squareSum;
	}
	*/
	
