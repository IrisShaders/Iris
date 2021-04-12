package kroppeb.stareval.token;

public class CallBaseToken extends Token {
	public final String id;
	
	public CallBaseToken(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "CallBase{" + id + "}";
	}
}
