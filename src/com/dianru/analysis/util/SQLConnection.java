package com.dianru.analysis.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 获取数据库连接，关闭连接
 * 
 * @author zhangtao
 * @date 2014-8-19
 */
public class SQLConnection {
	public static Logger LOG = LogManager.getLogger(SQLConnection.class);
	
	/**
	 * 分页实体
	 * 
	 * @author zhangtao
	 * @date 2014-8-19
	 */
	public static class PagedList {
		public long count;
		public long size;
		public long page;
		public List<?> items;

		public PagedList(long count, long size, long page, List<?> items) {
			this.count = count;
			this.size = size;
			this.page = page;
			this.items = items;
		}
	}

	public static class DataSource extends BasicDataSource {
		protected String prefix;
		
		public DataSource(Configuration conf, String name) {
			String driver = conf.getString("db." + name + ".driver","com.mysql.jdbc.Driver");
			String uri = conf.getString("db." + name + ".uri","");
			String username = conf.getString("db." + name + ".username","");
			String password = conf.getString("db." + name + ".password","");
			this.prefix = conf.getProperty("db." + name + ".prefix","");
			
			this.setDriverClassName(driver);
			this.setUrl(uri);
			this.setUsername(username);
			this.setPassword(password);
			
			this.setInitialSize(2); // 初始的连接数；  
			this.setMaxActive(1000);
			this.setMaxIdle(60);
			this.setMaxWait(60000);
			this.setTestOnBorrow(true);
			this.setTestWhileIdle(true);
			this.setValidationQuery("SELECT 1");
		}
		
		public String getPrefix() {
			return this.prefix;
		}
	}

	public static Map<String, DataSource> MAP = new HashMap<String, DataSource>();

	/**
	 * 初始化数据库配置文件
	 */
	static {
		Configuration conf = Configuration.getInstance();
		String dbnamesString = conf.getProperty("db.names", "main");
			
		String dbnames[] = dbnamesString.split(",");
		for (String name : dbnames) {
			name = name.trim();
			DataSource dbs = new DataSource(conf, name);
			MAP.put(name, dbs);
		}
	}

	private DataSource source = null;
	private Connection connection = null;
	
	public static DataSource getDataSource(String dbname) {
		return MAP.get(dbname);
	}

	/**
	 * 获取SQLConnection
	 * 
	 * @param databaseName
	 * @return
	 */
	public static SQLConnection getInstance(String dbname) {
		DataSource ds = MAP.get(dbname);
		if (ds == null)
			return null;

		return new SQLConnection(ds);
	}

	public SQLConnection(DataSource source) {
		this.source = source;
		this.open();
	}
	
