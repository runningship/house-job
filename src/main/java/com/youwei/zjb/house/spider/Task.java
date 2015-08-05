package com.youwei.zjb.house.spider;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Task {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;
	
	public String siteUrl;
	
	public String name;
	
	//数据库信息
	public String cityPy;
	
	//网站名称
	public String site;
	
	public int zufang;
	
	public String detailPageUrlPrefix;
	//是否开启
	public int taskOn;
	
	//运行状态
	public int status;
	
	public String encoding;
	
	//每条房源之间的时间间隔(单位秒)
	public int interval;
	
////////////以下字段为选择器/////////////
	public String filterSelector;
	
	//列表数据选择器
	public String listSelector;
	
	//详情页面链接选择器
	public String detailLink;
	//楼盘名称选择器
	public String area;
	
	public String quyu;
	
	public String lceng;
	
	public String zceng;
	
	public String hxf;
	
	public String hxt;
	
	public String hxw;
	
	public String zxiu;
	
	public String mji;
	
	public String zjia;
	
	public String djia;
	
	//联系人，房主
	public String lxr;
	
	//房主电话
	public String tel;
	
	public String dateyear;
	
	public String beizhu;
	
	public String address;
	
	public String pubtime;
	
	//出租信息
	public String wo;
	
	public String peizhi;
	
	public String cuzuTitle;
	
	public String xianzhi;
	
	public String fangshi;
	////////////以上字段为选择器/////////////
	
	//最近一次错误信息
	public String lastError;
	
	public Date lasttime;
	
	//58网站各个城市站点的拼音,如hf,la
	public String city58;
}
