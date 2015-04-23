package com.youwei.zjb.house.spider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.bc.sdak.CommonDaoService;
import org.bc.sdak.TransactionalServiceHelper;
import org.bc.sdak.utils.BeanUtil;
import org.bc.sdak.utils.LogUtil;
import org.bc.web.ThreadSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TaskExecutor extends Thread{

	CommonDaoService dao = TransactionalServiceHelper.getTransactionalService(CommonDaoService.class);
	
	private Task task;
	
	public TaskExecutor(Task task){
		this.task = task;
	}
	
	
	@Override
	public void run() {
		ThreadSession.setCityPY("hefei");
		task.status =KeyConstants.Task_Running;
		dao.saveOrUpdate(task);//设置任务状态为运行中
		try{
			execute();//execute方法会更新task.tatus
		}catch(Exception ex){
			task.status = KeyConstants.Task_Failed;
			task.lastError = ex.getMessage();
			ex.printStackTrace();
		}
		ThreadSession.setCityPY("hefei");
		//更新任务状态
		//重新再从数据库取一遍，防止脏数据
		Task po = dao.get(Task.class, task.id);
		po.lasttime  = new Date();
		po.lastError = task.lastError;
		po.status = task.status;
		dao.saveOrUpdate(po);
	}


	public void execute(){
		LogUtil.info("开始扫网.."+task.name);
		int total  = 0;
		if(StringUtils.isEmpty(task.siteUrl)){
			task.status = KeyConstants.Task_Failed;
			task.lastError = "siteUrl 不能为空";
			return;
		}
		String pageHtml = null;
		try {
			pageHtml = PullDataHelper.getHttpData(task.siteUrl, "", task.encoding);
		} catch (IOException e) {
			task.status = KeyConstants.Task_Stop;
			task.lastError = "访问"+task.siteUrl+"失败";
			LogUtil.log(Level.WARN, "访问"+task.siteUrl+"失败", e);
			return;
		}
		if(pageHtml.contains("您的访问速度太快")){
			task.status = KeyConstants.Task_Too_Fast;
			task.lastError = "访问速度过快";
			return;
		}
		if(StringUtils.isEmpty(task.cityPy)){
			task.status = KeyConstants.Task_Failed;
			task.lastError = "任务未设置城市";
			return;
		}
//		if(pageHtml==null){
//			task.status = KeyConstants.Task_Failed;
//			task.lastError = "列表页面数据获取失败";
//			return;
//		}
		Document page = Jsoup.parse(pageHtml);
		Elements dataList = page.select(task.listSelector);
//		System.out.println(pageHtml);
		if(dataList.isEmpty()){
			task.status = KeyConstants.Task_Stop;
			task.lastError = "列表没有找到数据,listSelector="+task.listSelector;
			return;
		}
		for(int i=dataList.size()-1;i>=0;i--){
			Element elem = dataList.get(i);
			if(isZhiDing(elem)){
				continue;
			}
			Elements link = elem.select(task.detailLink);
			if(link.isEmpty()){
				task.lastError = "列表没有找到详情页面链接,detailLinkSelector="+task.detailLink;
				continue;
			}
			String detailUrl = link.first().attr("href");
			try {
				processDetailPage(detailUrl);
				if(task.interval>0){
					Thread.sleep(task.interval*1000);
				}
				total++;
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
				task.status = KeyConstants.Task_Stop;
				task.lastError = e.getMessage() + ","+detailUrl;
				LogUtil.log(Level.WARN, "任务运行失败，请检查程序", e);
				return;
			} catch(Exception ex){
				//单挑数据失败，继续
				task.lastError = ex.getMessage()+";"+detailUrl;
				LogUtil.log(Level.WARN, "抓取数据失败:"+detailUrl, ex);
			}
		}
		task.status = KeyConstants.Task_Stop;
		LogUtil.info("本次扫网.."+task.name+",共处理"+total+"套新房源");
	}
	
	private boolean isZhiDing(Element elem) {
		if(StringUtils.isEmpty(task.filterSelector)){
			return false;
		}
		for(String sel : task.filterSelector.split(";")){
			if(!elem.select(sel).isEmpty()){
				return true;
			}
		}
		return false;
	}
	private void processDetailPage(String detailUrl) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ThreadSession.setCityPY(task.cityPy);
		if(StringUtils.isNotEmpty(task.detailPageUrlPrefix)){
			detailUrl = task.detailPageUrlPrefix+detailUrl;
		}
		if(task.zufang==0){
			prcessChushou(detailUrl);
		}else{
			prcessChuzu(detailUrl);
		}
		
	}

	private void prcessChuzu(String detailUrl) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		HouseRent po = dao.getUniqueByKeyValue(HouseRent.class, "href", detailUrl);
		if(po!=null){
			return;
		}
		String pageHtml = PullDataHelper.getHttpData(detailUrl, "", task.encoding);
		if(pageHtml.contains("页面可能被删除")){
			return;
		}
		if(pageHtml.contains("访问速度太快") || pageHtml.contains("过于频繁")){
			task.status = KeyConstants.Task_Too_Fast;
			return;
		}
		
		Document page = Jsoup.parse(pageHtml);
		HouseRent hr = new HouseRent();
		hr.cid = 1;
		hr.did = 90;
		hr.dhao = "";
		hr.fhao="";
		hr.sh=1;
		hr.ruku=1;
		hr.href = detailUrl;
		String area = getDataBySelector(page , "area");
		hr.area = TaskHelper.getAreaFromText(area);
		String address = getDataBySelector(page , "address");
		hr.address = address.replace("地址：", "").replace("()", "").replace(String.valueOf((char)160), "").replace("»", "");
		
		String dateyear = getDataBySelector(page , "dateyear");
		hr.dateyear = String.valueOf(TaskHelper.getYearFromText(dateyear));
		
		String beizhu = getDataBySelector(page , "beizhu");
		hr.beizhu = beizhu;
		
		//租金
		String zujin = getDataBySelector(page , "zjia");
		hr.zjia = TaskHelper.getZujinText(zujin);
		String fangshi = getDataBySelector(page , "fangshi");
		hr.fangshi = TaskHelper.getFangshiText(fangshi);
		String lxr = getDataBySelector(page , "lxr");
		hr.lxr = lxr.replace("个人", "").replace("姓名： ", "");
		String lceng = getDataBySelector(page , "lceng");
		hr.lceng = TaskHelper.getLcengFromText(lceng);
		
		String zceng = getDataBySelector(page , "zceng");
		hr.zceng = TaskHelper.getZcengFromText(zceng);
		
		String hxf = getDataBySelector(page , "hxf");
		hr.hxf = TaskHelper.getHxsFromText(hxf);
		
		String hxt = getDataBySelector(page , "hxt");
		hr.hxt = TaskHelper.getHxtFromText(hxt);
		 
		String hxw = getDataBySelector(page , "hxw");
		hr.hxw = TaskHelper.getHxwFromText(hxw);
		
		String zxiu = getDataBySelector(page , "zxiu");
		zxiu = TaskHelper.getZxiuFromText(zxiu);
		hr.zxiu = zxiu;
		
		String mji = getDataBySelector(page , "mji");
		hr.mji = TaskHelper.getMjiFromText(mji);
		String quyu = "";
		if(task.quyu.startsWith("@")){
				quyu = task.quyu.replace("@", "");
		}else{
			quyu = getDataBySelector(page , "quyu");
			quyu = quyu.replace("区域：", "");
			if(StringUtils.isNotEmpty(quyu)){
				if(quyu.length()>2){
					quyu = quyu.replace("区", "");
					quyu = quyu.replace("县", "");
				}
			}
		}
		
		hr.site = task.site;
		hr.quyu = quyu;
		hr.seeFH = 1;
		hr.seeGX = 1;
		hr.seeHM = 1;
		hr.ztai = String.valueOf(RentState.在租.getCode());
		
		String wo = getDataBySelector(page , "wo");
		hr.wo = TaskHelper.getWoFromText(wo);
		String xianzhi = getDataBySelector(page , "xianzhi");
		hr.xianzhi = TaskHelper.getXianzhiFromText(xianzhi);
		String peizhi = getDataBySelector(page , "peizhi");
		hr.peizhi = TaskHelper.getPeiZhiFromText(peizhi);
		hr.title = getDataBySelector(page , "cuzuTitle");
		
		hr.isdel = 0;
		
		String tel = getDataBySelector(page , "tel");
		page.select("td:containsOwn(出租租金：)");
		tel = TaskHelper.getTelFromText(tel);
		if(tel.contains("http")){
			hr.telImg = tel;
		}else if(tel.contains("img")){
			hr.telImg = task.detailPageUrlPrefix+tel;
		}else if(tel.contains("image")){
			hr.telImg = tel;
		}else{
			hr.tel = tel.replace(" ", "").replace("移动电话：", "");
		}
		String pubtime = getDataBySelector(page , "pubtime");
		hr.dateadd = TaskHelper.getPubtimeFromText(pubtime);
		LogUtil.info("抓取到"+task.name+"房源信息:"+BeanUtil.toString(hr));
		dao.saveOrUpdate(hr);
	}


	private void prcessChushou(String detailUrl) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		House po = dao.getUniqueByKeyValue(House.class, "href", detailUrl);
		if(po!=null){
			return;
		}
		String pageHtml = PullDataHelper.getHttpData(detailUrl, "", task.encoding);
		if(pageHtml.contains("页面可能被删除")){
			return;
		}
		if(pageHtml.contains("访问速度太快") || pageHtml.contains("过于频繁")){
			task.status = KeyConstants.Task_Too_Fast;
			return;
		}
		Document page = Jsoup.parse(pageHtml);
		House house = new House();
		house.cid = 1;
		//信息中心
		house.did = 90;
