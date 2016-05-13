package test.test;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.dianru.analysis.bean.Cheat;
import com.dianru.analysis.bean.MediaFilter;
import com.dianru.analysis.util.ProcessUtil;

public class TestTest {

	private static Logger LOG = LogManager.getLogger(TestTest.class);
	private Map<String,MediaFilter> map = new ConcurrentHashMap<String, MediaFilter>();
	@Test
	public void testoperator() {
		int invalid = 0;
		invalid |= 1;
		LOG.debug("invalid:" + invalid);
		invalid |= 2;
		LOG.debug("invalid:" + invalid);
		invalid |= 3;
		LOG.debug("invalid:" + invalid);
	}

	@Test
	public void testMap() {
		Map<String, Integer> testMap = Cheat.parseMediaOptions("");
		int test = testMap.get(Cheat.KEY_DISK);
		LOG.debug("test:" + test);
	}

	@Test
	public void testRandomInt() {
		LOG.debug("randomint:" + new Random().nextInt(100));
	}

	@Parameters({ "first-name" })
	@Test
	public void testSingleString(@Optional("Cedric") String firstName) {
		LOG.debug("Invoked testString " + firstName);
		assert "Cedric".equals(firstName) : "the firstname error";
		LOG.error("assert is success!");
	}
	
	@Parameters({ "datasource", "jdbcDriver" })
	@Test
	public void testTwoString(String ds, String driver) {
	  LOG.debug("datasource:" + ds + "| jdbcDriver:" + driver  );
	}
	
	@BeforeMethod
	public void testBeforeMethod(){
		LOG.debug("You're getting better and better!");
	}
	
	@Test
	public void testGetCurrentProcessId(){
		LOG.debug(ProcessUtil.getCurrentProcessId());
	}
	@Parameters({"rate"})
	@Test
	public void testMapModify(@Optional("1") String rate){
		MediaFilter wl = new MediaFilter(10, 20);
		map.put("1", wl);
		MediaFilter kl = map.values().iterator().next();
		kl.setRate(Integer.parseInt(rate));
		assert 1==map.get("1").getRate();
	}
}
