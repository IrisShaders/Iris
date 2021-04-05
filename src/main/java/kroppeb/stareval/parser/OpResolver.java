package kroppeb.stareval.parser;


abstract public class OpResolver<T extends Op> {
	abstract T check(StringReader input) throws Exception;
}

class SingleCharOpResolver<T extends Op> extends OpResolver<T> {
	final T t;
	
	SingleCharOpResolver(T t) {
		this.t = t;
	}
	
	@Override
	T check(StringReader input) {
		return t;
	}
}

class DualCharOpResolver<T extends Op> extends OpResolver<T> {
	final T t;
	final char c;
	
	DualCharOpResolver(T t, char c) {
		this.t = t;
		this.c = c;
	}
	
	@Override
	T check(StringReader input) throws Exception {
		input.read(c);
		return t;
	}
}

class SingleDualCharOpResolver<T extends Op> extends OpResolver<T> {
	final T tSingle;
	final T tDouble;
	final char c;
	
	public SingleDualCharOpResolver(T tSingle, T tDouble, char c) {
		this.tSingle = tSingle;
		this.tDouble = tDouble;
		this.c = c;
	}
	
	@Override
	T check(StringReader input) {
		if(input.tryRead(c)){
			return tDouble;
		} else {
			return tSingle;
		}
	}
}