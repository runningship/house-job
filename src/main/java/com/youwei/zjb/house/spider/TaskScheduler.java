package com.youwei.zjb.house.spider;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bc.sdak.CommonDaoService;
import org.bc.sdak.TransactionalServiceHelper;
import org.bc.sdak.utils.LogUtil;
import org.bc.web.ThreadSession;

public class TaskScheduler extends Thread{

	CommonDaoService dao = TransactionalServiceHelper.getTransactionalService(CommonDaoService.class);
	public void schedule(){
		ThreadSession.setCityPY("hefei");
//		List<Task> list = dao.listByParams(Task.class, "from Task where taskOn=1 and  status=? or status=? ", KeyConstants.Task_Stop , KeyConstants.Task_Too_Fast);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, -5);
		List<Task> list = dao.listByParams(Task.class, "from Task where taskOn=1 and lasttime<=? ", now.getTime());
		for(Task task : list){
			if(task.status==KeyConstants.Task_Too_Fast){
				if(task.lasttime!=null && System.currentTimeMillis()-task.lasttime.getTime()<15*60*1000){
					continue;
				}
			}
			try {
				//每个任务之间间隔5秒
				Thread.sleep(1000*2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			TaskExecutor te = new TaskExecutor(task);
			te.start();
		}
	}
	
	@Override
	public void run() {
		while(true){
			LogUtil.info("task schedular running");
			try{
				schedule();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			try {
				Thread.sleep(60*1000*5);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}
