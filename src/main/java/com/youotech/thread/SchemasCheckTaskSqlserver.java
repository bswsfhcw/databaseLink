//package com.youotech.thread;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
///**
// * 
// * @author bswsfhcw
// *
// */
//@Component
//@Order(1)
//public class SchemasCheckTaskSqlserver implements DisposableBean, Runnable,CommandLineRunner  {
//	protected static Logger LOGGER = LoggerFactory.getLogger(SchemasCheckTaskSqlserver.class);
//
//	private Thread thread;
//	
//	@Autowired
//	@Qualifier("jdbcTemplateSecondary")
//	private JdbcTemplate jdbcTemplate;
//	SchemasCheckTaskSqlserver() {
//		this.thread = new Thread(this);
//	}
//	@Value("${checkSchemas_sqlserver}")
//	private String checkSchemas;
//
//	@Value("${checkSchemasSqlserver}")
//	private boolean checkSqlserver;
//
//	@Value("${checkSchemas_dir}")
//	private String checkSchemasDir;
//	
//	@Override
//	public void run() {
//		if(!checkSqlserver){
//			return;
//		}
////		LOGGER.info("user.dir："+System.getProperty("user.dir"));
//		String fileName=checkSchemasDir+"\\sqlserver.text";
//		Map<String, Map<String, Object>> mapSchemaTableColumn=new HashMap<String, Map<String,Object>>();
//		Map<String, Set<String>> mapSchemaTableColumnSet=new HashMap<String, Set<String>>();//每个库+表的字段和全量字段
//
//		
//		Map<String, Set<String>> mapSchemaTable=new HashMap<String,  Set<String>>();//存每个库的表名
//		Set<String> setAllTables = new HashSet<>();
//		Set<String> listCommonTables = new HashSet<>();
//		clearInfoForFile(fileName);
//		try {
//			String[] schemas=checkSchemas.split(";");
//			String schema="";
//			String tbname="";
//			String sql_alltables="SELECT LOWER(name) tbname FROM SYSOBJECTS WHERE TYPE = 'U'";
//			String sql_columns="SELECT "+
//								"LOWER (o.name) tbname,"+
//								"LOWER (c.name) clnameor,"+
//								"c.name clname,"+
//								"CONCAT(tp.name,'(',c.length,')') cltype "+
//								"FROM "+
//								"SYSOBJECTS o,"+
//								"syscolumns c,"+
//								"sysTypes tp "+
//								"WHERE "+
//								"o.id = c.id "+
//								"AND c.xtype = tp.xtype "+
//								"ORDER BY o.name,c.name";
//			List<Map<String, Object>>  setAllTablesTemp;
//			Set<String> tableSet;
//			for (int i = 0; i < schemas.length; i++) {
//				schema=schemas[i];
//				jdbcTemplate.execute("USE "+schema);//切换库
//				setAllTablesTemp=jdbcTemplate.queryForList(sql_alltables);//查所有表
//				LOGGER.info("setAllTablesTemp:" + setAllTablesTemp.size());
//				for (int j = 0; j < setAllTablesTemp.size(); j++) {
//					tbname= setAllTablesTemp.get(j).get("tbname").toString();
//					tableSet = mapSchemaTable.get(schema);
//					if(tableSet == null || tableSet.size()==0){
//						tableSet = new HashSet<>();
//					}
//					tableSet.add(tbname);
//					mapSchemaTable.put(schema, tableSet);
//					setAllTables.add(tbname);
//					listCommonTables.add(tbname);
//				}
//				//封装表字段
//				List<Map<String, Object>>  listColumns=jdbcTemplate.queryForList(sql_columns);
//				LOGGER.info("listColumns:" + listColumns.size());
//				for (int j = 0; j < listColumns.size(); j++) {
//					Map<String, Object> mapColumn=listColumns.get(j);
//					mapSchemaTableColumn.put(schemas[i]+"_"+mapColumn.get("tbname")+"_"+mapColumn.get("clname"), mapColumn);
//					Set<String> schemaTableColumnSet=mapSchemaTableColumnSet.get(schemas[i]+"_"+mapColumn.get("tbname"));
//					if(schemaTableColumnSet==null){
//						schemaTableColumnSet=new HashSet<>();
//					}
//					schemaTableColumnSet.add(mapColumn.get("tbname").toString());
//					mapSchemaTableColumnSet.put(schemas[i]+"_"+mapColumn.get("tbname"), schemaTableColumnSet);
//					//
//					Set<String> tableColumnSet=mapSchemaTableColumnSet.get("ALL_"+mapColumn.get("tbname"));
//					if(tableColumnSet==null){
//						tableColumnSet=new HashSet<>();
//					}
//					tableColumnSet.add(mapColumn.get("clname").toString());
//					mapSchemaTableColumnSet.put("ALL_"+mapColumn.get("tbname"), tableColumnSet);
//				}
//				LOGGER.info("mapSchemaTableColumn:" + mapSchemaTableColumn.size());
//			}
//			LOGGER.info("setAllTables:" + setAllTables.size());
//			/**
//			 * 遍历所有库
//			 * 		找到各自没有的表
//			 * 		取所有表字段信息
//			 */
//			Set<String> tableSetAllTemple;
//			Set<String> setNotables=new HashSet<>();//少的表合集
//			for (int i = 0; i < schemas.length; i++) {
//				schema=schemas[i];
//				tableSet = mapSchemaTable.get(schema);
//				tableSetAllTemple = new HashSet<>();
//				tableSetAllTemple.addAll(setAllTables);//
//				tableSetAllTemple.removeAll(tableSet);//所有的移除当前 则为少的表
//				setNotables.addAll(tableSetAllTemple);//
//				LOGGER.info(schemas[i]+"--setNotables:" + setNotables.size());
//			
//				if(tableSetAllTemple.size()>0){
//					String mergedString=tableSetAllTemple.stream().filter(string -> !string.isEmpty()).collect(Collectors.joining("\n "));
//					LOGGER.info(schemas[i]+"--少表:\n" + mergedString);
//					appendInfoToFile(fileName, schemas[i]+"--少表:\n" + mergedString);
//
//				}
//			}
//			//都有的表，用来比对字段信息
//			setAllTables.removeAll(setNotables);
////			LOGGER.info("setAllTables end:" + setAllTables.size());
//			//所有交集表的字段集合
//			List<Map<String, Object>>  listAllColumns=new ArrayList<>();
//			for (String tb:setAllTables) {
//				tbname=tb;
//				Set<String> tableColums =mapSchemaTableColumnSet.get("ALL_"+tbname);
////				LOGGER.info(tbname+"_tableColums:" + tableColums.size());
//				for (String clname:tableColums) {
//					Map<String, Object> maptableCulmn=new HashMap<>();
//					maptableCulmn.put("tbname", tbname);
//					maptableCulmn.put("clname", clname);
//					listAllColumns.add(maptableCulmn);
//				}
//			}
//			LOGGER.info("listAllColumns:" + listAllColumns.size());
//			String clname;
//			Map<String, Object> mapColumnTypeI,mapColumnTypeK;
//			for (int j = 0; j < listAllColumns.size(); j++) {
//				tbname=listAllColumns.get(j).get("tbname").toString();
//				clname=listAllColumns.get(j).get("clname").toString();
//				for (int i = 0; i < schemas.length; i++) {
//					mapColumnTypeI=mapSchemaTableColumn.get(schemas[i]+"_"+tbname+"_"+clname);
//					if(mapColumnTypeI==null){
//						LOGGER.info("库"+schemas[i]+"表"+tbname+"无字段" + clname);
//						appendInfoToFile(fileName, "库"+schemas[i]+"表"+tbname+"无字段" + clname);
//						continue;
//					}
//					for (int k = i+1; k < schemas.length; k++) {
//						mapColumnTypeK=mapSchemaTableColumn.get(schemas[k]+"_"+tbname+"_"+clname);
//						if(mapColumnTypeK!=null && !(mapColumnTypeI.get("clnameor").toString().equals(mapColumnTypeK.get("clnameor").toString())) && (mapColumnTypeI.get("clnameor").toString().equalsIgnoreCase(mapColumnTypeK.get("clnameor").toString()))){//字段大小写
//							LOGGER.info("库"+schemas[i]+"表"+tbname+"字段" + mapColumnTypeI.get("clnameor").toString()+"和库"+schemas[k]+"表"+tbname+"字段" +mapColumnTypeK.get("clnameor").toString()+"大小写不一致");
//							appendInfoToFile(fileName,"库"+schemas[i]+"表"+tbname+"字段" + mapColumnTypeI.get("clnameor").toString()+"和库"+schemas[k]+"表"+tbname+"字段" +mapColumnTypeK.get("clnameor").toString()+"大小写不一致");
//						}
//						if(mapColumnTypeK!=null && !(mapColumnTypeI.get("cltype").toString().equalsIgnoreCase(mapColumnTypeK.get("cltype").toString()))){
//							LOGGER.info("库"+schemas[i]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeI.get("cltype").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeK.get("cltype").toString()+"不一致");
//							appendInfoToFile(fileName,"库"+schemas[i]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeI.get("cltype").toString()+"和库"+schemas[k]+"表"+tbname+"字段" + clname+"字段类型"+mapColumnTypeK.get("cltype").toString()+"不一致");
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//				e.printStackTrace();
//		}
//	}
//
//	@Override
//	public void destroy() {
//	}
//
//	@Override
//	public void run(String... arg0) throws Exception {
//		this.thread.start();
//	}
//	 public static void appendInfoToFile(String fileName, String info) {
//	    File file =new File(fileName);
//	    try {
//	        if(!file.exists()){
//	            file.createNewFile();
//	        }
//	        FileWriter fileWriter =new FileWriter(file, true);
//	        info =info +System.getProperty("line.separator");
//	        fileWriter.write(info);
//	        fileWriter.flush();
//	        fileWriter.close();
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
//	 public static void clearInfoForFile(String fileName) {
//	    File file =new File(fileName);
//	    try {
//	        if(!file.exists()) {
//	            file.createNewFile();
//	        }
//	        FileWriter fileWriter =new FileWriter(file);
//	        fileWriter.write("");
//	        fileWriter.flush();
//	        fileWriter.close();
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
//	 public static void main(String[] args) {
//		 LOGGER.info(System.getProperty("user.dir"));
//		 File file=new File("");//新建File类
//        try {
//            System.out.println(file.getAbsolutePath());//获取绝对路径
//            System.out.println(file.getCanonicalPath());//获取标准路径
//        }
//        catch (Exception e) {
//            System.out.println("error");
//        }
//        System.out.println(Thread.currentThread().getContextClassLoader().getResource("./").getPath());
//        System.out.println(Thread.currentThread().getContextClassLoader().getResource("").getPath());//ClassPath的绝对URI路径
//        System.out.println(Thread.currentThread().getContextClassLoader().getResource(".").getPath());//项目的绝对路径
//	}
//}
