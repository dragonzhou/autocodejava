package com.auto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteDBToJava {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(WriteDBToJava.class);

	private static String		pakg;
	private static String		url		= "d:/test/";
	private static String		subUrl1	= "model";
	private static String		subUrl2	= "mybatis";
	private static String		subUrl3	= "dao";
	private static String		subUrl4	= "service";
	private static String		rowNum	= "10";

	public static void init(String package_name, String filePath) {
		pakg = package_name;
		if (filePath != null && filePath.length() > 0) {
			url = filePath;
		}
	}

	public static String longtoInteger(String name) {
		if ("long".equalsIgnoreCase(name)) {
			return "Integer";
		}
		if ("Boolean".equalsIgnoreCase(name)) {
			return "Integer";
		}
		return name;
	}

	public static void writeJavaBean(Map<String, List<Colm>> mTables) {

		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Map<String, String> imports = new HashMap<String, String>();
			try {
				File tmp = new File(url);
				if (!tmp.isDirectory()) {
					tmp.mkdir();
				}
				File f = new File(url + subUrl1 + "/");
				f.mkdir();

				f = new File(url + subUrl1 + "/" + key + ".java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				output.write("package " + pakg + "." + subUrl1 + ";\n");
				output.write("\n");

				for (Colm colm : l) {
					if (imports.get(colm.getTypeImport()) == null && !"java.lang.String".equals(colm.getTypeImport())) {
						output.write("import " + colm.getTypeImport() + ";");
						output.write("\n");
						imports.put(colm.getTypeImport(), colm.getTypeImport());
					}
				}
				output.write("\n");
				output.write("public class " + key + " {\n\n");
				StringBuffer line;
				for (Colm colm : l) {
					line = new StringBuffer();
					line.append("\t ");
					line.append("private ");
					line.append(longtoInteger(colm.getTypeName()) + " ");
					LOGGER.info("xxxxxxxxxxxxxx:" + colm.getComment());
					line.append(colm.getName() + "; //" + colm.getComment() + "\n\n");
					output.write(line.toString());
				}
				for (Colm colm : l) {
					// set
					line = new StringBuffer();
					line.append("\t\t");
					line.append("public ");
					line.append(key + " ");
					line.append("set" + AutoBean.toU(colm.getName().charAt(0))
									+ colm.getName().substring(1, colm.getName().length()));
					line.append(" (" + longtoInteger(colm.getTypeName()) + " " + colm.getName() + ") {\n");
					output.write(line.toString());

					line = new StringBuffer();
					line.append("\t\t\tthis." + colm.getName() + " = " + colm.getName());
					line.append(";\n");
					output.write(line.toString());

					line = new StringBuffer();
					line.append("\t\t\treturn this");
					line.append(";\n");
					output.write(line.toString());

					line = new StringBuffer();
					line.append("\t\t}");
					line.append("\n\n");
					output.write(line.toString());

					// get
					line = new StringBuffer();
					line.append("\t\t");
					line.append("public ");
					line.append(longtoInteger(colm.getTypeName()) + " ");
					line.append("get" + AutoBean.toU(colm.getName().charAt(0))
									+ colm.getName().substring(1, colm.getName().length()));
					line.append(" () {\n");
					output.write(line.toString());

					line = new StringBuffer();
					line.append("\t\t\treturn " + colm.getName());
					line.append(";\n");
					output.write(line.toString());

					line = new StringBuffer();
					line.append("\t\t}");
					line.append("\n\n");
					output.write(line.toString());
				}
				output.write("\t\tpublic " + key + " (){ \n\t\t\tsuper();\n\t\t}\n");

				output.write("}\n");
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param mybatis
	 */
	public static void writeXML(Map<String, List<Colm>> mTables) {
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			// System.out.println("<typeAlias type=\""+pakg+"pojo."+key+"\" alias=\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"\" />");
			// Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url + subUrl2 + "/");
				f.mkdir();
				f = new File(url + subUrl2 + "/" + AutoBean.jNameToDbName(key).toLowerCase() + "_sql.xml");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
				output.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
				// 增加了类名
				output.write("<mapper namespace=\"" + pakg.substring(0, pakg.length() - 1) + "." + key + "Dao\">\n");

				// 插入操作
				output.write("<!-- insert " + key + " -->\n");
				output.write("<insert id=\"insert" + key + "\" parameterType=\"" + key
								+ "\" keyProperty=\"id\" useGeneratedKeys=\"true\">\n");
				output.write("\t<![CDATA[\n");
				output.write("\t\tINSERT INTO " + AutoBean.jNameToDbName(key) + " (\n");
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				for (int i = 0; i < l.size(); i++) {
					if (i > 0) {
						sb1.append(",");
						sb2.append(",");
					}
					Colm c = l.get(i);
					sb1.append(c.getDbName());
					// 去掉特殊处理
					// if(i == 0){
					// sb2.append(AutoBean.jNameToDbName(key)+"_SEQ.nextval");
					// }else{
					// sb2.append("#{"+c.getName()+",jdbcType="+AutoBean.typeDbToJava2(c.getType())+"}");
					// }
					sb2.append("#{" + c.getName() + ",jdbcType=" + AutoBean.typeDbToJava2(c.getType()) + "}");

				}
				output.write("\t" + sb1.toString() + "\n");
				output.write("\t\t)VALUES(\n");
				output.write("\t\t" + sb2.toString());
				output.write(")\n\t]]>\n</insert>\n");
				output.write("\n");

				// 修改操作
				output.write("<!-- update " + key + " -->\n");
				// 去掉驼峰
				output.write("<update id=\"update" + key + "\" parameterType=\"" + key + "\">\n");
				// output.write("<update id=\"update"+key+"\" parameterType=\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"\">\n");
				output.write("\t<![CDATA[\n");
				output.write("\t\tUPDATE \n");
				output.write("\t\t" + AutoBean.jNameToDbName(key) + " \n");
				output.write("\t\tSET \n");
				Colm cl = l.get(0);
				output.write("\t\t " + cl.getDbName() + "=" + cl.getDbName() + "\n");
				output.write("\t]]>\n");
				StringBuffer sb3 = new StringBuffer("\t\t");
				for (int i = 1; i < l.size(); i++) {
					Colm c = l.get(i);

					// 空的字段不做修改操作...
					// if(c.getTypeName().equals("String")){
					// sb3.append("\t<if test=\"null != "+c.getName()+" and '' != "+c.getName()+"\">\n");
					// }else{
					sb3.append("\t<if test=\"null != " + c.getName() + "\">\n");
					// }
					sb3.append("\t<![CDATA[\n");
					sb3.append(",");
					sb3.append(c.getDbName());
					sb3.append(" = #{");
					sb3.append(c.getName());
					sb3.append("}");
					sb3.append("\n\t]]>\n");
					sb3.append("\t</if>\n");
				}
				sb3.append("\t<![CDATA[\n");
				sb3.append("\n");
				output.write(sb3.toString());
				output.write("\t\tWHERE  " + cl.getDbName() + " = #{" + cl.getName() + "} \n");
				output.write("\t]]>\n");
				output.write("</update>\n\n");

				// 删除操作
				output.write("<!-- delete " + key + " -->\n");
				output.write("<delete id=\"delete" + key + "\" parameterType=\"string\">\n");
				output.write("\t<![CDATA[\n");
				output.write("\t\tDELETE FROM " + AutoBean.jNameToDbName(key) + " WHERE id = #{id}\n");
				output.write("\t]]>\n");
				output.write("</delete>\n");
				output.write("\n");

				// 查询操作
				output.write("<!-- select " + key + " -->\n");
				output.write("<select id=\"query" + key + "ByWhere\" resultType=\"" + key + "\" parameterType=\"" + key
								+ "\" >\n");
				output.write("<![CDATA[\n");
				output.write("SELECT \n");
				StringBuffer sb4 = new StringBuffer();
				for (int i = 0; i < l.size(); i++) {
					if (i > 0) {
						sb4.append(",");
					}
					Colm cc = l.get(i);
					sb4.append(cc.getDbName());
					sb4.append(" as ");
					sb4.append(cc.getName());
				}
				output.write(sb4.toString());
				// output.write(" FROM "+AutoBean.jNameToDbName(key)+" WHERE ROWNUM <= "+rowNum+" \n");
				output.write(" FROM " + AutoBean.jNameToDbName(key) + " WHERE 1=1 \n");
				output.write("]]>\n");
				for (Colm colm : l) {
					if (colm.getTypeName().equals("String")) {
						output.write("<if test=\"null != " + colm.getName() + " and '' != " + colm.getName() + "\">\n");
					} else {
						output.write("<if test=\"null != " + colm.getName() + "\">\n");
					}
					output.write("<![CDATA[\n");
					output.write("AND " + colm.getDbName() + " = #{" + colm.getName() + "}\n");
					output.write("]]>\n");
					output.write("</if>\n");
				}
				output.write("</select>\n");
				output.write("</mapper>\n");

				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param dao
	 */
	public static void writeDAO(Map<String, List<Colm>> mTables) {
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			// Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url + subUrl3 + "/");
				f.mkdir();
				f = new File(url + subUrl3 + "/" + key + "Dao.java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				String s = AutoBean.toL(key.charAt(0)) + key.substring(1, key.length());
				output.write("package " + pakg + "." + subUrl3 + ";\n\n");
				output.write("import java.util.List;\n");
				output.write("import " + pakg + "." + subUrl1 + "." + key + ";\n");
				// output.write("import com.ucf.onlinepay.framework.exception.in.FnFiTechnicalException;\n\n");

				// output.write("@Repository(\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"Dao\")\n");
				output.write("public interface " + key + "Dao  {\n\n");
				output.write("\tboolean insert" + "(" + key + " " + s + ");\n\n");
				output.write("\tboolean update" + "(" + key + " " + s + ");\n\n");
				output.write("\tboolean delete" + "(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ ");\n\n");
				output.write("\t" + key + " get" + "(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ ");\n\n");
				output.write("\tList< " + key + ">  list(" + key + " " + s + ",int pageStart, int pageSize);\n");
				output.write("\tint" + " count("+ key + " " + s +");\n");
				output.write("}");
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public static String getInt(String type) {
		if (type.equals("CHAR")) {
			return "String";
		}
		if (type.equals("VARCHAR")) {
			return "String";
		}
		if (type.equals("INT")) {
			return "Int";
		}
		if (type.equals("DATETIME")) {
			return "Date";
		}
		if (type.contains("INT")) {
			return "Int";
		}
		if (type.contains("DOUBLE")) {
			return "Double";
		}
		return type;
	}

	public static String getInteger(String type) {
		if (type.equals("VARCHAR")) {
			return "String";
		}
		if (type.equals("INT")) {
			return "Integer";
		}
		if (type.equals("DATETIME")) {
			return "Date";
		}
		if (type.contains("INT")) {
			return "Integer";
		}

		return type;
	}

	/**
	 * @param daoImpl
	 */
	public static void writeDAOImpl(Map<String, List<Colm>> mTables) {
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			// Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url + subUrl3 + "/impl/");
				f.mkdir();
				f = new File(url + subUrl3 + "/impl/" + key + "DaoImpl.java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				String s = AutoBean.toL(key.charAt(0)) + key.substring(1, key.length());
				output.write("package " + pakg + "." + subUrl3 + ".impl;\n\n");
				output.write("import org.springframework.stereotype.Repository;\n");
				output.write("import com" + ".company" + ".common.core.data.jdbc.Jdbc;\n");
				output.write("import org.springframework.beans.factory.annotation.Autowired;\n");
				output.write("import org.apache.commons.lang.StringUtils;\n");
				output.write("import com" + ".company" + ".common.core.data.jdbc.StatementParameter;\n");
				output.write("import com" + ".company" + ".common.core.data.jdbc.builder.InsertBuilder;\n");
				output.write("import com" + ".company" + ".common.core.data.jdbc.builder.UpdateBuilder;\n");
				output.write("import java.util.List;\n");
				output.write("import " + pakg + "." + subUrl1 + "." + key + ";\n");
				output.write("import " + pakg + "." + subUrl3 + "." + key + "Dao;\n");
				// output.write("import com.ucf.onlinepay.framework.exception.in.FnFiTechnicalException;\n\n");
				output.write("@Repository\n");
				output.write("public class " + key + "DaoImpl  implements " + key + "Dao {\n\n");
				// table name
				output.write("\tprivate static final String	TABLE	= \"" + AutoBean.jNameToDbName(s).toLowerCase()
								+ "\";\n");
				output.write("\t@Autowired\n");
				output.write("\tprivate Jdbc				jdbc;\n\n");
				output.write("\tpublic boolean insert" + "(" + key + " " + s + "){\n");
				output.write("\t\tInsertBuilder ib = new InsertBuilder(TABLE);\n");
				for (Colm colm : l) {
					output.write("\t\tib.set" + getInt(colm.getType()) + "(\""
									+ AutoBean.jNameToDbName(colm.getName()).toLowerCase() + "\"," + s + ".get"
									+ AutoBean.getUpper(colm.getName()) + "());\n");
				}
				// 插入
				output.write("\t\treturn this.jdbc.updateForBoolean(ib);\n");
				output.write("\t}\n\n");
				output.write("\tpublic boolean update" + "(" + key + " " + s + "){\n");
				output.write("\t\tUpdateBuilder ub = new UpdateBuilder(TABLE);\n");
				for (Colm colm : l) {
					output.write("\t\tif (null != " + s + ".get" + AutoBean.getUpper(colm.getName()) + "()){\n");
					output.write("\t\tub.set" + getInt(colm.getType()) + "(\""
									+ AutoBean.jNameToDbName(colm.getName()).toLowerCase() + "\"," + s + ".get"
									+ AutoBean.getUpper(colm.getName()) + "());\n");
					output.write("\t\t}\n\n");
				}
				output.write("\t\tub.where.set" + getInt(l.get(0).getType()) + "(\""
								+ AutoBean.jNameToDbName(l.get(0).getName()).toLowerCase() + "\"," + s + ".get"
								+ AutoBean.getUpper(l.get(0).getName()) + "());\n");
				// 插入
				output.write("\t\treturn this.jdbc.updateForBoolean(ub);\n");
				output.write("\t}\n\n");
				output.write("\tpublic boolean delete(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ "){\n");
				output.write("\t\tString sql = \"delete  from \" + TABLE + \" where " + l.get(0).getName()
								+ " = ?\";\n");
				output.write("\t\tStatementParameter sp = new StatementParameter();\n");
				output.write("\t\tsp.set" + getInt(l.get(0).getType()) + "( " + l.get(0).getName() + ");\n");
				output.write("\t\treturn this.jdbc.updateForBoolean(sql, sp);\n");
				output.write("\t}\n\n");
				output.write("\tpublic " + key + " get(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ "){\n");
				output.write("\t\tString sql = \"select * from \" + TABLE + \" where " + l.get(0).getName()
								+ " = ?\";\n");
				output.write("\t\tStatementParameter sp = new StatementParameter();\n");
				output.write("\t\tsp.set" + getInt(l.get(0).getType()) + "( " + l.get(0).getName() + ");\n");
				output.write("\t\treturn this.jdbc.query(sql," + key + ".class, sp);\n");
				output.write("\t}\n\n");
				output.write("\tpublic List<" + key + "> list" + "(" + key + " " + s
								+ ",int pageStart, int pageSize){\n");
				output.write("\t\tStringBuilder sb = new StringBuilder();\n");
				output.write("\t\tsb.append(\"select * from \");\n");
				output.write("\t\tsb.append(TABLE);\n");
				output.write("\t\tsb.append(\" where 1=1 \");\n");
				output.write("\t\tStatementParameter sp = new StatementParameter();\n");

				for (Colm colm : l) {
					if(!getInt(colm.getType()).equals("Date"))
					{
						output.write("\t\tif (null != " + s + ".get" + AutoBean.getUpper(colm.getName()) + "()){\n");
						output.write("\t\tsb.append" + "(\" and " + AutoBean.jNameToDbName(colm.getName()).toLowerCase()
										+ "=?\");\n");
						output.write("\t\tsp.set" + getInt(colm.getType()) + "(" + s + ".get"
										+ AutoBean.getUpper(colm.getName()) + "());\n");
						output.write("\t\t}\n\n");
					}
				}
				output.write("\t\tif (pageSize > 0) {\n");
				output.write("\t\tsb.append(\" limit ?, ?\");\n");
				output.write("\t\tsp.setInt(pageStart);\n");
				output.write("\t\tsp.setInt(pageSize);\n");
				output.write("\t\t}\n");
				output.write("\t\treturn this.jdbc.queryForList(sb.toString(), " + key + ".class, sp);\n");
				output.write("\t}\n\n");

				output.write("\tpublic int count(" + key + " " + s + "){\n");
				output.write("\t\tStringBuilder sb = new StringBuilder();\n");
				output.write("\t\tsb.append(\"select count(1) from \");\n");
				output.write("\t\tsb.append(TABLE);\n");
				output.write("\t\tsb.append(\" where 1=1 \");\n");
				output.write("\t\tStatementParameter sp = new StatementParameter();\n");
				for (Colm colm : l) {
					if(!getInt(colm.getType()).equals("Date"))
					{
						output.write("\t\tif (null != " + s + ".get" + AutoBean.getUpper(colm.getName()) + "()){\n");
						output.write("\t\tsb.append" + "(\" and " + AutoBean.jNameToDbName(colm.getName()).toLowerCase()
										+ "=?\");\n");
						output.write("\t\tsp.set" + getInt(colm.getType()) + "(" + s + ".get"
										+ AutoBean.getUpper(colm.getName()) + "());\n");
						output.write("\t\t}\n\n");
					}

				}
				output.write("\t\treturn this.jdbc.queryForInt(sb.toString(),sp);\n");
				output.write("\t}\n\n");

				output.write("}");
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param service
	 */
	public static void writeService(Map<String, List<Colm>> mTables) {
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			// Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url + subUrl4 + "/");
				f.mkdir();
				f = new File(url + subUrl4 + "/" + key + "Service.java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				String s = AutoBean.toL(key.charAt(0)) + key.substring(1, key.length());
				output.write("package " + pakg + "." + subUrl4 + ";\n\n");
				output.write("import java.util.List;\n");
				output.write("import " + pakg + "." + subUrl1 + "." + key + ";\n");
				output.write("import java.util.List;\n");
				output.write("import com.company.common.core.data.jdbc.Page;\n");
				// output.write("import com.ucf.onlinepay.framework.exception.in.FnFiTechnicalException;\n\n");

				// output.write("@Repository(\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"Dao\")\n");
				output.write("public interface " + key + "Service  {\n\n");
				output.write("\tboolean insert" + "(" + key + " " + s + ");\n\n");
				output.write("\tboolean update" + "(" + key + " " + s + ");\n\n");
				output.write("\tboolean delete" + "(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ ");\n\n");
				output.write("\t" + key + " get" + "(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ ");\n\n");
				// output.write("\tList<"+key+"> query"+key+"ByWhere("+key+" "+s+")throws Exception;\n\n");
				output.write("\tPage< " + key + ">  list" + "(" + key + " " + s + ",int pageStart, int pageSize"
								+ ");\n");
				output.write("}");
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param daoImpl
	 */
	public static void writeServiceImp(Map<String, List<Colm>> mTables) {
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			// Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url + subUrl4 + "/impl/");
				f.mkdir();
				f = new File(url + subUrl4 + "/impl/" + key + "ServiceImpl.java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				String s = AutoBean.toL(key.charAt(0)) + key.substring(1, key.length());
				output.write("package " + pakg + "." + subUrl4 + ".impl;\n\n");
				output.write("import org.springframework.stereotype.Service;\n");
				output.write("import java.util.List;\n");
				output.write("import org.springframework.beans.factory.annotation.Autowired;\n");
				output.write("import com.company.common.core.data.jdbc.Page;\n");
				output.write("import " + pakg + "." + subUrl1 + "." + key + ";\n");
				output.write("import " + pakg + "." + subUrl4 + "." + key + "Service;\n");
				output.write("import " + pakg + "." + "dao" + "." + key + "Dao;\n");
				// output.write("import com.ucf.onlinepay.framework.exception.in.FnFiTechnicalException;\n\n");
				output.write("@Service\n");
				output.write("public class " + key + "ServiceImpl  implements " + key + "Service {\n\n");

				output.write("\t@Autowired\n");
				output.write("\tprivate " + key + "Dao " + s + "Dao" + ";\n\n");

				output.write("\tpublic boolean insert" + "(" + key + " " + s + "){\n");
				output.write("\t\treturn this." + s + "Dao" + ".insert(" + s + ");\n");
				output.write("\t}\n\n");
				output.write("\tpublic boolean update" + "(" + key + " " + s + "){\n");
				output.write("\t\treturn this." + s + "Dao" + ".update(" + s + ");\n");
				output.write("\t}\n\n");
				output.write("\tpublic boolean delete(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ "){\n");
				output.write("\t\treturn this." + s + "Dao" + ".delete(" + l.get(0).getName() + ");\n");
				output.write("\t}\n\n");
				output.write("\tpublic " + key + " get(" + getInteger(l.get(0).getType()) + " " + l.get(0).getName()
								+ "){\n");
				output.write("\t\treturn this." + s + "Dao" + ".get(" + l.get(0).getName() + ");\n");
				output.write("\t}\n\n");
				output.write("\tpublic Page<" + key + "> list(" + key + " " + s + ",int pageStart, int pageSize"
								+ "){\n");
				output.write("\t\t" + "Page<" + key + ">" + " " + "page=new " + " Page<" + key + ">()" + ";\n");
				output.write("\t\t" + "List<" + key + ">" + " " + "data=" + "this." + s + "Dao"
								+ ".list("+s+",pageStart,pageSize)" + ";\n");
				output.write("\t\t" + "int" + " " + "count=" + "this." + s + "Dao" + ".count("+s + ");\n");
				output.write("\t\tpage.setCount(count);\n");
				output.write("\t\tpage.setData(data);\n");
				output.write("\t\treturn page;\n");
				output.write("\t}\n\n");

				output.write("}");
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
