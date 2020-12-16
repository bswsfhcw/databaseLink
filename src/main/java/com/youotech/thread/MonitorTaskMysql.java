package com.youotech.thread;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
 * 监控进程
 * 保留
 * @author bswsfhcw
 *
 */
@Component
@Order(1)
public class MonitorTaskMysql implements DisposableBean, Runnable,CommandLineRunner  {
	protected static Logger LOGGER = LoggerFactory.getLogger(MonitorTaskMysql.class);

	private Thread thread;
	@Autowired
	@Qualifier("jdbcTemplatePrimary")
	private JdbcTemplate jdbcTemplate;
	MonitorTaskMysql() {
		this.thread = new Thread(this);
	}
	@Value("${checkSql_mysql}")
	private String checkSql;
	@Value("${checkMysql}")
	private boolean checkMysql;
	@Override
	public void run() {
		while (checkMysql) {
			try {
				String sql = (checkSql==null||"".equalsIgnoreCase(checkSql.trim())?"select now()":checkSql);
				 List<Map<String, Object>>  list=jdbcTemplate.queryForList(sql);
				LOGGER.info("Monitor:" + new Date()+",111:"+(list.size()==0?"0":list.get(0).toString()));
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void destroy() {
		checkMysql = false;
	}

	@Override
	public void run(String... arg0) throws Exception {
		this.thread.start();
	}
}
