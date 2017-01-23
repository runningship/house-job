/*
 * Copyright 2017 Cisco Ltd.
 *
 * Created on Jan 22, 2017
 */

package com.youwei.zjb.house.spider;

import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.bc.sdak.utils.LogUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;


/**
 * @author alxu
 *
 */
public class TaskExecutorTest {

    @Test
    /**
     * 58网的扫网测试用例
     */
    public void test58Chuzu() {
        Task task = new Task();
        task.id = 1;
        task.siteUrl="http://hf.58.com/chuzu/0/";
        task.name="58合肥-出租";
        task.cityPy="hefei";
        task.site="58";
        task.detailPageUrlPrefix="";
        task.taskOn=1;
        task.zufang=1;
        task.encoding="utf8";
        task.status=1;
        task.filterSelector=".dingico;.ico.ding;.ico.dun;.wlt-ico";
        task.listSelector=".listUl li";
//        task.detailLink=".qj-rentd > a";
        task.detailLink=".des>h2> a";
//        task.area="span:containsOwn(小区：)+div;span:contains(小区：)+div a[onclick*=xiaoqu]";
        task.area="a[onclick*=xiaoqu]";
        task.quyu="a[onclick*=quyu]+a[onclick*=shangquan]";
        task.lceng="span:contains(楼层)+span";//楼层
        task.zceng="span:contains(楼层)+span";//总层
        task.hxf="span:contains(房屋)+span";//室
        task.hxt="span:contains(房屋)+span";//厅
        task.hxw="span:contains(房屋)+span";//卫
        task.zxiu="span:contains(房屋)+span";//毛坯，精装
        task.mji="span:contains(房屋)+span";//面积
        task.djia="";//单价
        task.zjia=".f36";//租金
        task.lxr=".agent-name>a[title=点击查看ta的信用]";//联系人
        task.tel="#t_phone;#t_phone script";//电话号码
        task.dateyear="li:containsOwn(建造年代) + li";
        task.address="span:contains(地址)+.dz";
        task.beizhu=".house-word-introduce p";
        task.pubtime=".house-update-info p";
        task.lastError="";
        task.interval=60;
        //task.lasttime="";
        task.wo="div:containsOwn(出租) + .su_con";//主卧
        task.xianzhi="div:containsOwn(出租) + .su_con";//限制
        task.cuzuTitle=".bigtitle h1";
        task.peizhi="span:contains(配置)+div";
        task.fangshi=".bigtitle h1";
        task.city58="hf";
        
       TaskNoDBExecutor te = new TaskNoDBExecutor(task);
       te.execute();
    }
    
    @Test
    /**
     * 58网的扫网测试用例
     */
    public void test58ChuShou() {
        Task task = new Task();
        task.id = 1;
        task.siteUrl="http://wuhu.58.com/ershoufang/0/";
        task.name="芜湖-58-二手房";
        task.cityPy="wuhu";
        task.site="58";
        task.detailPageUrlPrefix="";
        task.taskOn=1;
        task.zufang=0;
        task.encoding="utf8";
        task.status=1;
        task.filterSelector=".ico.ding;.qj-listjjr:contains(经纪人)";
        task.listSelector=".tbimg tr";
        task.detailLink=".bthead a";
//        task.area="span:containsOwn(小区：)+div;span:contains(小区：)+div a[onclick*=xiaoqu]";
        task.area="p:containsOwn(小区名称) :first-child;li:contains(位置);.bigtitle h1";
        task.quyu="li:contains(位置) :nth-child(2);li:contains(位置) :nth-child(3)";
        task.lceng="li:containsOwn(楼层) + li";//楼层
        task.zceng="li:containsOwn(楼层) + li";//总层
        task.hxf="div:containsOwn(户型) + .su_con";//室
        task.hxt="div:containsOwn(户型) + .su_con";//厅
        task.hxw="div:containsOwn(户型) + .su_con";//卫
        task.zxiu="li:containsOwn(装修程度) + li";//毛坯，精装
        task.mji="div:containsOwn(户型) + .su_con";//面积
        task.djia="";//单价
        task.zjia=".bigpri";//租金
        task.lxr="li>div>span>a[rel=nofollow]";//联系人
        task.tel="#t_phone script";//电话号码
        task.dateyear="li:containsOwn(建造年代) + li";
        task.address="div:containsOwn(地址) + div";
        task.beizhu=".description_con :first-child";
        task.pubtime=".mainTitle .timep";
        task.lastError="";
        task.interval=60;
        //task.lasttime="";
        task.city58="wuhu";
        
       TaskNoDBExecutor te = new TaskNoDBExecutor(task);
       te.execute();
    }
    
