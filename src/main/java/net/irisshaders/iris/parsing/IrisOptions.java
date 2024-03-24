package net.irisshaders.iris.parsing;

import kroppeb.stareval.parser.BinaryOp;
import kroppeb.stareval.parser.ParserOptions;
import kroppeb.stareval.parser.UnaryOp;

public class IrisOptions {
	public static final ParserOptions options;
	static final BinaryOp Multiply = new BinaryOp("multiply", 0);
	static final BinaryOp Divide = new BinaryOp("divide", 0);
	static final BinaryOp Remainder = new BinaryOp("remainder", 0);
	static final BinaryOp Add = new BinaryOp("add", 1);
	static final BinaryOp Subtract = new BinaryOp("subtract", 1);
	static final BinaryOp Equals = new BinaryOp("equals", 2);
	static final BinaryOp NotEquals = new BinaryOp("notEquals", 2);
	static final BinaryOp LessThan = new BinaryOp("lessThan", 2);
	static final BinaryOp MoreThan = new BinaryOp("moreThan", 2);
	static final BinaryOp LessThanOrEquals = new BinaryOp("lessThanOrEquals", 2);
	static final BinaryOp MoreThanOrEquals = new BinaryOp("moreThanOrEquals", 2);
	static final BinaryOp And = new BinaryOp("and", 3);
	static final BinaryOp Or = new BinaryOp("or", 3);
	static final UnaryOp Not = new UnaryOp("not");
	static final UnaryOp Negate = new UnaryOp("negate");

	static {
		final ParserOptions.Builder builder = new ParserOptions.Builder();
		builder.addBinaryOp("*", Multiply);
		builder.addBinaryOp("/", Divide);
		builder.addBinaryOp("%", Remainder);

		builder.addBinaryOp("+", Add);
		builder.addBinaryOp("-", Subtract);

		builder.addBinaryOp("==", Equals);
		builder.addBinaryOp("!=", NotEquals);
		builder.addBinaryOp("<", LessThan);
		builder.addBinaryOp(">", MoreThan);
		builder.addBinaryOp("<=", LessThanOrEquals);
		builder.addBinaryOp(">=", MoreThanOrEquals);

		builder.addBinaryOp("≠", NotEquals);
		builder.addBinaryOp("≤", LessThanOrEquals);
		builder.addBinaryOp("≥", MoreThanOrEquals);

		builder.addBinaryOp("&&", And);
		builder.addBinaryOp("||", Or);

		builder.addUnaryOp("!", Not);
		builder.addUnaryOp("-", Negate);

		options = builder.build();
	}
}
