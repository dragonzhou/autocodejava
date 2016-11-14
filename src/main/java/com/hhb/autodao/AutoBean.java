package com.hhb.autodao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author haibo.huang
 * 
 */
public class AutoBean {
	private static final Logger	LOGGER		= LoggerFactory.getLogger(AutoBean.class);

	private static Connection	conn;
	private static Statement	stm;
	private static ResultSet	rs;
	private static String		driver		= "com.mysql.jdbc.Driver";
	private static String		database_name		= "create";
	private static String		url			= "jdbc:mysql://localhost:3306/"+database_name;
	private static String		username	= "root";
	private static String		password	= "andy";
	private static String		packageName	= "com.company.biz.huiche";

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
					ClassNotFoundException {
		System.out.println(jNameToDbName("javaHava").toLowerCase());
		try {
			DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
			conn = DriverManager.getConnection(url, username, password);
			stm = conn.createStatement();
			// 传递数据原
			List<String> ltn = getAllTabName(database_name);
			System.out.println("----------connect success----------ltn:" + ltn);
			Map<String, List<Colm>> mtcn = getTabColName(ltn);
			mtcn = toUL(mtcn);

			// init
			WriteDBToJava.init(packageName, null);

			// create files
			WriteDBToJava.writeJavaBean(mtcn);
			// WriteDBToJava.writeXML(mtcn);
			WriteDBToJava.writeDAO(mtcn);
			WriteDBToJava.writeDAOImpl(mtcn);
			WriteDBToJava.writeService(mtcn);
			WriteDBToJava.writeServiceImp(mtcn);

		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private static List<String> getAllTabName(String datasource) {
		List<String> ltn = new ArrayList<String>();
		try {

			// rs =
			// stm.executeQuery("select table_name from all_tables WHERE owner='"
			// + username.toUpperCase() + "'");
			rs = stm.executeQuery("select table_name from information_schema.tables where table_schema='" + datasource
							+ "' and table_type='base table';");
			while (rs.next()) {
				ltn.add(rs.getString(1));
			}

		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return ltn;
	}

	private static Map<String, List<Colm>> getTabColName(List<String> allTabName) {
		Map<String, List<Colm>> mtcn = new HashMap<String, List<Colm>>();
		try {

			DatabaseMetaData dbmd = conn.getMetaData();
			for (String n : allTabName) {
				rs = stm.executeQuery("select * from " + n + " where 1 < 0");

				// 只为了单纯的加个注释,还不想破坏原来的代码结构，先放入map中===start===
				Map<String, String> commentMap = new HashMap<String, String>();
				ResultSetMetaData rsmd = rs.getMetaData();
				ResultSet resultSet = dbmd.getTables(null, "%", "%", new String[] { "TABLE" });
				while (resultSet.next()) {
					String tableName = resultSet.getString("TABLE_NAME");
					LOGGER.info("tableName:" + tableName);
					ResultSet rs = dbmd.getColumns(null, "%", tableName, "%");
					while (rs.next()) {
						LOGGER.info(rs.getString("COLUMN_NAME") + ":" + rs.getString("REMARKS"));
						commentMap.put(rs.getString("COLUMN_NAME"), rs.getString("REMARKS"));
					}
				}
				// ===end====

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columnName = rsmd.getColumnLabel(i);
					LOGGER.info("columnName: " + columnName);
					String columnType = rsmd.getColumnTypeName(i);
					LOGGER.info("oracle columnType: " + columnType);
					String typeImport = rsmd.getColumnClassName(i);
					LOGGER.info("java full type: " + typeImport);
					String typeName = typeImport.substring(typeImport.lastIndexOf(".") + 1, typeImport.length());
					LOGGER.info("java type: " + typeName);
					List<Colm> l = mtcn.get(n);
					Colm colm = new Colm();
					colm.setDbName(columnName);
					colm.setType(columnType);
					colm.setTypeName(typeDbToJava(typeName));
					LOGGER.info("....................." + commentMap.get(columnName));
					colm.setComment(commentMap.get(columnName)); // 设置注释

					colm.setTypeImport(typeDbToJava(typeImport));
					if (null == l) {
						l = new ArrayList<Colm>();
						l.add(colm);
						mtcn.put(n, l);
					} else {
						l.add(colm);
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return mtcn;
	}

	private static Map<String, List<Colm>> toUL(Map<String, List<Colm>> mtcn) {
		Map<String, List<Colm>> allTab = new HashMap<String, List<Colm>>();
		Set<String> s = mtcn.keySet();
		for (Iterator<String> iterator = s.iterator(); iterator.hasNext();) {
			String tn = (String) iterator.next();
			boolean b = true;
			StringBuffer sbTabName = new StringBuffer();

			for (int i = 0; i < tn.length(); i++) {
				char c = tn.charAt(i);
				if (b) {
					sbTabName.append(toU(c));
					b = false;
				} else {
					if (c == '_') {
						b = true;
					} else {
						sbTabName.append(toL(c));
					}
				}
			}
			// sbTabName.append(".java");
			// LOGGER.info(sbTabName);

			List<Colm> lcn = mtcn.get(tn);
			for (Colm cn : lcn) {
				StringBuffer sbColumName = new StringBuffer();
				boolean bb = false;
				for (int i = 0; i < cn.getDbName().length(); i++) {
					char c = cn.getDbName().charAt(i);
					if (bb) {
						sbColumName.append(toU(c));
						bb = false;
					} else {
						if (c == '_') {
							bb = true;
						} else {
							sbColumName.append(toL(c));
						}
					}
				}
				List<Colm> allColum = allTab.get(sbTabName.toString());
				Colm colm = new Colm();
				colm.setName(sbColumName.toString());
				colm.setDbName(cn.getDbName());
				colm.setType(cn.getType());
				colm.setTypeImport(cn.getTypeImport());
				colm.setTypeName(cn.getTypeName());
				colm.setComment(cn.getComment());
				// LOGGER.info(sbColumName.toString());
				// LOGGER.info(cn.getDbName());
				// LOGGER.info(cn.getType());
				// LOGGER.info(cn.getTypeName());
				// LOGGER.info("------------------------------");
				if (null == allColum) {
					allColum = new ArrayList<Colm>();
					allTab.put(sbTabName.toString(), allColum);
					allColum.add(colm);
				} else {
					allColum.add(colm);
				}
				// LOGGER.info(sbColumName);
			}
		}
		return allTab;
	}

	public static char toU(char c) {
		if (c >= 'a' && c <= 'z') {
			return c -= 32;
		}
		return c;
	}

	public static char toL(char c) {
		if (c >= 'A' && c <= 'Z') {
			return c += 32;
		}
		return c;
	}

	public static boolean isU(char c) {
		if (c >= 'A' && c <= 'Z') {
			return true;
		}
		return false;
	}

	public static String jNameToDbName(String jName) {
		char[] cs = jName.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cs.length; i++) {
			if (i > 0 && isU(cs[i])) {
				sb.append("_");
			}
			sb.append(toU(cs[i]));
		}
		return sb.toString();
	}

	public static String getUpper(String input) {

		char[] cs = input.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cs.length; i++) {
			if (i == 0) {
				sb.append(toU(cs[i]));
			} else {
				sb.append((cs[i]));
			}
		}
		return sb.toString();

	}

	/**
	 * @param type
	 * @return
	 */
	public static String typeDbToJava(String type) {
		if (type.equals("")) {
			return "String";

		} else if (type.equals("TIMESTAMP") || type.equals("Timestamp")) {
			return "Date";
		} else if (type.equals("oracle.sql.TIMESTAMP") || type.equals("java.sql.Timestamp")) {
			return "java.util.Date";

			// 榛樿鏁板瓧绫诲瀷閮界敓鎴愰噾棰濆瓧娈�
		}
		// else if(type.equals("Long")){
		// return "Money";
		// }else if(type.equals(" java.lang.Long")){
		// return "com.ucf.onlinepay.framework.common.bean.Money";
		//
		// }else if(type.equals("Integer")){
		// return "Money";
		// }else if(type.equals(" java.lang.Integer")){
		// return "com.ucf.onlinepay.framework.common.bean.Money";
		//
		// }
		else {
			return type;
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public static String typeDbToJava2(String type) {
		if (type.equals("VARCHAR2")) {
			return "VARCHAR";
		} else if (type.equals("NUMBER")) {
			return "NUMERIC";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else if (type.equals("")) {
			return "";
		} else {
			return type;
		}
	}

}
