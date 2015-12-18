package com.youwei.zjb.house.spider;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class House {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;

	/**
	 * 房产公司
	 */
	public Integer cid;
	
	/**
	 * 店面编号
	 */
	public Integer did;

	public Integer uid;

	@Column(nullable=false)
	public String quyu;

	/**
	 * 楼盘名称
	 */
	@Column(nullable=false)
	public String area;

	/**
	 * 楼栋号
	 */
	@Column(nullable=false)
	public String dhao;

	/**
	 * 房号
	 */
	public String fhao;

	@Column(name="luduan")
	public String address;

	/**
	 * 楼层
	 */
	@Column(nullable=false)
	public Integer lceng;

	/**
	 * 总层
	 */
	public Integer zceng;

	/**
	 * 楼型
	 */
	public String lxing;

	/**
	 * 房型(房数)
	 */
	public Integer hxf;

	/**
	 * 房型(厅数)
	 */
	public Integer hxt;

	/**
	 * 房型(卫数)
	 */
	public Integer hxw;


	/**
	 * 装修
	 */
	public String zxiu;

	public Float mji;

	/**
	 * 总价(单位万)
	 */
	public Float zjia;

	public Float djia;

	/**
	 * 联系人,房主
	 */
	public String lxr;

	/**
	 * 房主电话，/ 分隔
	 * 已分开放在HouseTel表中，但查看房源中仍使用这个字段
	 */
	public String tel;

	/**
	 * 发布人
	 */
	public String forlxr;

	public String fortel;

	public String beizhu;

	 /**
	 * 状态：4 在售，6 已售，7 停售
	 */
	public String ztai;

	public Integer dateyear;

	@Column(name="dateadds")
	public Date dateadd;

	public Date dategenjin;
	
	public Date dategjlock;
	
	/**
	 * 收藏此房源的人
	 */
	public String fav;

	/**
	 * 1,已删除 0 或空未删除
	 * 好像没用
	 */
//	public Integer isdel;

	/**
	 * 1 审核通过，0 审核不通过
	 */
	public Integer sh;
	
	/**
	 * 共享该房源
	 */
	public Integer seeGX;
	
	/**
	 * 是否可以看房主号码
	 */
	public Integer seeHM;
	
	/**
	 * 是否可以看房号
	 */
	public Integer seeFH;

	public Integer isdel;

	public String href;
	
	public String site;
	
	public String telImg;
	
	public Date updatetime;
}