    @Test
    /**
     * 赶集网的扫网测试用例
     */
    public void testGanjiChuzu() {
        Task task = new Task();
        task.id = 1;
        task.siteUrl="http://fuyang.ganji.com/fang1/a1/";
        task.name="赶集阜阳-出租";
        task.cityPy="hefei";
        task.site="赶集";
        task.detailPageUrlPrefix="";
        task.taskOn=1;
        task.zufang=1;
        task.encoding="utf8";
        task.status=1;
        task.filterSelector=".common-icon-top;.common-icon-tip";
        task.listSelector=".f-main-list>div";
//        task.detailLink=".qj-rentd > a";
        task.detailLink=".img-wrap>a";
//        task.area="span:containsOwn(小区：)+div;span:contains(小区：)+div a[onclick*=xiaoqu]";
        task.area="a[onclick*=xiaoqu]";
        task.quyu="a[onclick*=quyu]+a[onclick*=shangquan]";
        task.lceng="span:contains(房屋)+li";
        task.zceng="span:contains(房屋)+li";
        task.hxf="span:contains(房屋)+li";
        task.hxt="span:contains(房屋)+li";
        task.hxw="span:contains(房屋)+li";
        task.zxiu="span:contains(房屋)+li";
        task.mji="span:contains(房屋)";
        task.djia="";
        task.zjia=".f36";
        task.lxr=".agent-name>a[title=点击查看ta的信用]";
        task.tel="#t_phone;#t_phone script";
        task.dateyear="li:containsOwn(建造年代) + li";
        task.address="span:contains(地址)+.dz";
        task.beizhu=".house-word-introduce p";
        task.pubtime=".house-update-info p";
        task.lastError="";
        task.interval=60;
        //task.lasttime="";
        task.wo="div:containsOwn(出租) + .su_con";
        task.xianzhi="div:containsOwn(出租) + .su_con";
        task.cuzuTitle=".bigtitle h1";
        task.peizhi="span:contains(配置)+div";
        task.fangshi=".bigtitle h1";
        task.city58="fy";

        TaskNoDBExecutor te = new TaskNoDBExecutor(task);
        te.execute();
    }
    
    @Test
    public  void getm58Tel(){
        URL url=null;
        Task task = new Task();
        task.city58="wuhu";
        String detailUrl="http://wuhu.58.com/ershoufang/28816042371651x.shtml";
        try{
            String[] arr = detailUrl.split("\\?");
            arr = arr[0].split("\\/");
            String houseId = arr[arr.length-1];
            String wapUrl="http://wap.58.com/"+task.city58+"/ershoufang/"+houseId+"?device=wap";
            url = new URL(wapUrl);
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("User-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
            conn.setDefaultUseCaches(false);
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            String result = IOUtils.toString(conn.getInputStream(),"utf8");
            Document page = Jsoup.parse(result);
            String tel=page.select("div+font[color=red]").first().html();
            if(StringUtils.isNotBlank(tel)){
                tel=tel.replaceAll("\\s+", "");
            }
            System.out.println("tel:"+tel);
            System.out.println(arr[arr.length-1]);
        }catch(Exception ex){
            LogUtil.log(Level.WARN, "试图从58手机版获取手机号码失败,"+url, ex);
        }
    }
}
