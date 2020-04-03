package cis552project.iterator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cis552project.CIS552SO;
import cis552project.TableColumnData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class SelectItemIT extends BaseIT {

	BaseIT result = null;
	List<SelectItem> selectItems = null;
	CIS552SO cis552SO = null;

	public SelectItemIT(List<SelectItem> selectItems, BaseIT result, CIS552SO cis552SO) {
		this.selectItems = selectItems;
		this.result = result;
		this.cis552SO = cis552SO;
	}

	@Override
	public TableResult getNext() {
		try {
			return solveSelectItemExpression();
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

	private TableResult solveSelectItemExpression() throws SQLException {

		TableResult oldTableResult = result.getNext();
		TableResult newTableResult = oldTableResult;
		updateColDefMap(oldTableResult.fromItems, newTableResult);
		if (selectItems.get(0) instanceof AllColumns) {
			newTableResult.resultTuples = oldTableResult.resultTuples;
		} else {
			newTableResult.resultTuples = new ArrayList<>();
			List<Expression> finalExpItemList = new ArrayList<>();
			for (SelectItem selectItem : selectItems) {
				if (selectItem instanceof SelectExpressionItem) {
					finalExpItemList.add(((SelectExpressionItem) selectItem).getExpression());
				} else if (selectItem instanceof AllTableColumns) {
					AllTableColumns allTableColumn = (AllTableColumns) selectItem;
					TableColumnData tableSchema = cis552SO.tables
							.get(newTableResult.aliasandTableName.get(allTableColumn.getTable().getName()));

					finalExpItemList.addAll(tableSchema.colList.stream()
							.map(col -> (Expression) new Column(allTableColumn.getTable(), col.getColumnName()))
							.collect(Collectors.toList()));
				}
			}
			for (String[] resultRow : oldTableResult.resultTuples) {
				String[] primValToString = new String[finalExpItemList.size()];
				for (int i = 0; i < finalExpItemList.size(); i++) {

					PrimitiveValue value = ExpressionEvaluator.applyCondition(resultRow, finalExpItemList.get(i),
							newTableResult, cis552SO);
					primValToString[i] = value.toRawString();

				}
				newTableResult.resultTuples.add(primValToString);
			}
		}

		return newTableResult;
	}

	private void updateColDefMap(List<FromItem> fromItems, TableResult tableResult) {
		if (selectItems.get(0) instanceof AllColumns) {
			for (FromItem fromItem : fromItems) {
				TableColumnData tableSchema = cis552SO.tables.get(((Table) fromItem).getName());
				tableResult.colDefMap.putAll(tableSchema.colDefMap);
			}
		} else {
			for (SelectItem si : selectItems) {
				if (si instanceof AllTableColumns) {

				} else if (si instanceof SelectExpressionItem) {
					SelectExpressionItem sei = (SelectExpressionItem) si;
					Expression exp = sei.getExpression();
					ColumnDefinition colDef = new ColumnDefinition();
					ColumnDefinition oldColDef = getColDefOfExpression(exp, fromItems);
					colDef.setColDataType(oldColDef.getColDataType());

					String columnAlias = sei.getAlias() != null ? sei.getAlias() : oldColDef.getColumnName();
					colDef.setColumnName(columnAlias);
					colDef.setColumnSpecStrings(oldColDef.getColumnSpecStrings());
					tableResult.colDefMap.put(columnAlias, colDef);

				}
			}
		}
	}

	private ColumnDefinition getColDefOfExpression(Expression exp, List<FromItem> fromItems) {

		ColumnDefinition colDef = null;
		if (exp instanceof Column) {
			Column column = (Column) exp;
			colDef = getTableSchemaForColumnFromFromItems(column, fromItems).colDefMap.get(column.getColumnName());
		}
		if (exp instanceof Addition) {
			Addition add = (Addition) exp;
			colDef = getColDefOfExpression(add.getLeftExpression(), fromItems);
			if (colDef == null) {
				colDef = getColDefOfExpression(add.getRightExpression(), fromItems);
			}
		}
		if (exp instanceof Subtraction) {
			Subtraction sub = (Subtraction) exp;
			colDef = getColDefOfExpression(sub.getLeftExpression(), fromItems);
			if (colDef == null) {
				colDef = getColDefOfExpression(sub.getRightExpression(), fromItems);
			}
		}
		if (exp instanceof Multiplication) {
			Multiplication mul = (Multiplication) exp;
			colDef = getColDefOfExpression(mul.getLeftExpression(), fromItems);
			if (colDef == null) {
				colDef = getColDefOfExpression(mul.getRightExpression(), fromItems);
			}
		}
		if (exp instanceof Division) {
			Division div = (Division) exp;
			colDef = getColDefOfExpression(div.getLeftExpression(), fromItems);
			if (colDef == null) {
				colDef = getColDefOfExpression(div.getRightExpression(), fromItems);
			}
		}
		return colDef;
	}

	protected TableColumnData getTableSchemaForColumnFromFromItems(Column column, List<FromItem> fromItems) {
		for (FromItem fromItem : fromItems) {
			if (fromItem instanceof Table) {
				Table table = (Table) fromItem;
				TableColumnData tableSchema = cis552SO.tables.get(table.getName());
				if (tableSchema.containsColumn(column.getColumnName())) {
					return tableSchema;
				}
			}
		}
		return null;
	}
}
