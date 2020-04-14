package cis552project.iterator;

import cis552project.CIS552SO;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class PlainSelectIT extends BaseIT {

	BaseIT result = null;

	public PlainSelectIT(PlainSelect plainSelect, CIS552SO cis552SO) {
		result = new FromItemIT(plainSelect.getFromItem(), cis552SO);
		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				BaseIT result2 = new FromItemIT(join.getRightItem(), cis552SO);
				result = new JoinIT(result, result2);
			}
		}
		if (plainSelect.getWhere() != null) {
			result = new WhereIT(plainSelect.getWhere(), result, cis552SO);
		}

		if (plainSelect.getGroupByColumnReferences() != null) {
			result = new GroupByIT(plainSelect.getGroupByColumnReferences(), result, cis552SO);
		}
		if (plainSelect.getLimit() != null) {
			// implement limit
			result = new GroupByIT(plainSelect.getGroupByColumnReferences(), result, cis552SO);
		}
		result = new SelectItemIT(plainSelect.getSelectItems(), result, cis552SO);
		if (plainSelect.getDistinct() != null) {
			result = new DistinctIT(result);
		}

	}

	@Override
	public TableResult getNext() {
		return result.getNext();
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

}