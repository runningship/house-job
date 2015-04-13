package com.youwei.zjb.house.spider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TaskHelper {

	private static Map<String,Integer> nums = new HashMap<String , Integer>();
	static{
		nums.put("一", 1);
		nums.put("二", 2);
		nums.put("三", 3);
		nums.put("四", 4);
		nums.put("五", 5);
		nums.put("六", 6);
		nums.put("七", 7);
		nums.put("八", 8);
		nums.put("九", 9);
	}
	public static Integer getHxtFromText(String text){
		text=text.replace(" ", "");
		int ting = text.indexOf("厅");
		try{
			if(ting>0){
				String str = String.valueOf(text.charAt(ting-1));
				if(nums.containsKey(str)){
					return nums.get(str);
				}
				return Integer.valueOf(str);
			}
		}catch(Exception ex){
			//暂不处理
		}
		
		return 0;
	}
	
	public static Integer getHxsFromText(String text){
		text=text.replace(" ", "");
		int ting = text.indexOf("室");
		try{
			if(ting>0){
				String str = String.valueOf(text.charAt(ting-1));
				if(nums.containsKey(str)){
					return nums.get(str);
				}
				return Integer.valueOf(str);
			}
		}catch(Exception ex){
			//暂不处理
		}
		
		return 0;
	}
	
	public static Integer getHxwFromText(String text){
		text=text.replace(" ", "");
		int ting = text.indexOf("卫");
		try{
			if(ting>0){
				String str = String.valueOf(text.charAt(ting-1));
				if(nums.containsKey(str)){
					return nums.get(str);
				}
				return Integer.valueOf(str);
			}
		}catch(Exception ex){
			//暂不处理
		}
		
		return 0;
	}
	
	public static int getLcengFromText(String text){
		text = text.replace("楼层：", "").replace("楼", "").replace("层", "").trim().replace(" ", "/");
		if(StringUtils.isEmpty(text)){
			return 0;
		}
		try{
			String tmp = text.split("/")[0];
			return Integer.valueOf(tmp);
		}catch(Exception ex){
			//暂不处理
			return 0;
		}
	}
	public static int getZcengFromText(String text){
		text = text.replace("楼", "").replace("共", "").replace("层", "").trim().replace(" ", "/");
		try{
			String[] lcen = text.split("/");
			int i = lcen.length-1;
			if(i==0){
				return 0;
			}
			String tmp = lcen[i];
			return Integer.valueOf(tmp);
		}catch(Exception ex){
			//暂不处理
			return 0;
		}
	}
	
	public static int getYearFromText(String text){
		String tmp = "";
		if(text.contains("年")){
			tmp =  text.replace("年", "").trim();
		}else{
			tmp = text;
		}
		if("暂无".equals(tmp)){
			return 0;
		}
		try{
			return Integer.valueOf(tmp);
		}catch(Exception ex){
			return 0;
		}
	}
	
	public static Float getMjiFromText(String text){
		if(StringUtils.isEmpty(text)){
			return 0f;
		}
		String tmp = "";
		text = text.replace(" ㎡", "㎡");
		text  = text.replace("　", " ").replace("面积：", "").replace("平米", "㎡");
		text = text.split("（")[0];
		for(String str : text.split(" ")){
			if(str.contains("㎡") && !str.contains("（")){
				tmp =  str.replace("㎡", "").trim();
				break;
			}
		}
		try{
			return Float.valueOf(tmp);
		}catch(Exception ex){
			return 0f;
		}
	}
	
	public static String getTelFromText(String text){
		if(text.startsWith("<img")){
			return Jsoup.parse(text).select("img").attr("src");
		}
		if(StringUtils.isEmpty(text)){
			return "";
		}
		if(!text.contains("src=")){
			return text.replace(" ", "").trim();
		}
		String[] arr = text.split("src=");
		if(arr.length>1){
			text = arr[1];
			arr = text.split("'");
			if(arr.length>2){
				text = arr[1];
				return text;
			}
		}
		return "";
	}
	
	public static String getAreaFromText(String text){
		//需要过滤括号里的内容
		if(StringUtils.isEmpty(text)){
			return "";
		}
		text = text.replace(String.valueOf((char)160), "");
		text = text.replace("小区：", "");
		text = text.replace("位置：", "");
		text = text.replace("(出售)", "");
		text = text.replace("（出售 ）", "");
		text = text.replace("出售", "");
		text = text.replace("):", "");
		text = text.replace(")", "");
		text = text.replace("(单间出租)", "");
		text = text.replace("-", "").trim();
		text = text.split("\\(")[0].trim();
		return text.split(" ")[0];
	}

	public static String getPeiZhiFromText(String script){
		script = script.replace("\r\n", "").replace("\t", "");
		script = script.split(" document.write")[0];
		String[] arr = script.split(" tmp =");
		if(arr.length>1){
			return arr[1];
		}
		return "";
	}
	
	public static Date getPubtimeFromText(String text){
		text = text.trim();
		try{
			return DataHelper.dateSdf.parse(text);
		}catch(Exception ex){
			return new Date();
		}
		
	}

	public static String getZjiaFromText(String zjia) {
		if(StringUtils.isEmpty(zjia)){
			return "";
		}
		if(zjia.contains("面议")){
			return "";
		}
		if(zjia.contains("急售")){
			return "";
		}
		return zjia.replace(",", "").replace("价格：", "").replace("万", "").replace("w", "").replace("W", "").replace("元", "").replace("左右", "");
	}

	public static String getZxiuFromText(String zxiu) {
		if(StringUtils.isEmpty(zxiu)){
			return "";
		}
		zxiu = zxiu.replace("房源概况", "");
		if(zxiu.contains("简单装修")){
			return "简装";
		}else if(zxiu.contains("精装修")){
			return "精装";
		}else if(zxiu.contains("中等装修")){
			return "中装";
		}else if(zxiu.contains("豪华装修")){
			return "豪装";
		}else{
			return zxiu.replace("装修情况：", "").replace("房", "");
		}
	}

	public static Float getZujinText(String zujin) {
		try{
			return Float.valueOf(zujin);
		}catch(Exception ex){
			return 0f;
		}
	}

	public static Integer getFangshiText(String fangshi) {
		if(StringUtils.isEmpty(fangshi)){
			return RentType.合租.getCode();
		}
		if(fangshi.contains("合租")){
			return RentType.合租.getCode();
		}
		if(fangshi.contains("单间")){
			return RentType.合租.getCode();
		}
		return RentType.整租.getCode();
	}

	public static String getWoFromText(String wo) {
		if(StringUtils.isEmpty(wo)){
			return "";
		}
		if(wo.contains("主卧")){
			return "主卧";
		}
		if(wo.contains("次卧")){
			return "次卧";
		}
		return "";
	}

	public static String getXianzhiFromText(String text) {
		if(StringUtils.isEmpty(text)){
			return "无";
		}
		if(text.contains("限女性")){
			return "限女性";
		}
		return "男女不限";
	}
}
