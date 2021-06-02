package kroppeb.stareval.parser;

import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.token.ExpressionToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {
	private static final Parser parser = new Parser(IrisOptions.options);

	@ParameterizedTest
	@CsvFileSource(resources = "/shouldBeAbleToBeParsed.csv", delimiter = ';')
	void checkIfValidExpressionsParse(String input) throws ParseException {
		parser.parse(input);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/shouldNotBeAbleToBeParsed.csv", delimiter = ';')
	void checkIfInvalidExpressionsDontParse(String input) {
		try{
			parser.parse(input);
		} catch (ParseException parseException){
			parseException.printStackTrace();
			return;
		} catch (RuntimeException runtimeException){
			fail("Unexpected exception has been thrown", runtimeException);
			return;
		}
		fail("No exception has been thrown");
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/fullyEquivalent.csv", delimiter = ';')
	void checkOrderOfOperationsParse(String input1, String input2) throws ParseException {
		ExpressionToken exp1 = parser.parse(input1);
		ExpressionToken exp2 = parser.parse(input2);
		assertEquals(exp1.simplify().toString(), exp2.simplify().toString());
	}
}
