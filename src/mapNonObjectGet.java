import java.util.HashMap;

public class mapNonObjectGet<K, V> extends HashMap<K, V> {
	
	public mapNonObjectGet(){
		
	}
	@Override
	public V get(Object k){
		
		for( Entry<K, V> e : this.entrySet()){
			if(k.equals( e.getKey() )){
				return e.getValue();
			}
		}
		return null;
		
	}
}
