package kroppeb.stareval.token;

public class IdToken extends AccessableToken {
	public final String id;
	
	public IdToken(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Id{" + id + "}";
	}
}
