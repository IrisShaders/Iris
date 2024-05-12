package kroppeb.stareval.parser;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kroppeb.stareval.exception.ParseException;

import java.util.Map;

/**
 * OpResolver maps the trailing characters identifying an operator to an actual operator.
 *
 * @param <T>
 */
abstract class OpResolver<T> {
	abstract T resolve(StringReader input) throws ParseException;

	static class Builder<T> {
		private final Map<String, T> map;

		public Builder() {
			map = new Object2ObjectOpenHashMap<>();
		}

		public void singleChar(T op) {
			multiChar("", op);
		}

		/**
		 * Adds a new multi-character operator. Calling this with an empty string is equivalent to calling singleChar.
		 *
		 * @param trailing every character after the first character of the operator.
		 * @param op       the operator
		 */
		public void multiChar(String trailing, T op) {
			T previous = map.put(trailing, op);

			if (previous != null) {
				throw new RuntimeException("Tried to add multiple operators that map to the same string.");
			}
		}

		public OpResolver<T> build() {
			if (map.size() > 2) {
				throw new RuntimeException("unimplemented: Cannot currently build an optimized operator resolver " +
					"tree when more than two operators start with the same character");
			}

			T singleChar = map.get("");

			if (singleChar != null) {
				if (map.size() == 1) {
					return new OpResolver.SingleChar<>(singleChar);
				} else {
					for (Map.Entry<String, T> subEntry : map.entrySet()) {
						if ("".equals(subEntry.getKey())) {
							// We already checked this key
							continue;
						}

						if (subEntry.getKey().length() != 1) {
							throw new RuntimeException("unimplemented: Optimized operator resolver trees can " +
								"currently only be built of operators that contain one or two characters.");
						}

						// We can assume that this is the only other entry in the map due to the size check above
						return new OpResolver.SingleDualChar<>(
							singleChar,
							subEntry.getValue(),
							subEntry.getKey().charAt(0)
						);
					}
				}
			} else {
				if (map.size() > 1) {
					throw new RuntimeException("unimplemented: Optimized operator resolver trees can currently only " +
						"handle two operators starting with the same character if one operator is a single character");
				}

				for (Map.Entry<String, T> subEntry : map.entrySet()) {
					if (subEntry.getKey().length() != 1) {
						throw new RuntimeException("unimplemented: Optimized operator resolver trees can " +
							"currently only be built of operators that contain one or two characters.");
					}

					// We can assume that this is the only entry in the map due to the size check above.
					return new OpResolver.DualChar<>(
						subEntry.getValue(),
						subEntry.getKey().charAt(0)
					);
				}
			}

			if (map.isEmpty()) {
				throw new RuntimeException("Tried to build an operator resolver tree that contains no operators.");
			} else {
				throw new RuntimeException("This shouldn't be reachable");
			}
		}
	}

	/**
	 * Matches a single-character operator.
	 */
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

	/**
	 * Matches a two-character operator.
	 */
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

	/**
	 * Matches either a dual character operator or a single character operator.
	 */
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