//		house.lxing="";
		house.ztai = "4";
		house.sh = 1;
		house.seeFH = 1;
		house.seeGX = 1;
		house.seeHM = 1;
		house.dhao = "";
		house.site = task.site;
		house.href = detailUrl;
		
		String area = getDataBySelector(page , "area");
		if(area.contains("地址:")){
			area = area.split(String.valueOf((char)160))[1].replace("-", "");
		}else{
			area = TaskHelper.getAreaFromText(area).replace("地址:", "");
		}
		house.area = area;
		if(StringUtils.isEmpty(house.area.trim())){
			System.out.println(detailUrl);
		}
//		page.select("li:contains(小区)").first().ownText()
//		page.select("span:containsOwn(地区) :first-child")
		String quyu = "";
		if(task.quyu.startsWith("@")){
				quyu = task.quyu.replace("@", "");
		}else{
			quyu = getDataBySelector(page , "quyu");
			quyu = quyu.replace("位置：", "").replace("()", "").trim();
			if(StringUtils.isNotEmpty(quyu)){
				if(quyu.contains("地址:")){
					quyu = quyu.replace("地址:", "").split(String.valueOf((char)160))[0];
				}else	if(quyu.length()>2){
					quyu = quyu.replace("区", "").replace("域：", "");
					quyu = quyu.replace("县", "").replace("域：", "");
				}
			}
		}
		house.quyu = quyu;
		
		String address = getDataBySelector(page , "address");
		if(address.contains("地址:")){
			address = address.split(String.valueOf((char)160))[2].replace("-", "");
		}else{
			address = address.replace("地址：", "").replace("()", "");
		}
		house.address = address;
		
		String lceng = getDataBySelector(page , "lceng");
		house.lceng = TaskHelper.getLcengFromText(lceng);
		
		String zceng = getDataBySelector(page , "zceng");
		house.zceng = TaskHelper.getZcengFromText(zceng);
		
		String hxf = getDataBySelector(page , "hxf");
		house.hxf = TaskHelper.getHxsFromText(hxf);
		
		String hxt = getDataBySelector(page , "hxt");
		house.hxt = TaskHelper.getHxtFromText(hxt);
		 
		String hxw = getDataBySelector(page , "hxw");
		house.hxw = TaskHelper.getHxwFromText(hxw);
		
		String zxiu = getDataBySelector(page , "zxiu");
		zxiu = TaskHelper.getZxiuFromText(zxiu);
		//装修情况：毛坯房
		house.zxiu = zxiu;
		
		String mji = getDataBySelector(page , "mji");
		house.mji = TaskHelper.getMjiFromText(mji);
		String zjia = getDataBySelector(page , "zjia");
		zjia = TaskHelper.getZjiaFromText(zjia);
		//价格：48万元
		try{
			if(StringUtils.isEmpty(zjia)){
				house.zjia = 0f;
			}else{
				house.zjia = Float.valueOf(zjia);
			}
		}catch(Exception ex){
			LogUtil.log(Level.INFO, "获取价格失败,href="+detailUrl+"zjia="+zjia, ex);
		}
		
		
		
