package cis552project.iterator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cis552project.CIS552SO;
import cis552project.ExpressionEvaluator;
import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.PrimitiveValue;
//import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class GroupByIT extends BaseIT {

	TableResult finalTableResult = null;
	Iterator<Tuple> resIT = null;
	List<Tuple> finalResultTuples = null;

	TableResult initialTabRes = null;

	Map<Tuple, List<Tuple>> groupByMap = new HashMap<>();

	public GroupByIT(List<Column> groupByColumnList, List<SelectItem> selectItems, BaseIT result, CIS552SO cis552so)
			throws SQLException {

		finalResultTuples = new ArrayList<>();
		while (result.hasNext()) {
			initialTabRes = result.getNext();
			finalResultTuples.addAll(initialTabRes.resultTuples);
		}
		List<Tuple> resultCombiningSelect = new ArrayList<>();
		for (Tuple tuple : finalResultTuples) {
			PrimitiveValue primValeArray[] = new PrimitiveValue[groupByColumnList.size()];
			for (int i = 0; i < groupByColumnList.size(); i++) {
				Column column = groupByColumnList.get(i);

				Eval eval = new ExpressionEvaluator(Arrays.asList(tuple), initialTabRes, cis552so);
				primValeArray[i] = eval.eval(column);
			}
			Tuple groupedTuple = new Tuple(primValeArray);
			if (!groupByMap.containsKey(groupedTuple)) {
				groupByMap.put(groupedTuple, new ArrayList<>());
			}
			groupByMap.get(groupedTuple).add(tuple);

		}
		for (Map.Entry<Tuple, List<Tuple>> keyValuePair : groupByMap.entrySet()) {
			PrimitiveValue[] functionSolution = evaluateFunction(keyValuePair.getKey().resultRow,
					keyValuePair.getValue(), selectItems, groupByColumnList, cis552so);
			resultCombiningSelect.add(new Tuple(functionSolution));
		}
		finalResultTuples = resultCombiningSelect;
		resIT = finalResultTuples.iterator();

	}

	@Override
	public TableResult getNext() {
		finalTableResult.resultTuples = new ArrayList<>();
		finalTableResult.resultTuples.add(resIT.next());
		return finalTableResult;
	}

	@Override
	public boolean hasNext() {
		return resIT.hasNext();
	}

	@Override
	public void reset() {
		resIT = finalResultTuples.iterator();

	}

	private PrimitiveValue[] evaluateFunction(PrimitiveValue[] groupByKey, List<Tuple> resultTuples,
			List<SelectItem> selectItems, List<Column> groupByColumnList, CIS552SO cis552SO) throws SQLException {
		PrimitiveValue[] valueAfterFuntion = new PrimitiveValue[selectItems.size()];
		for (int i = 0; i < selectItems.size(); i++) {
			SelectExpressionItem sei = (SelectExpressionItem) selectItems.get(i);
			if (finalTableResult == null) {
				finalTableResult = new TableResult();
			}
			String columnName = selectItems.get(i).toString();
			if (selectItems.get(i) instanceof Column) {
				Column column = (Column) selectItems.get(i);
				columnName = column.getColumnName();
			}
			String columnAlias = sei.getAlias() != null ? sei.getAlias() : columnName;
			finalTableResult.colPosWithTableAlias.put(new Column(null, columnAlias), i);
			Expression exp = sei.getExpression();
			Eval eval = new ExpressionEvaluator(resultTuples, initialTabRes, cis552SO);
			valueAfterFuntion[i] = eval.eval(exp);
		}
		return valueAfterFuntion;
	}

}
