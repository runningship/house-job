package com.youwei.zjb.house.spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public enum RentType {
	整租(1),
	合租(2);
	private int code;
	
	private RentType( int code){
		this.code = code;
	}

	public int getCode() {
		return code;
	}
	
	public static JSONArray toJsonArray(){
		JSONArray arr = new JSONArray();
		for(RentType state : RentType.values()){
			JSONObject jobj = new JSONObject();
			jobj.put("value", state.name());
			jobj.put("name", state.name());
			jobj.put("code", state.code);
			arr.add(jobj);
		}
		return arr;
	}
	
	public static RentType parse(int codec){
		for(RentType zx : RentType.values()){
			if(zx.code==codec){
				return zx;
			}
		}
		return null;
	}
	
	public static List<Map<String,Object>> toList(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(RentType state : RentType.values()){
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("name", state.name());
			map.put("code", state.code);
			list.add(map);
		}
		return list;
	}
}
