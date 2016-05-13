package com.dianru.analysis.parse;

import java.util.List;
import com.dianru.analysis.bean.Define;

public interface Parser extends Define.Index {
	public List<Object> parse(String line);
}
