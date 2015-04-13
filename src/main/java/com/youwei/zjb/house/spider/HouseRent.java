package com.youwei.zjb.house.spider;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="house_rent")
public class HouseRent {

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
	 * 租金
	 */
	public Float zjia;

	public Float djia;

	/**
	 * 联系人,房主
	 */
	public String lxr;

	/**
	 * 房主电话，/ 分隔
	 */
	public String tel;

	/**
	 * 发布人
	 */
	public String forlxr;

	public String fortel;

	public String beizhu;

	 /**
	 * 状态：1 在租，2 已租，3 停租
	 */
	public String ztai;

	public String dateyear;

	@Column(name="dateadds")
	public Date dateadd;

	/**
	 * 收藏此房源的人
	 */
	public String fav;

	/**
	 * 1,已删除 0 或空未删除
	 */
	public Integer isdel;

	public Integer sh;
	
	/**
	 * 1 整租, 2 合租
	 */
	public Integer fangshi;
	
	public Integer seeGX;
	
	public Integer seeHM;
	
	public Integer seeFH;

	public String href;
	
	public String site;
	
//	public String code;
	
//	public String repeat;
	
	public String wo;
	
	public String xianzhi;
	
	public String peizhi;
	
	public String title;
	public String telImg;
	
	public int ruku;
	
}
