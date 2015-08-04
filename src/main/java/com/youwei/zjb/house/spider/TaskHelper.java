package com.youwei.zjb.house.spider;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.bc.sdak.utils.LogUtil;
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
		text=text.replace(" ", "").replace("两", "二");
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
		text=text.replace(" ", "").replace("两", "二");
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
		text=text.replace(" ", "").replace("两", "二");
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
		text = text.replace(" ", "").replace(String.valueOf((char)160),"");
		Matcher match = Pattern.compile("第[0-9]*层").matcher(text);
		if(match.find()){
			String tmp = match.group();
			tmp = tmp.replace("第", "").replace("层", "");
			try{
				return Integer.valueOf(tmp);
			}catch(Exception ex){
				//暂不处理
				return 0;
			}
		}
		text = text.replace("楼层：", "").replace("总高", "").replace("楼", "").replace("层", "").trim().replace(" ", "/").replace("，", "/");
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
		text = text.replace(" ", "").replace(String.valueOf((char)160),"").replace("楼层：", "");
		Matcher match = Pattern.compile("[总共高]+[0-9]*层").matcher(text);
		if(match.find() && !text.contains("/")){
			String tmp =match.group();
			tmp = tmp.replace("共", "").replace("总高", "").replace("总", "").replace("层", "");
			try{
				return Integer.valueOf(tmp);
			}catch(Exception ex){
				//暂不处理
				return 0;
			}
		}
		text = text.replace("楼层：", "").replace("楼", "").replace("层", "").trim().replace(" ", "/").replace("，", "/");
		try{
			String[] lcen = text.split("/");
			int i = lcen.length-1;
			try{
				return Integer.valueOf(lcen[i]);
			}catch(Exception ex){
				return 0;
			}
		}catch(Exception ex){
			//暂不处理
			return 0;
		}
	}
	
	public static int getYearFromText(String text){
		String tmp = "";
		if(text.contains("年")){
			tmp =  text.replace("年代：", "").replace("年", "").trim();
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
		text = text.replace(" ㎡", "㎡").replace(" m²", "㎡").replace(String.valueOf((char)160),"");
		text  = text.replace("　", " ").replace("面积：", "").replace("平米", "㎡");
		text = text.replace("产证", "").replace("平方米", "").trim();
		text = text.split("（")[0];
		try{
			return Float.parseFloat(text);
		}catch(Exception ex){
		}
		
		for(String str : text.split(" ")){
			if(str.contains("㎡") && !str.contains("（")){
				tmp =  str.split("㎡")[0].trim();
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
		text = text.replace("小区名称：", "");
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
		}else{
			return arr[0];
		}
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
		String tmp = zjia.replace(",", "").replace("价格：", "").replace("万", "").replace("w", "").replace("W", "").replace("元", "").replace("左右", "");
		tmp = tmp.replace("/平方米", "").replace("一平方", "");
		tmp = tmp.replace(String.valueOf((char)8195), "");
		return tmp;
	}

	public static String getZxiuFromText(String zxiu) {
		if(StringUtils.isEmpty(zxiu)){
			return "";
		}
		zxiu = zxiu.replace("房源概况", "");
		if(zxiu.contains("简单装修") || zxiu.contains("简易装修")||zxiu.contains("简装")){
			return "简装";
		}else if(zxiu.contains("精装修")||zxiu.contains("精装") || zxiu.contains("高档装修")){
			return "精装";
		}else if(zxiu.contains("中等装修")||zxiu.contains("中装") || zxiu.contains("中档装修")){
			return "中装";
		}else if(zxiu.contains("豪华装修")||zxiu.contains("豪装")){
			return "豪装";
		}else if(zxiu.contains("毛坯")){
			return "毛坯";
		}else{
			return "毛坯";
		}
	}

	public static Float getZujinText(String zujin) {
		if(zujin.contains("面议")){
			return 0f;
		}
		if(zujin.contains("急售")){
			return 0f;
		}
		zujin = zujin.replace(String.valueOf((char)12288), "");
		zujin = zujin.replace("元/月","").replace("元", "").trim();
		try{
			return Float.valueOf(zujin);
		}catch(Exception ex){
			LogUtil.log(Level.INFO, "获取价格失败,zujin="+zujin, ex);
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
		if(text.contains("限女")){
			return "限女性";
		}else if(text.contains("限男")){
			return "限男性";
		}else{
			return "男女不限";
		}
	}

	public static void main(String[] args) throws IOException{
		TaskHelper.getm58Tel(null,"http://hf.58.com/ershoufang/21");
	}
	public static String getm58Tel(Task task, String detailUrl){
		try{
			String[] arr = detailUrl.split("\\?");
			arr = arr[0].split("\\/");
			String houseId = arr[arr.length-1];
			URL url = new URL("http://m.58.com/hf/ershoufang/"+houseId);
			URLConnection conn = url.openConnection();
			conn.addRequestProperty("User-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			String result = IOUtils.toString(conn.getInputStream(),"utf8");
			System.out.println(result);
			Document page = Jsoup.parse(result);
			String href= page.getElementById("contact_phone").attr("href");
			arr = href.split(":");
			return arr[arr.length-1];
		}catch(Exception ex){
			LogUtil.log(Level.WARN, "试图从58手机版获取手机号码失败", ex);
			return "";
		}
	}
}
