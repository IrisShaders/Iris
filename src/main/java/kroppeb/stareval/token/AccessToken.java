package kroppeb.stareval.token;

public class AccessToken extends AccessableToken {
	private final AccessableToken base;
	private final String index;

	public AccessToken(AccessableToken base, String index) {
		this.base = base;
		this.index = index;
	}

	@Override
	public String toString() {
		return "Access{" + this.base + "[" + this.index + "]}";
	}
}
