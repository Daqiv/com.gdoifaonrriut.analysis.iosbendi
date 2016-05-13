package com.dianru.analysis.store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.CallbackItem;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.util.SQLConnection;

public class DBCallbackStore {
	
	public static Logger LOG = LogManager.getLogger(DBCallbackStore.class);
	
	public static int VALUES_COUNT = 14;
	
	private static String SQL_INSERT = "INSERT INTO `%s_callback_%s` ("+CallbackItem.SQL_KEYS+") VALUES ("+CallbackItem.SQL_VALUES+")";
	private static String SQL_UPDATE = "UPDATE `%s_callback_%s` SET action=?,create_time=?,invalid=?,income=income+?,cost=cost+?,score=score+?,process=?,keywords=? WHERE id=?";
	private static String SQL_SELECT = "SELECT `id`,"+CallbackItem.SQL_KEYS+" FROM `%s_callback_%s` WHERE id=?";

	private String dataSource;
	private String prefix;
	private int date;

	public DBCallbackStore(String ds, int date){
		this.dataSource = ds;
		this.date = date;
		this.prefix = SQLConnection.getDataSource(dataSource).getPrefix();
	}
	
	private final static Map<Integer,DBCallbackStore> STORES = new ConcurrentHashMap<Integer,DBCallbackStore>();
	
	public static DBCallbackStore getInstance(int type, int date) {
		DBCallbackStore store = STORES.get(type);
		if(store == null) {
			String ds = Define.getDataSourceName(type);
			store = new DBCallbackStore(ds, date);
		}
		
		return store;
	}
	
	/**callback明细表添加数据
	 * @author zhangtao
	 * @date 2014年10月14日
	 * @param vals
	 * @return
	 */
	public long add(CallbackItem item) {
		
		SQLConnection conn = SQLConnection.getInstance(dataSource);
		//get sql
		String sql = String.format(SQL_INSERT, prefix, date);
		
		long id = conn.insertAndGetLastId(sql, item.toObjects());
		conn.close();
		return id;
	}
	
	/**callback明细表更新数据
	 * @author zhangtao
	 * @date 2014年10月14日
	 * @param id
	 * @param status
	 * @param time 如20141012
	 * @return
	 */
	public int update(CallbackItem item) {
		
		SQLConnection conn = SQLConnection.getInstance(dataSource);
		//get sql
		String sql = String.format(SQL_UPDATE, prefix,date);
		//LOG.debug(sql);
		
		int count =conn.update(sql, new Object[]{item.action, item.create_time, item.invalid, item.income, item.cost, item.score,item.process,item.keywords,item.id});
		conn.close();
		return count;
	}
	
	public CallbackItem get(long id) {
		
		SQLConnection conn = SQLConnection.getInstance(dataSource);
		//get sql
		String sql = String.format(SQL_SELECT, prefix, date);
		List<Object> vals = conn.queryOneList(sql, new Object[]{id});
		conn.close();
		if(vals == null || vals.isEmpty()) return null;
		
		CallbackItem item = CallbackItem.parseFromData(vals);
		if(item != null) item.date = date;
		
		return item;
	}
	
	public static void main(String[] args) {
		
	}
}
