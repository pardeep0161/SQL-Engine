package cis552project.iterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import cis552project.CIS552ProjectUtils;
import cis552project.CIS552SO;
import cis552project.SQLDataType;
import cis552project.TableColumnData;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

public class TableIT extends BaseIT {
	File tableFile = null;
	TableResult tableRes = null;
	Scanner fileScanner = null;
	List<Column> colList = null;
	TableColumnData selectTableTemp = null;

	public TableIT(Table table, CIS552SO cis552SO) {
		try {
			tableRes = new TableResult();
			tableFile = new File(cis552SO.dataPath, table.getName() + ".dat");
			fileScanner = new Scanner(tableFile);
			this.selectTableTemp = cis552SO.tables.get(table.getName());
			this.colList = selectTableTemp.colList;
			String aliasName = table.getAlias() != null ? table.getAlias() : table.getName();
			int colPos = 0;
			for (Column col : selectTableTemp.colList) {
				tableRes.colPosWithTableAlias.put(new Column(new Table(aliasName), col.getColumnName()), colPos);
				colPos++;
			}
//			tableRes.colDefMap = selectTableTemp.colDefMap;
			tableRes.fromItems.add(table);
			tableRes.aliasandTableName.put(aliasName, table.getName());
		} catch (FileNotFoundException ex1) {
			Logger.getLogger(CIS552ProjectUtils.class.getName()).log(Level.SEVERE, null, ex1);
		}

	}

	@Override
	public TableResult getNext() {
		List<Tuple> resultRows = new ArrayList<>();
		resultRows.add(fetchConvertedTupleFromStringArray(fileScanner.nextLine().split("\\|")));
		tableRes.resultTuples = resultRows;
		return tableRes;
	}

	@Override
	public boolean hasNext() {
		return fileScanner.hasNext();
	}

	@Override
	public void reset() {
		try {
			fileScanner = new Scanner(tableFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Tuple fetchConvertedTupleFromStringArray(String[] stringArray) {
		PrimitiveValue[] resultRow = new PrimitiveValue[stringArray.length];
		for (int i = 0; i < stringArray.length; i++) {

			resultRow[i] = convertStringToPV(stringArray[i], i);
		}
		return new Tuple(resultRow);
	}

	private PrimitiveValue convertStringToPV(String stringValue, int colPos) {

		ColumnDefinition colDef = selectTableTemp.colDefMap.get(colList.get(colPos).getColumnName());
		SQLDataType colSqlDataType = SQLDataType.valueOf(colDef.getColDataType().getDataType().toUpperCase());
		switch (colSqlDataType) {
		case CHAR:
		case VARCHAR:
		case STRING:
			return new StringValue(stringValue);
		case DATE:
			return new DateValue(stringValue);
		case DECIMAL:
			return new DoubleValue(stringValue);
		case INT:
			return new LongValue(stringValue);
		}
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
	}

}