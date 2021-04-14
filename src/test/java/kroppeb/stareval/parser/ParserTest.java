package kroppeb.stareval.parser;

import kroppeb.stareval.token.ExpressionToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {
	private static final Parser parser = new Parser(IrisOptions.options);

	@ParameterizedTest
	@CsvFileSource(resources = "/shouldBeAbleToBeParsed.csv", delimiter = ';')
	void checkIfValidExpressionsParse(String input) throws Exception {
		parser.parse(input);
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = "/fullyEquivalent.csv", delimiter = ';')
	void checkOrderOfOperationsParse(String input1, String input2) throws Exception {
		ExpressionToken exp1 = parser.parse(input1);
		ExpressionToken exp2 = parser.parse(input2);
		assertEquals(exp1.simplify() .toString(), exp2.simplify().toString());
	}
}
