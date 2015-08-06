package com.youwei.zjb.house.spider;

import org.bc.sdak.SimpDaoTool;

public class TaskExecutorTest {

	public static void main(String[] args){
//		String str = "第15层(共22层)";
//		Pattern.matches("第[0-9]*层", "第15层");
//		Pattern.compile("第[0-9]*层").matcher("第15层").find();
//		String tmp = Pattern.compile("第[0-9]*层").matcher("15层").appendTail(new StringBuffer());
		StartUpListener.initDataSource();
		Task task = SimpDaoTool.getGlobalCommonDaoService().get(Task.class, 116);
		System.out.println(task.name);
		TaskExecutor te = new TaskExecutor(task);
		te.execute();
	}
}
