package com.youwei.zjb.house.spider;

import org.bc.sdak.CommonDaoService;
import org.bc.sdak.TransactionalServiceHelper;

public class SpiderRun {

	static CommonDaoService dao = TransactionalServiceHelper.getTransactionalService(CommonDaoService.class);
	public static void main(String[] args){
		StartUpListener.initDataSource();
		TaskScheduler ts = new TaskScheduler();
		ts.start();
	}
}
