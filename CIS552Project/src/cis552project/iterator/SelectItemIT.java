package cis552project.iterator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cis552project.CIS552SO;
import cis552project.TableColumnData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class SelectItemIT extends BaseIT {

	BaseIT result = null;
	List<SelectItem> selectItems = null;
	CIS552SO cis552SO = null;

	public SelectItemIT(List<SelectItem> selectItems, BaseIT result, CIS552SO cis552SO) {
		if (selectItems.get(0) instanceof SelectExpressionItem) {
			if (((SelectExpressionItem) selectItems.get(0)).getExpression() instanceof Function) {
				Function funExp = (Function) ((SelectExpressionItem) selectItems.get(0)).getExpression();

				result = new AggFunctionIT(funExp, result, cis552SO,
						((SelectExpressionItem) selectItems.get(0)).getAlias());
			}
		}

		this.selectItems = selectItems;
		this.result = result;
		this.cis552SO = cis552SO;
	}

	@Override
	public TableResult getNext() {
		try {

			TableResult oldTableResult = result.getNext();
			TableResult newTableResult = new TableResult();
			updateColDefMap(oldTableResult, newTableResult);
			return solveSelectItemExpression(oldTableResult, newTableResult);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean hasNext() {
		if (result == null || !result.hasNext()) {
			return false;
		}
		return true;
	}

	@Override
	public void reset() {
		result.reset();
	}

	private TableResult solveSelectItemExpression(TableResult oldTableResult, TableResult newTableResult)
			throws SQLException {
		if (selectItems.get(0) instanceof AllColumns) {
			newTableResult.resultTuples = oldTableResult.resultTuples;
		} else {
			List<Expression> finalExpItemList = new ArrayList<>();
			for (SelectItem selectItem : selectItems) {
				if (selectItem instanceof SelectExpressionItem) {
					Expression expr = ((SelectExpressionItem) selectItem).getExpression();
					if (expr instanceof Function) {
						return oldTableResult;
					} else {
						finalExpItemList.add(expr);
					}
				} else if (selectItem instanceof AllTableColumns) {
					AllTableColumns allTableColumn = (AllTableColumns) selectItem;
					TableColumnData tableSchema = cis552SO.tables
							.get(oldTableResult.aliasandTableName.get(allTableColumn.getTable().getName()));

					finalExpItemList.addAll(tableSchema.colList.stream()
							.map(col -> (Expression) new Column(allTableColumn.getTable(), col.getColumnName()))
							.collect(Collectors.toList()));
				}
			}
			for (Tuple resultTuple : oldTableResult.resultTuples) {
				PrimitiveValue[] primValToString = new PrimitiveValue[finalExpItemList.size()];
				for (int i = 0; i < finalExpItemList.size(); i++) {
					primValToString[i] = ExpressionEvaluator.applyCondition(resultTuple.resultRow,
							finalExpItemList.get(i), oldTableResult, cis552SO);

				}

				newTableResult.resultTuples = new ArrayList<>();
				newTableResult.resultTuples.add(new Tuple(primValToString));
			}
		}

		return newTableResult;
	}

	private void updateColDefMap(TableResult oldTableResult, TableResult newTableResult) {
		newTableResult.fromItems.addAll(oldTableResult.fromItems);
		newTableResult.aliasandTableName.putAll(oldTableResult.aliasandTableName);
		if (selectItems.get(0) instanceof AllColumns) {
//			for (FromItem fromItem : oldTableResult.fromItems) {
//				TableColumnData tableColData = cis552SO.tables.get(((Table) fromItem).getName());
//				newTableResult.colDefMap.putAll(tableColData.colDefMap);
//			}
			newTableResult.colPosWithTableAlias.putAll(oldTableResult.colPosWithTableAlias);

		} else {
			int pos = 0;
			for (SelectItem si : selectItems) {
				if (si instanceof AllTableColumns) {
//					AllTableColumns atcSi = (AllTableColumns) si;
//					TableColumnData tableColData = cis552SO.tables.get(atcSi.getTable().getName());
//					newTableResult.colDefMap.putAll(tableColData.colDefMap);
					for (Entry<Column, Integer> entrySet : oldTableResult.colPosWithTableAlias.entrySet()) {
						newTableResult.colPosWithTableAlias.put(entrySet.getKey(), pos);
					}
				} else if (si instanceof SelectExpressionItem) {
					SelectExpressionItem sei = (SelectExpressionItem) si;
					Expression exp = sei.getExpression();
					if (!(exp instanceof Function)) {
//						ColumnDefinition colDef = new ColumnDefinition();
//						ColumnDefinition oldColDef = getColDefOfExpression(exp, oldTableResult.fromItems);
//						colDef.setColDataType(oldColDef.getColDataType());
//String columnName = oldColDef.getColumnName();
						String columnName = exp.toString();
						if (exp instanceof Column) {
							Column column = (Column) exp;
							columnName = column.getColumnName();
						}
						String columnAlias = sei.getAlias() != null ? sei.getAlias() : columnName;
//						colDef.setColumnName(columnAlias);
//						colDef.setColumnSpecStrings(oldColDef.getColumnSpecStrings());
//						newTableResult.colDefMap.put(columnAlias, colDef);
						newTableResult.colPosWithTableAlias.put(new Column(null, columnAlias), pos);
					}

				}
				pos++;
			}
		}
	}

//	private ColumnDefinition getColDefOfExpression(Expression exp, List<FromItem> fromItems) {
//
//		ColumnDefinition colDef = null;
//		if (exp instanceof Column) {
//			Column column = (Column) exp;
//			colDef = getTableSchemaForColumnFromFromItems(column, fromItems).colDefMap.get(column.getColumnName());
//		}
//		if (exp instanceof Addition) {
//			Addition add = (Addition) exp;
//			colDef = getColDefOfExpression(add.getLeftExpression(), fromItems);
//			if (colDef == null) {
//				colDef = getColDefOfExpression(add.getRightExpression(), fromItems);
//			}
//		}
//		if (exp instanceof Subtraction) {
//			Subtraction sub = (Subtraction) exp;
//			colDef = getColDefOfExpression(sub.getLeftExpression(), fromItems);
//			if (colDef == null) {
//				colDef = getColDefOfExpression(sub.getRightExpression(), fromItems);
//			}
//		}
//		if (exp instanceof Multiplication) {
//			Multiplication mul = (Multiplication) exp;
//			colDef = getColDefOfExpression(mul.getLeftExpression(), fromItems);
//			if (colDef == null) {
//				colDef = getColDefOfExpression(mul.getRightExpression(), fromItems);
//			}
//		}
//		if (exp instanceof Division) {
//			Division div = (Division) exp;
//			colDef = getColDefOfExpression(div.getLeftExpression(), fromItems);
//			if (colDef == null) {
//				colDef = getColDefOfExpression(div.getRightExpression(), fromItems);
//			}
//		}
//		return colDef;
//	}
//
//	protected TableColumnData getTableSchemaForColumnFromFromItems(Column column, List<FromItem> fromItems) {
//		for (FromItem fromItem : fromItems) {
//			if (fromItem instanceof Table) {
//				Table table = (Table) fromItem;
//				TableColumnData tableSchema = cis552SO.tables.get(table.getName());
//				if (tableSchema.containsColumn(column.getColumnName())) {
//					return tableSchema;
//				}
//			}
//		}
//		return null;
//	}
}