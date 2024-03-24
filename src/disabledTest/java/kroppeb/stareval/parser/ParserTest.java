package kroppeb.stareval.parser;

import kroppeb.stareval.element.Element;
import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.element.ExpressionElement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {
	private static ExpressionElement parse(String string) throws ParseException {
		return Parser.parse(string, IrisParserOptions.options);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/shouldBeAbleToBeParsed.csv", delimiter = ';')
	void checkIfValidExpressionsParse(String input) throws ParseException {
		parse(input);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/shouldNotBeAbleToBeParsed.csv", delimiter = ';')
	void checkIfInvalidExpressionsDontParse(String input) {
		try {
			parse(input);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			return;
		} catch (RuntimeException runtimeException) {
			fail("Unexpected exception has been thrown", runtimeException);
			return;
		}
		fail("No exception has been thrown");
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/fullyEquivalent.csv", delimiter = ';')
	void checkOrderOfOperationsParse(String input1, String input2) throws ParseException {
		Element exp1 = parse(input1);
		Element exp2 = parse(input2);
		assertEquals(exp1.toString(), exp2.toString());
	}
}
