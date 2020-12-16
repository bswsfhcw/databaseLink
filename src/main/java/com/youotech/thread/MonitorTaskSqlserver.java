//package com.youotech.thread;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
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
// * 监控进程
// * 保留
// * @author bswsfhcw
// *
// */
//@Component
//@Order(1)
//public class MonitorTaskSqlserver implements DisposableBean, Runnable,CommandLineRunner  {
//	protected static Logger LOGGER = LoggerFactory.getLogger(MonitorTaskSqlserver.class);
//
//	private Thread thread;
//	
//	@Autowired
//	@Qualifier("jdbcTemplateSecondary")
//	private JdbcTemplate jdbcTemplate;
//	MonitorTaskSqlserver() {
//		this.thread = new Thread(this);
//	}
//	@Value("${checkSql_sqlserver}")
//	private String checkSql;
//	@Value("${checkSqlserver}")
//	private boolean checkSqlserver;
//	@Override
//	public void run() {
//		while (checkSqlserver) {
//			try {
//				String sql = (checkSql==null||"".equalsIgnoreCase(checkSql.trim())?"select GETDATE()":checkSql);
//				 List<Map<String, Object>>  list=jdbcTemplate.queryForList(sql);
//				LOGGER.info("Monitor:" + new Date()+",111:"+(list.size()==0?"0":list.get(0).toString()));
//				Thread.sleep(5000L);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
//	}
//
//	@Override
//	public void destroy() {
//		checkSqlserver = false;
//	}
//
//	@Override
//	public void run(String... arg0) throws Exception {
//		this.thread.start();
//	}
//}
