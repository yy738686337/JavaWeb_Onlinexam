package com.onlinexam.servlet.student;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onlinexam.po.Paper;
import com.onlinexam.po.Question;
import com.onlinexam.po.Student;
import com.onlinexam.service.teacher.PaperService;
import com.onlinexam.service.teacher.QuestionService;
import com.onlinexam.service.teacher.TestService;
import com.onlinexam.util.ToolUtil;

@WebServlet(value="/studentTest")
public class StudentTestServlet extends HttpServlet{
	
	private ToolUtil  toolUtil = new ToolUtil();
	TestService testService = new TestService();
	PaperService paperService = new PaperService();
	QuestionService questionService = new QuestionService();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String id = req.getParameter("testId");
		
		Student student = (Student)req.getSession().getAttribute("user");
		int studentid = student.getId();
		Map test = testService.findStudentTestsById(studentid, Integer.valueOf(id));
		
		String ids = (String)test.get("question_ids");
		List ques = questionService.findQuestionByIds(ids);
		Double sum = (Double)test.get("score");
		Double scoreperques = sum / ques.size();
		
		req.setAttribute("scoreperques", scoreperques);
		req.setAttribute("quesList", ques);
		req.setAttribute("test", test);
		
		req.setAttribute("current", test.get("test_time"));
		System.out.println(test.get("test_time"));
		
		req.setAttribute("user", student);
		req.getRequestDispatcher("student/exam.jsp").forward(req, resp);
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		
		Student student = (Student)req.getSession().getAttribute("user");
		String id = req.getParameter("testId");
		
		String courseId = req.getParameter("courseId");
		Double scoreSum = 0.0;
		Map test = testService.findStudentTestsById(student.getId(),Integer.valueOf(id));
		
		StringBuffer wrongAnsIds = new StringBuffer();
		StringBuffer wrongAns = new StringBuffer();
		
		List<Question> quesList = questionService.findQuestionByIds((String)test.get("question_ids"));
		if(quesList == null || quesList.size() < 1) 
			req.getRequestDispatcher("pastTestServlet").forward(req, resp);
		
		int i = -1;
		for (Question question : quesList) {
			i ++;
			String quesId = String.valueOf(question.getId());
			String StuAns = "";
			try {
				StuAns = req.getParameter("ques_" + quesId);
			} catch (Exception e) {
				StuAns = "";
				wrongAnsIds.append(quesId + ",");
				wrongAns.append("未答,") ;
			}
			if(StuAns != null && !"".equals(StuAns)) {
				if (question.getAns().equals(StuAns)) {
					Double scorePerQues = Double.valueOf(req.getParameter("scoreperques"));
					scoreSum += scorePerQues;
				} else {
					wrongAnsIds.append(quesId + ",");
					wrongAns.append(StuAns + ",");
				}
			} else {
				wrongAnsIds.append(quesId + ",");
				wrongAns.append("未答,") ;
			}
		}
		String time = req.getParameter("hidden1");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
		Paper paper = new  Paper(Integer.valueOf(id), time, Integer.valueOf(courseId), scoreSum,(wrongAnsIds).substring(0, wrongAnsIds.length() - 1) 
				, (wrongAns).substring(0, wrongAns.length() - 1), student.getId(),
				new Date());
		paperService.save(paper);
		System.out.println(scoreSum);
		req.getRequestDispatcher("student/index.jsp").forward(req, resp);
	}

}
