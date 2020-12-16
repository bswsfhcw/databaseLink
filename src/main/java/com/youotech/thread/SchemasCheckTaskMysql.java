package com.youotech.thread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 
 * @author bswsfhcw
 *
 */
@Component
@Order(1)
public class SchemasCheckTaskMysql implements DisposableBean, Runnable,CommandLineRunner  {
	protected static Logger LOGGER = LoggerFactory.getLogger(SchemasCheckTaskMysql.class);

	private Thread thread;
	@Autowired
	@Qualifier("jdbcTemplatePrimary")
	private JdbcTemplate jdbcTemplate;
	SchemasCheckTaskMysql() {
		this.thread = new Thread(this);
	}
	@Value("${checkSchemas_mysql}")
	private String checkSchemas;

	@Value("${checkSchemasMysql}")
	private boolean checkMysql;

	@Value("${checkSchemas_dir}")
	private String checkSchemasDir;
	
	@Override
	public void run() {
		if(!checkMysql){
			return;
		}
//		LOGGER.info("user.dir："+System.getProperty("user.dir"));
		String fileName=checkSchemasDir+"\\mysql.text";
		Map<String, Map<String, Object>> mapSchemaTableColumn=new HashMap<String, Map<String,Object>>();
		clearInfoForFile(fileName);
		try {
			String[] schemas=checkSchemas.split(";");
			String schema="";
			for (int i = 0; i < schemas.length; i++) {
				schema+="'"+schemas[i]+"'";
				if(i<schemas.length-1){
					schema+=",";
				}
			}
			//查所有库的表集合
			String sql_alltables="SELECT DISTINCT TABLE_NAME tbname FROM information_schema.TABLES where TABLE_SCHEMA in ("+schema+") ";
			LOGGER.info("sql_alltables:" + jdbcTemplate);
			List<Map<String, Object>>  listAllTables=jdbcTemplate.queryForList(sql_alltables);
			LOGGER.info("listAllTables begain:" + listAllTables.size());
			List<Map<String, Object>>  listNoTables = new ArrayList<Map<String,Object>>();
			String sql_notables;
			String sql_columns;
			/**
			 * 遍历所有库
			 * 		找到各自没有的表
			 * 		取所有表字段信息
			 */
			for (int i = 0; i < schemas.length; i++) {
				sql_notables = sql_alltables +
						" and TABLE_NAME not  in ("+
						"SELECT TABLE_NAME  FROM information_schema.TABLES where TABLE_SCHEMA = '"+schemas[i]+"'"+
						")";
				LOGGER.info("sql_notables:" + sql_notables);
				List<Map<String, Object>>  listNoTables_=jdbcTemplate.queryForList(sql_notables);
				LOGGER.info(schemas[i]+"--listNoTables:" + listNoTables_.size());
				listNoTables.addAll(listNoTables_);
				if(listNoTables_.size()>0){
					LOGGER.info(schemas[i]+"--少表:\n" + listNoTables_.stream().map(table -> table.get("tbname").toString()).collect(Collectors.joining("\n")));
					appendInfoToFile(fileName, schemas[i]+"--少表:\n" + listNoTables_.stream().map(table -> table.get("tbname").toString()).collect(Collectors.joining("\n")));

				}
				//封装表字段
				sql_columns="select LOWER(table_name) tbname ,column_name clnameor,LOWER(column_name) clname,column_type  cltype,LOWER(column_key) clkey,LOWER(EXTRA) clextra ,LOWER(IS_NULLABLE) clisnullable "+
							"from information_schema.columns "+ 
							"where  table_schema='"+schemas[i]+"' ORDER BY table_name";
				LOGGER.info("sql_columns:" + sql_columns);
				List<Map<String, Object>>  listColumns=jdbcTemplate.queryForList(sql_columns);
				LOGGER.info("listColumns:" + listColumns.size());
				for (int j = 0; j < listColumns.size(); j++) {
					Map<String, Object> mapColumn=listColumns.get(j);
					mapSchemaTableColumn.put(schemas[i]+"_"+mapColumn.get("tbname")+"_"+mapColumn.get("clname"), mapColumn);
				}
				LOGGER.info("mapSchemaTableColumn:" + mapSchemaTableColumn.size());
			}
			//都有的表，用来比对字段信息
			listAllTables.removeAll(listNoTables);
			LOGGER.info("listAllTables end:" + listAllTables.size());
			//所有交集表的字段集合
			String sql_allcolumns="select  DISTINCT table_name tbname ,LOWER( column_name) clname "+
									"from information_schema.columns  "+
									"where  table_schema in ("+schema+") and  table_name in  "+
									"(SELECT "+
									"	a.tbname "+
									"FROM "+
									"	( "+
									"		SELECT DISTINCT "+
									"			TABLE_NAME tbname, "+
									"			count(1) "+
									"		FROM "+
									"			information_schema. TABLES "+
									"		WHERE "+
									"			TABLE_SCHEMA IN ("+schema+") "+
									"		GROUP BY "+
									"			TABLE_NAME "+
									"		HAVING "+
									"			count(1) ="+schemas.length+
									"	) a) "+
									"ORDER BY table_name";
			List<Map<String, Object>>  listAllColumns=jdbcTemplate.queryForList(sql_allcolumns);
			LOGGER.info("listAllColumns:" + listAllColumns.size());
			String tbname,clname;
			Map<String, Object> mapColumnTypeI,mapColumnTypeK;
			for (int j = 0; j < listAllColumns.size(); j++) {
				tbname=listAllColumns.get(j).get("tbname").toString();
				clname=listAllColumns.get(j).get("clname").toString();
				for (int i = 0; i < schemas.length; i++) {
					mapColumnTypeI=mapSchemaTableColumn.get(schemas[i]+"_"+tbname+"_"+clname);
					if(mapColumnTypeI==null){
						LOGGER.info("库"+schemas[i]+"表"+tbname+"无字段" + clname);
						appendInfoToFile(fileName, "库"+schemas[i]+"表"+tbname+"无字段" + clname);
						continue;
					}
					for (int k = i+1; k < schemas.length; k++) {
						mapColumnTypeK=mapSchemaTableColumn.get(schemas[k]+"_"+tbname+"_"+clname);
						if(mapColumnTypeK!=null && !(mapColumnTypeI.get("clnameor").toString().equals(mapColumnTypeK.get("clnameor").toString())) && (mapColumnTypeI.get("clnameor").toString().equalsIgnoreCase(mapColumnTypeK.get("clnameor").toString()))){//字段大小写
							LOGGER.info("库"+schemas[i]+"表"+tbname+"字段" + mapColumnTypeI.get("clnameor").toString()+"和库"+schemas[k]+"表"+tbname+"字段" +mapColumnTypeK.get("clnameor").toString()+"大小写不一致");
							appendInfoToFile(fileName,"库"+schemas[i]+"表"+tbname+"字段" + mapColumnTypeI.get("clnameor").toString()+"和库"+schemas[k]+"表"+tbname+"字段" +mapColumnTypeK.get("clnameor").toString()+"大小写不一致");
						}
						if(mapColumnTypeK!=null && !(mapColumnTypeI.get("cltype").toString().equalsIgnoreCase(mapColumnTypeK.get("cltype").toString()))){
							LOGGER.info("库"+schemas[i]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeI.get("cltype").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeK.get("cltype").toString()+"不一致");
							appendInfoToFile(fileName,"库"+schemas[i]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeI.get("cltype").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeK.get("cltype").toString()+"不一致");
						}
						if(mapColumnTypeK!=null && !(mapColumnTypeI.get("clkey").toString().equalsIgnoreCase(mapColumnTypeK.get("clkey").toString()))){
							LOGGER.info("库"+schemas[i]+"表"+tbname+"字段" + clname+"索引类型（主键相关）"+mapColumnTypeI.get("clkey").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"索引类型（主键相关）"+mapColumnTypeK.get("clkey").toString()+"不一致");
							appendInfoToFile(fileName,"库"+schemas[i]+"表"+tbname+"字段" + clname+"索引类型（主键相关）"+mapColumnTypeI.get("clkey").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"索引类型（主键相关）"+mapColumnTypeK.get("clkey").toString()+"不一致");
						}
						if(mapColumnTypeK!=null && !(mapColumnTypeI.get("clextra").toString().equalsIgnoreCase(mapColumnTypeK.get("clextra").toString()))){
							LOGGER.info("库"+schemas[i]+"表"+tbname+"字段" + clname+"其他信息（自增等相关）"+mapColumnTypeI.get("clextra").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"其他信息（自增等相关）"+mapColumnTypeK.get("clextra").toString()+"不一致");
							appendInfoToFile(fileName,"库"+schemas[i]+"表"+tbname+"字段" + clname+"其他信息（自增等相关）"+mapColumnTypeI.get("clextra").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"其他信息（自增等相关）"+mapColumnTypeK.get("clextra").toString()+"不一致");
						}
					}
				}
			}
		} catch (Exception e) {
				e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void run(String... arg0) throws Exception {
		this.thread.start();
	}
	 public static void appendInfoToFile(String fileName, String info) {
	    File file =new File(fileName);
	    try {
	        if(!file.exists()){
	            file.createNewFile();
	        }
	        FileWriter fileWriter =new FileWriter(file, true);
	        info =info +System.getProperty("line.separator");
	        fileWriter.write(info);
	        fileWriter.flush();
	        fileWriter.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	 public static void clearInfoForFile(String fileName) {
	    File file =new File(fileName);
	    try {
	        if(!file.exists()) {
	            file.createNewFile();
	        }
	        FileWriter fileWriter =new FileWriter(file);
	        fileWriter.write("");
	        fileWriter.flush();
	        fileWriter.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	 public static void main(String[] args) {
		 LOGGER.info(System.getProperty("user.dir"));
		 File file=new File("");//新建File类
        try {
            System.out.println(file.getAbsolutePath());//获取绝对路径
            System.out.println(file.getCanonicalPath());//获取标准路径
        }
        catch (Exception e) {
            System.out.println("error");
        }
        System.out.println(Thread.currentThread().getContextClassLoader().getResource("./").getPath());
        System.out.println(Thread.currentThread().getContextClassLoader().getResource("").getPath());//ClassPath的绝对URI路径
        System.out.println(Thread.currentThread().getContextClassLoader().getResource(".").getPath());//项目的绝对路径
	}
}
