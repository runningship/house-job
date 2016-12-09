package com.youwei.zjb.house.spider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.bc.sdak.CommonDaoService;
import org.bc.sdak.TransactionalServiceHelper;
import org.bc.sdak.utils.BeanUtil;
import org.bc.sdak.utils.LogUtil;
import org.bc.web.ThreadSession;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
//		po.status = KeyConstants.Task_Stop;
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
			if(task.siteUrl.contains("58")){
				pageHtml = PullDataHelper.getHttpData(task.siteUrl+"?"+new Date().getTime(), "", task.encoding);
			}else{
				pageHtml = PullDataHelper.getHttpData(task.siteUrl, "", task.encoding);
			}
			
		} catch (IOException e) {
			task.status = KeyConstants.Task_Stop;
			task.lastError = "访问"+task.siteUrl+"失败 at "+new Date();
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
			if(task.site.equals("58")){
				URL url;
				try {
					url = new URL(detailUrl);
					detailUrl = url.toExternalForm().replace("?"+url.getQuery(),"");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			try {
				processDetailPage(detailUrl);
				if(task.interval>0){
					Thread.sleep(task.interval*1000);
				}
				total++;
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
				task.status = KeyConstants.Task_Stop;
				task.lastError = e.getMessage() + ","+detailUrl;
				LogUtil.log(Level.WARN, "单条数据抓取运行失败，请检查程序"+detailUrl, e);
				continue;
			} catch(DataInvalidException ex){
				LogUtil.info(detailUrl+"信息不完整");
				continue;
			}catch(Exception ex){
				if(!"重复的房源".equals(ex.getMessage())){
					task.lastError = ex.getMessage()+";"+detailUrl;
					LogUtil.log(Level.WARN, "抓取数据失败:"+detailUrl, ex);
				}
				//单条数据失败，继续
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
	private void processDetailPage(String detailUrl) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, DataInvalidException {
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

	private void prcessChuzu(String detailUrl) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, DataInvalidException {
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
		if(StringUtils.isEmpty(hr.area)){
			throw new DataInvalidException("楼盘名称不存在");
		}
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
				if(!task.cityPy.equals("hefei")){
					if(quyu.length()>2){
						quyu = quyu.replace("区", "");
						quyu = quyu.replace("县", "");
					}
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
		if("58".equals(hr.site)){
			System.out.println();
			hr.tel = TaskHelper.getmRent58Tel(task, detailUrl);
			LogUtil.info("租房获取到58手机号码:"+hr.tel);
		}
		if(StringUtils.isEmpty(hr.tel)){
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
		}
		
		String pubtime = getDataBySelector(page , "pubtime");
		hr.dateadd = TaskHelper.getPubtimeFromText(pubtime);
		LogUtil.info("抓取到"+task.name+"房源信息:"+BeanUtil.toString(hr));
		dao.saveOrUpdate(hr);
	}


	private void prcessChushou(String detailUrl) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, DataInvalidException {
		if(StringUtil.isBlank(detailUrl)){
			throw new RuntimeException("无效的数据, detailUrl is null ");
		}
		
		if(detailUrl.contains("click.ganji.com")){
			throw new RuntimeException("无效的数据");
		}
		
		detailUrl=filterULR(detailUrl);
		LogUtil.info("抓取到"+task.name+"房源信息， URL:"+detailUrl);
		
		House po = dao.getUniqueByKeyValue(House.class, "href", detailUrl);
		if(po!=null){
			LogUtil.info(task.name+"重复的房源"+detailUrl);
			po.updatetime = new Date();
			dao.saveOrUpdate(po);
			throw new RuntimeException("重复的房源");
		}
		String pageHtml = PullDataHelper.getHttpData(detailUrl, "", task.encoding);
		if(pageHtml.contains("页面可能被删除")){
			throw new RuntimeException("页面可能被删除");
		}
		if(pageHtml.contains("访问速度太快") || pageHtml.contains("过于频繁")){
			task.status = KeyConstants.Task_Too_Fast;
			throw new RuntimeException("访问速度太快");
		}
		Document page = Jsoup.parse(pageHtml);
		House house = new House();
		house.cid = 1;
		//信息中心
		house.did = 90;
//		house.lxing="";
		house.ztai = "4";
		if("wuhu".equals(task.cityPy)){
			house.sh=0;
		}else{
			house.sh=1;
		}
		house.seeFH = 1;
		house.seeGX = 1;
		house.seeHM = 1;
		house.dhao = "";
		house.site = task.site;
		house.href = detailUrl;
		
		String area = getDataBySelector(page , "area");
		if(StringUtils.isEmpty(area)){
			throw new DataInvalidException(detailUrl);
		}
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
					quyu = quyu.replace("市", "").replace("域：", "");
				}
			}else{
				quyu="其他";
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
		if(task.siteUrl.equals("http://www.aofenglu.com/esf/esftuijian1.asp")){
			if(lxr.split("联系人：").length>1){
				lxr = lxr.split("联系人：")[1].replace("）", "");
			}
		}
		house.lxr = lxr.replace("联系人： ", "").replace("个人", "").replace("姓名： ", "").replace("联 系 人：", "");
		
		if("58".equals(house.site)){
			house.tel = TaskHelper.getm58Tel(task, detailUrl);
			LogUtil.info("获取到58手机号码:"+house.tel);
		}
		if(StringUtils.isEmpty(house.tel)){
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
		}
		
		String dateyear = getDataBySelector(page , "dateyear");
		house.dateyear = TaskHelper.getYearFromText(dateyear);
		
		String beizhu = getDataBySelector(page , "beizhu");
		house.beizhu = beizhu;
		house.isdel=0;
//		house.dateadd = new Date();
		String pubtime = getDataBySelector(page , "pubtime");
		house.dateadd = TaskHelper.getPubtimeFromText(pubtime);
		//清洗房源
		if(cleanse(house)){
		  return;  
		}
		
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
//			page.select(".peizhi span").first().html()
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
			if(StringUtils.isEmpty(text)){
				if(elems.first().html().contains("tmp = ")){
					text = elems.first().html();
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
	
	private String filterULR(String detailUrl) throws MalformedURLException{
		URL url=new URL(detailUrl);
		 return url.toExternalForm().replace("?"+url.getQuery(),"");
	}
	
    /**
     * @param house
     * 
     * @return false : not cleansed; true : cleansed
     * 
     */
    public boolean cleanse(House house) {
      if(house == null){
          return false;
      }
      
      //清洗蚁族房源
      String filterCondition = "房蚁安徽站";
      if(!StringUtils.isBlank(house.beizhu) && house.beizhu.contains(filterCondition)){
          LogUtil.info("清洗"+task.name+"蚁族房源信息:"+BeanUtil.toString(house));
          return true;
      }
      return false;
    }

	
	public static void main(String[] args) throws Exception{
//		StartUpListener.initDataSource();
//		Task task  =  SimpDaoTool.getGlobalCommonDaoService().get(Task.class, 131);
	    Task task = new Task();
		TaskExecutor te = new TaskExecutor(task);
//		te.run();
//		//te.processDetailPage("http://bengbu.baixing.com/ershoufang/a768899475.html?index=81");
//		URL url=new URL("http://wuhu.ganji.com//fang5/2397485328x.htm?jingxuan=INKicKZPP1V6kz_LL6uyeVFm4eEH5-ItIelvGdnQQlEmWMyVaWca3SaXdpxFDRy4a3xtWApubhCQUc6hx4JL6YlrJUb91d6v-Dh66Ig3pOzcTfsimkfr_Q&trackkey=2b89101f0078896ca2d78c47f6d12e08");
//		String detailUrl = url.toExternalForm().replace("?"+url.getQuery(),"");
//		System.out.print(detailUrl);
	    
	    House house = new House();
	    house.beizhu ="你还在为收集个人房源而守在电脑前刷屏吗？ 你还在为手慢而抢录不到新房源捶胸顿足吗？ 你还在纳闷小伙伴录新房源老是比自己快吗？ 不是你手慢，而是别人开挂！ 关注【房蚁安徽站】公众号,体验开挂抢房的乐趣";
	    
	    System.out.println(te.cleanse(house));
	}
}
