package kroppeb.stareval.parser;


abstract class OpResolver<T extends Op> {
	abstract T check(StringReader input) throws Exception;
}

class SingleCharOpResolver<T extends Op> extends OpResolver<T> {
	private final T t;

	SingleCharOpResolver(T t) {
		this.t = t;
	}

	@Override
	T check(StringReader input) {
		return this.t;
	}
}

class DualCharOpResolver<T extends Op> extends OpResolver<T> {
	private final T t;
	private final char c;

	DualCharOpResolver(T t, char c) {
		this.t = t;
		this.c = c;
	}

	@Override
	T check(StringReader input) throws Exception {
		input.read(this.c);
		return this.t;
	}
}

class SingleDualCharOpResolver<T extends Op> extends OpResolver<T> {
	private final T tSingle;
	private final T tDouble;
	private final char c;

	SingleDualCharOpResolver(T tSingle, T tDouble, char c) {
		this.tSingle = tSingle;
		this.tDouble = tDouble;
		this.c = c;
	}

	@Override
	T check(StringReader input) {
		if (input.tryRead(this.c)) {
			return this.tDouble;
		} else {
			return this.tSingle;
		}
	}
}
