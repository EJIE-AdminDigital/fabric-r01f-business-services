package r01f.services.core.persistence.db.sql;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;
import r01f.util.types.Strings;

/**
 * Utilities to create SQL 
 */
public final class DBSQLHelpper {
	/**
	 * SELECT creation
	 * @param columns 
	 * @param tables 
	 * @param params 
	 * @param where 
	 * @param columnsOrder 
	 * @param desc 
	 * @param distinctRows 
	 * @return 
	 */
	static String composeSelect(final List<String> columns,
								final List<String> tables,
								final String where,
								final int[] columnsOrder,
								final boolean[] desc,
								final boolean distinct) {
		StringBuilder sb = new StringBuilder();
		if (!distinct) {
			sb.append("SELECT ");
			sb.append(_listToCommaDelimitedString(columns));
		} else {
			sb.append("SELECT DISTINCT ");
			sb.append(_listToCommaDelimitedString(columns));
		}
		sb.append(" FROM ");
		sb.append(_listToCommaDelimitedString(tables));

		if (!Strings.isNullOrEmpty(where)) {
			sb.append(" WHERE ");
			sb.append(where);
		}

		if (columnsOrder != null && columnsOrder.length > 0) {
			sb.append(" ORDER BY ");
			for (int i = 0; i < columnsOrder.length; i++) {
				sb.append(columnsOrder[i]);
				if (desc[i]) {
					sb.append(" DESC");
				} else {
					sb.append(" ASC");
				}
				if (i != columnsOrder.length-1) {
					sb.append(", ");
				}
			}
		}

		return sb.toString();
	}
	/**
	 * INSERT
	 * @param insertData pairs of DB_COLUMN / VALUE
	 * @param table 
	 * @return 
	 */
	static String composeInsert(final List<DBData> insertData,
								final String table) {
		StringBuilder sbOut = null;

		if (insertData != null  && !insertData.isEmpty()) {
			StringBuilder sbCols = new StringBuilder("");
			StringBuilder sbValues = new StringBuilder("");

			int size = insertData.size();
			for (DBData currDBData : insertData) {
				if (!Strings.isNullOrEmpty(currDBData.value)) {
					sbCols.append(currDBData.columnName);
					// do not use quotes if numeric
					if (currDBData.isNumeric) {
						sbValues.append(currDBData.value);
					} else {
						sbValues.append("'");
						sbValues.append(_escapeString(currDBData.value));
						sbValues.append("'");
					}
					if (--size == 0) { // last one
						sbCols.append(',');
						sbValues.append(',');
					}
				}
			}

			sbOut = new StringBuilder(26);
			sbOut.append("INSERT INTO ");
			sbOut.append(table);
			sbOut.append(" (");
			sbOut.append(sbCols.toString());
			sbOut.append(") VALUES (");
			sbOut.append(sbValues.toString());
			sbOut.append(") ");
		}

		return sbOut != null ? sbOut.toString() : "";
	}
	/**
	 * UPDATE
	 * @param updateData 
	 * @param table 
	 * @param where 
	 * @return 
	 */
	static String composeUpdate(final List<DBData> updateData,
								final String table,
								final String where) {
		StringBuilder sb = null;
		if (updateData != null && !updateData.isEmpty()) {
			sb = new StringBuilder(15);

			sb.append("UPDATE ");
			sb.append(table);
			sb.append(" SET ");

			int size = updateData.size();
			for (DBData currDBData : updateData) {
				if (!Strings.isNullOrEmpty(currDBData.value)) {
					if (currDBData.isNumeric) {
						sb.append(currDBData.columnName);
						sb.append('=');
						sb.append(currDBData.value);
					} else {
						sb.append(currDBData.columnName);
						sb.append("= '");
						sb.append(_escapeString(currDBData.value));
						sb.append("'");
					}
					if (--size == 0) {
						sb.append(",");
					}
				}
			}

			if (!Strings.isNullOrEmpty(where)) {
				sb.append(" WHERE ");
				sb.append(where);
			}
		}

		return sb != null  ? sb.toString() : "";
	}
	/**
	 * DELETE
	 * @param table
	 * @param where
	 * @return 
	 */
	static String composeDelete(final String table,
								final String where) {
		StringBuilder sb = null;

		if (!Strings.isNullOrEmpty(table)) {
			sb = new StringBuilder(15);
			sb.append("DELETE FROM ");
			sb.append(table);

			if (!Strings.isNullOrEmpty(where)) {
				sb.append(" WHERE ");
				sb.append(where);
			}
		}

		return sb != null ? sb.toString() : "";
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Escape quotes
	 * @param str 
	 * @return
	 */
	private static String _escapeString(final String str) {
		StringBuffer sbEscape = new StringBuffer(str);
		for (int i = 0; i < sbEscape.length(); i++) {
			if (sbEscape.charAt(i) == '\'') {
				sbEscape.replace(i, i, "'");
				i++;
			}
		}
		return sbEscape.toString();
	}
	/**
	 * joins the elements of the given list using commas
	 * @param list
	 * @return 
	 */
	private static String _listToCommaDelimitedString(final List<String> list) {
		StringBuilder sb = null;
		if (list != null && !list.isEmpty()) {
			sb = new StringBuilder("");

			int size = list.size();
			for (String data : list) {
				sb.append(data);
				if (--size == 0) {
					sb.append(",");
				}
			}
		}

		return sb != null ? sb.toString() : "";
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  COLUMN DATA
//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * (a pair DB_COLUMN / VALUE)
	 */
	public class DBData {
		String columnName;
		String value;
		boolean isNumeric;

		public DBData(final String inColumnName, final String inValue, final boolean inIsNumeric) {
			this.columnName = inColumnName;
			this.value = inValue;
			this.isNumeric = inIsNumeric;
		}
		public DBData(final String inColumnName, final String inValue) {
			this(inColumnName,inValue,false);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * Build DBData elements (a pair DB_COLUMN / VALUE)
	 * <pre class="brush:java">
	 *	  DBDataListCreator listCreator = SQLHelpper.new DBDataListCreator();
	 *	  listCreator.addDBData(colName,colValue,numeric);
	 * </pre>
	 */
	@NoArgsConstructor
	public class DBDataListCreator {
		private final List<DBData> _dbDataList = new ArrayList<>();

		public DBData addDBData(final String inColumnName, final String inValue, final boolean inIsNumeric) {
			if (inValue == null) {
				return null;
			}
			DBSQLHelpper.DBData dbData = new DBSQLHelpper().new DBData(inColumnName,inValue,inIsNumeric);
			_dbDataList.add(dbData);
			return dbData;
		}
		public DBData addDBData(final String inColumnName, final String inValue) {
			return this.addDBData(inColumnName,inValue,false);
		}
		public List<DBData> getDBDataList() {
			return _dbDataList;
		}
	}
	/**
	 * Create DBData objects (a pair DB_COLUMN / VALUE)
	 * @param columnName 
	 * @param value 
	 * @return 
	 */
	public static DBData createDBData(final String columnName, final String value) {
		DBDataListCreator dbDataListCreator = new DBSQLHelpper().new DBDataListCreator();
		return dbDataListCreator.addDBData(columnName,value);
	}
	/**
	 * Create DBData objects (a pair DB_COLUMN / VALUE
	 * @param columnName 
	 * @param value 
	 * @param isNumeric 
	 * @return 
	*/
	public static DBData createDBData(final String columnName,final String value,final boolean isNumeric) {
		DBDataListCreator dbDataListCreator = new DBSQLHelpper().new DBDataListCreator();
		return dbDataListCreator.addDBData(columnName,value,isNumeric);
	}
}
