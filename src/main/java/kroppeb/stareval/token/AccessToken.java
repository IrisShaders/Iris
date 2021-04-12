package kroppeb.stareval.token;

public class AccessToken extends AccessableToken {
	public final AccessableToken base;
	public final String index;
	
	public AccessToken(AccessableToken base, String index) {
		this.base = base;
		this.index = index;
	}
	
	@Override
	public String toString() {
		return "Access{" + base + "[" + index + "]}";
	}
}
