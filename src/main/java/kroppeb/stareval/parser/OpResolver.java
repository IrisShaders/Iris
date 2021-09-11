package kroppeb.stareval.parser;


import kroppeb.stareval.exception.ParseException;

abstract class OpResolver<T> {
	abstract T resolve(StringReader input) throws ParseException;

	static class SingleChar<T> extends OpResolver<T> {
		private final T op;

		SingleChar(T op) {
			this.op = op;
		}

		@Override
		T resolve(StringReader input) {
			return this.op;
		}
	}

	static class DualChar<T> extends OpResolver<T> {
		private final T op;
		private final char secondChar;

		DualChar(T op, char secondChar) {
			this.op = op;
			this.secondChar = secondChar;
		}

		@Override
		T resolve(StringReader input) throws ParseException {
			input.read(this.secondChar);
			return this.op;
		}
	}

	static class SingleDualChar<T> extends OpResolver<T> {
		private final T singleCharOperator;
		private final T doubleCharOperator;
		private final char secondChar;

		SingleDualChar(T singleCharOperator, T doubleCharOperator, char secondChar) {
			this.singleCharOperator = singleCharOperator;
			this.doubleCharOperator = doubleCharOperator;
			this.secondChar = secondChar;
		}

		@Override
		T resolve(StringReader input) {
			if (input.tryRead(this.secondChar)) {
				return this.doubleCharOperator;
			} else {
				return this.singleCharOperator;
			}
		}
	}
}
