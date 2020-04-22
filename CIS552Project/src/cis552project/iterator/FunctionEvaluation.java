package cis552project.iterator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import cis552project.CIS552SO;
import cis552project.ExpressionEvaluator;
import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue;

public class FunctionEvaluation {

	public static PrimitiveValue applyFunction(List<Tuple> initialResult, Function funExp, TableResult finalTableResult,
			CIS552SO cis552so) throws SQLException {
		String funName = funExp.getName().toUpperCase();
		switch (funName) {
		case "COUNT":
			return evaluateCount(initialResult, funExp, finalTableResult, cis552so);
		case "SUM":
			return evaluateSum(initialResult, funExp, finalTableResult, cis552so);
		case "AVG":
			return evaluateAvg(initialResult, funExp, finalTableResult, cis552so);
		case "MIN":
			return evaluateMin(initialResult, funExp, finalTableResult, cis552so);
		case "MAX":
			return evaluateMax(initialResult, funExp, finalTableResult, cis552so);
		case "DATE_PART":
		case "DATEPART":
			return evaluateDatePart(initialResult, funExp, finalTableResult, cis552so);
		case "DATE":
			return evaluateDate(initialResult, funExp, finalTableResult, cis552so);
		}

		throw new UnsupportedOperationException(funName + " Not supported yet.");
	}

	private static PrimitiveValue evaluateDate(List<Tuple> initialResult, Function funExp, TableResult finalTableResult,
			CIS552SO cis552so) throws SQLException {

		List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
				funExp.getParameters().getExpressions().get(0), finalTableResult, cis552so);

		return new DateValue(pValueList.get(0).toString().replace("'", ""));

	}

	private static PrimitiveValue evaluateDatePart(List<Tuple> initialResult, Function funExp,
			TableResult finalTableResult, CIS552SO cis552so) throws SQLException {

		List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
				funExp.getParameters().getExpressions().get(1), finalTableResult, cis552so);

		long value = 0;
		switch (funExp.getParameters().getExpressions().get(0).toString().toUpperCase().replace("'", "")) {
		case "YEAR":
		case "YYYY":
		case "YY":
			value = 1900 + new DateValue(pValueList.get(0).toString().replace("'", "")).getYear();
			break;
		case "MONTH":
		case "MM":
		case "M":
			value = new DateValue(pValueList.get(0).toString().replace("'", "")).getMonth();
			break;
		case "DAY":
		case "DD":
		case "D":
			value = new DateValue(pValueList.get(0).toString().replace("'", "")).getDate();
			break;
		}
		return new LongValue(value);

	}

	private static PrimitiveValue evaluateCount(List<Tuple> initialResult, Function funExp,
			TableResult finalTableResult, CIS552SO cis552so) throws SQLException {
		if (funExp.isAllColumns()) {
			return new LongValue(initialResult.size());
		} else {
			List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
					funExp.getParameters().getExpressions().get(0), finalTableResult, cis552so);

			return new DoubleValue(pValueList.size());

		}
	}

	private static PrimitiveValue evaluateSum(List<Tuple> initialResult, Function funExp, TableResult finalTableResult,
			CIS552SO cis552so) throws SQLException {
		List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
				funExp.getParameters().getExpressions().get(0), finalTableResult, cis552so);
		Double sum = 0.0;
		for (PrimitiveValue primitiveValue : pValueList) {
			sum += primitiveValue.toDouble();
		}
		return new DoubleValue(sum);
	}

	private static PrimitiveValue evaluateAvg(List<Tuple> initialResult, Function funExp, TableResult finalTableResult,
			CIS552SO cis552so) throws SQLException {
		List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
				funExp.getParameters().getExpressions().get(0), finalTableResult, cis552so);
		double sum = 0;
		double avg = 0;
		for (PrimitiveValue primitiveValue : pValueList) {
			sum += primitiveValue.toDouble();
		}
		avg = sum / pValueList.size();
		return new DoubleValue(avg);
	}

	private static PrimitiveValue evaluateMin(List<Tuple> initialResult, Function funExp, TableResult finalTableResult,
			CIS552SO cis552so) throws SQLException {
		List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
				funExp.getParameters().getExpressions().get(0), finalTableResult, cis552so);
		double min = pValueList.get(0).toDouble();
		for (PrimitiveValue primitiveValue : pValueList) {
			if (min <= primitiveValue.toDouble())
				continue;
			min = primitiveValue.toDouble();
		}
		return new DoubleValue(min);
	}

	private static PrimitiveValue evaluateMax(List<Tuple> initialResult, Function funExp, TableResult finalTableResult,
			CIS552SO cis552so) throws SQLException {
		List<PrimitiveValue> pValueList = fetchEvaluatedExpressions(initialResult,
				funExp.getParameters().getExpressions().get(0), finalTableResult, cis552so);
		double max = pValueList.get(0).toDouble();
		for (PrimitiveValue primitiveValue : pValueList) {
			if (max >= primitiveValue.toDouble())
				continue;
			max = primitiveValue.toDouble();
		}
		return new DoubleValue(max);
	}

	private static List<PrimitiveValue> fetchEvaluatedExpressions(List<Tuple> initialResult, Expression expression,
			TableResult finalTableResult, CIS552SO cis552so) throws SQLException {
		List<PrimitiveValue> pValueList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(initialResult)) {
			for (Tuple tuple : initialResult) {
				Eval eval = new ExpressionEvaluator(Arrays.asList(tuple), finalTableResult, cis552so);
				PrimitiveValue value = eval.eval(expression);
				pValueList.add(value);
			}
		} else {
			Eval eval = new ExpressionEvaluator(null, finalTableResult, cis552so);
			PrimitiveValue value = eval.eval(expression);
			pValueList.add(value);
		}

		return pValueList;
	}

}