//		house.djia = Float.valueOf(djia);
		if(house.mji!=null && house.mji!=0f){
			house.djia = house.zjia*10000/house.mji;
			house.djia = house.djia.intValue()*1.0f;
		}
		
		String lxr = getDataBySelector(page , "lxr");
		house.lxr = lxr.replace("联系人： ", "").replace("个人", "").replace("姓名： ", "");
		
		String tel = getDataBySelector(page , "tel");
		page.select("#t_phone");
		Elements whao = page.select(".show-contact");
		if(tel.contains("http:")){
			house.telImg = TaskHelper.getTelFromText(tel);
		}else if(tel.contains("<img")){
			house.telImg = TaskHelper.getTelFromText(tel).replace("/..", task.detailPageUrlPrefix);
		}else{
			if(whao.isEmpty()){
				house.tel = tel.replace(" ", "").replace("移动电话：", "");
			}else {
				String weihao = whao.first().attr("data-contact");
				house.tel = tel.replace("*", "")+weihao;
			}
			
		}
		
		
		String dateyear = getDataBySelector(page , "dateyear");
		house.dateyear = TaskHelper.getYearFromText(dateyear);
		
		String beizhu = getDataBySelector(page , "beizhu");
		house.beizhu = beizhu;
		
//		house.dateadd = new Date();
		String pubtime = getDataBySelector(page , "pubtime");
		house.dateadd = TaskHelper.getPubtimeFromText(pubtime);
		LogUtil.info("抓取到"+task.name+"房源信息:"+BeanUtil.toString(house));
		dao.saveOrUpdate(house);
	}


	private String getDataBySelector(Document page ,String selectorField) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field = Task.class.getField(selectorField);
		String selectors = (String)field.get(task);
		if(StringUtils.isEmpty(selectors)){
			return "";
		}
		for(String sel : selectors.split(";")){
			Elements elems = page.select(sel);
//			page.select("li:contains(位置)").first().ownText(); //楼盘 p:containsOwn(小区名称) :first-child
//			page.select("li:contains(位置) a").first().ownText();//区域
//			page.select("li:containsOwn(楼层) + li"); //楼层
//			page.select("p:containsOwn(小区名称) :first-child"); //地址
//			page.select("li:containsOwn(装修程度) + li"); //装修
//			page.select("li:containsOwn(建造年代) + li"); //年代
//			page.select("div:containsOwn(户型) + .su_con"); //户型 面积
//			page.select(".bigpri");//售价
//			page.select(".liv0 a"); //联系人
//			page.select("#t_phone script");//电话
//			page.select(".description_con :first-child");//备注
			//page.select(".mainTitle.time");//发布时间
			if(elems.isEmpty()){
				continue;
			}
			String text = elems.first().ownText();
			if(StringUtils.isEmpty(text)){
				text = elems.first().text();
			}
			if(StringUtils.isEmpty(text)){
				if(elems.first().html().contains("img")){
					text = elems.first().html();
				}
			}
			if(StringUtils.isEmpty(text)){
				if(elems.first().outerHtml().contains("img")){
					text = elems.first().outerHtml();
				}
			}
			//过滤点无用字符
			if(!text.contains("src")){
				text = text.replace("-", "");
			}
			String tmp = text.replace(" ", "").replace(String.valueOf((char)160),"");
			if(StringUtils.isEmpty(tmp)){
				continue;
			}
			return text;
		}
		return "";
	}
	
	public static void main(String[] args) throws Exception{
//		StartUpListener.initDataSource();
		Task task  = new Task();
		task.cityPy = "bengbu";
		TaskExecutor te = new TaskExecutor(task);
		task.area= "p:containsOwn(小区名称) :first-child";
//		task.tel="#t_phone script";
		te.processDetailPage("http://bengbu.58.com/ershoufang/21472816271009x.shtml");
	}
}