	public Connection getConnection(){
		return connection;
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	public boolean open() {
		try {
			for(int i=0;i<5;i++) {
				this.connection = source.getConnection();
				if(this.connection != null) break;
				else {
					LOG.warn("connection open method retry : " + i);
					Thread.sleep(1000);
				}
			}
			if(this.connection == null) {
				LOG.error("sql connection error");
			}
			return true;
		} catch (Exception e) {
			this.connection = null;
			LOG.error("connection open method fail."+source.toString()+":"+e.toString());
			return false;
		}
	}

	/**
	 * 关闭数据库连接资源
	 * 
	 * @param connection
	 * @param ps
	 * @param rs
	 */
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
			connection = null;
		}
	}
	
	public static void shutdown() {
		for(Iterator<DataSource> it = MAP.values().iterator();it.hasNext();) {
			DataSource ds = it.next();
			try {
				ds.close();
			} catch (SQLException e) {
				LOG.error("data source close : " + e.toString());
			}
		}
	}
	
	public boolean begin () {
		if(connection == null) return false;
		
		try {
			connection.setAutoCommit(false);
			return true;
		} catch (SQLException e) {
			LOG.error("begin fail."+e.toString());
			return false;
		}
	}
	
	public boolean commit () {
		if(connection == null) return false;
		
		boolean result = true;
		try {
			connection.commit();
		} catch (SQLException e) {
			LOG.error("commit fail."+e.toString());
			result = false;
		}

		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			result = false;
		}
		return result;
	}
	
	public boolean rollback () {
		if(connection == null) return false;
		
		boolean result = true;
		try {
			connection.rollback();
		} catch (SQLException e) {
			LOG.error("rollback fail."+e.toString());
			result = false;
		}

		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			result = false;
		}
		return result;
	}
	
	public long getLastId() {
		Object obj = this.queryOne("SELECT LAST_INSERT_ID()",null);
		if(obj == null) {
			LOG.error("SELECT LAST_INSERT_ID fail.");
			return -1;
		}
		LOG.trace("SELECT LAST_INSERT_ID obj = " + obj);
		return (long)obj;
	}

	public Object queryOne(String sql, Object[] args) {
		List<Object> list = this.queryOneList(sql, args);
		if(list == null || list.isEmpty()) return null;
		
		return list.get(0);
	}

	/**
	 * 查询
	 * 
	 * @param sql
	 *            查询语句
	 * @param args
	 *            传参，替换sql中的?
	 * @return 符合条件的结果集
	 * @throws SQLException
	 */
	public List<Map<String,Object>> queryMap(String sql, Object[] args) {
		if(connection == null) return null;
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(rsmd.getColumnLabel(i), getResultObject(rs, i));
				}
				list.add(map);
			}
		} catch (Exception e) {
			LOG.error("queryMap " + sql + " fail."+e.toString());
		} finally {

			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return list;
	}
	
	/**
	 * 查询
	 * 
	 * @param sql
	 *            查询语句
	 * @param args
	 *            传参，替换sql中的?
	 * @return 符合条件的结果集
	 * @throws SQLException
	 */
	public List<List<Object>> queryList(String sql, Object... args) {
		if(connection == null) return null;
		
		List<List<Object>> list = new ArrayList<List<Object>>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			rs = ps.executeQuery();

			while (rs.next()) {
				List<Object> vals = new ArrayList<Object>();
				int count = rs.getMetaData().getColumnCount();
				for (int i=1;i<=count;i++) {
					Object val = getResultObject(rs, i);
					vals.add(val);
				}
				list.add(vals);
			}
		} catch (Exception e) {
			LOG.error("queryList " + sql + " fail."+e.toString());
		} finally {

			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return list;
	}

	/**
	 * 单个查询
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return
	 */
	public Map<String,Object> queryOneMap(String sql, Object[] args) {
		if(connection == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			if (rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(rsmd.getColumnName(i), getResultObject(rs, i));
				}
				return map;
			}
		} catch (Exception e) {
			LOG.error("queryOneMap " + sql + " fail."+e.toString());
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return null;
	}
	
	public Object getResultObject(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		if(obj == null) return obj;
		if(obj instanceof java.math.BigInteger) {
			return ((java.math.BigInteger) obj).longValue();
		} else if(obj instanceof java.math.BigDecimal) {
			return ((java.math.BigDecimal) obj).floatValue();
		}
		return obj;
	}
	
	/**
	 * 单个查询
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return
	 */
	public List<Object> queryOneList(String sql, Object[] args) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			if (rs.next()) {
				List<Object> vals = new ArrayList<Object>();
				for (int i = 1; i <= columnCount; i++) {
					vals.add(getResultObject(rs, i));
				}
				return vals;
			}
		} catch (Exception e) {
			LOG.error("queryOneList " + sql + " fail."+e.toString());
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return null;
	}

	/**
	 * 分页查询
	 * 
	 * @param sql
	 * @return
	 */
	public PagedList queryPagedList(String sql, PagedList page,
			Object[] args) {
		
		if(connection == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if (page != null) {
			page.count = count(sql, args);
			long start = ((page.size - 1) * page.page + 1);
			long end = page.size * page.page;
			sql = sql + " limit " + start + "," + end;
		}
		try {
			Map<String,Object> map = null;
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				map = new HashMap<String,Object>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(rsmd.getColumnName(i), getResultObject(rs, i));
				}
				list.add(map);
			}
			page.items = list;
			return page;
		} catch (SQLException e) {
			LOG.error("queryPagedList " + sql + " fail."+e.toString());
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}

		return null;
	}

	/**
	 * 查询符合条件的记录数
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return 符合条件的记录数
	 */
	public long count(String sql, Object[] args) {
		if(connection == null) return -1;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				rs.last();
				return rs.getRow();
			}
		} catch (SQLException e) {
			LOG.error("count " + sql + " fail."+e.toString());
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return 0L;
	}
	
	/**
	 * 执行增删改
	 * 
	 * @param sql
	 * @return boolean
	 */
	public boolean execute(String sql) {
		if(connection == null) return false;
		
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			boolean result = ps.execute();
			return result;
		} catch (SQLException e) {
			LOG.error("execute " + sql + " fail."+e.toString());
		} finally {
			try {
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return false;
	}

	/**
	 * 执行增删改
	 * 
	 * @param sql
	 * @param args
	 * @return boolean
	 */
	public int execute(String sql, Object[] args) {
		if(connection == null) {
			LOG.error("execute " + sql + " get connection faild.");
			return -1;
		}
		
		PreparedStatement ps = null;
		try {
			String str = "";
			ps = connection.prepareStatement(sql);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
					str = str +","+args[i];
				}
			}
			int rows = ps.executeUpdate();
			LOG.trace(sql + " "+str + " rows " + rows);
			return rows;
		} catch (SQLException e) {
			LOG.error("execute " + sql + " fail."+e.toString());
		} finally {
			try {
				if (ps != null) ps.close();
			} catch (SQLException e) {
			}
		}
		return 0;
	}

	/**
	 * 插入
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return
	 */
	public int insert(String sql, Object[] args) {
		return execute(sql, args);
	}
	
	/**
	 * 插入
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return
	 */
	public long insertAndGetLastId(String sql, Object[] args) {
		int result = execute(sql, args);
		if(result > 0) return this.getLastId();
		return -1;
	}

	/**
	 * 批量插入数据
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public int[] batch(String sql, List<Object[]> args) {
		if(connection == null) return null;
		
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			//LOG.trace("sql:"+sql);
			for (Object[] params : args) {
				ps.clearParameters();
				String str = "";
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i + 1, params[i]);
					str = str +","+params[i];
				}
				//LOG.trace("values:"+str.substring(1));
				ps.addBatch();
			}
			return ps.executeBatch();
		} catch (SQLException e) {
			LOG.error("batch " + sql + " fail."+e.toString());
			return null;
		} finally {
			try {
				ps.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * 批量插入数据
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public int[] batch(List<String> sqls) {
		if(connection == null) return null;
		
		if(sqls.size() == 0) return null;
		
		PreparedStatement ps = null;
		try {
			Statement s = connection.createStatement();
			for (int i=0;i<sqls.size();i++) {
				s.addBatch(sqls.get(i));
			}
			int[] results = s.executeBatch();
			return results;
		} catch (SQLException e) {
			
			LOG.error("batch " + (sqls == null || sqls.isEmpty() ? "" : sqls.get(0)) + " fail."+e.toString());
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if(ps != null) ps.close();
			} catch (SQLException e1) {
				LOG.error("ps close fail."+e1.getMessage());
			}
		}

		return null;
	}

	/**
	 * 删除
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return
	 */
	public int delete(String sql, Object[] args) {
		return execute(sql, args);
	}

	/**
	 * 更新
	 * 
	 * @param sql
	 *            要执行的sql语句
	 * @param args
	 *            给sql语句中的？赋值的参数列表
	 * @return
	 */
	public int update(String sql, Object[] args) {
		return execute(sql, args);
	}
}