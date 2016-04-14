package com.codelab.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailService {
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	@Resource
	private JavaMailSenderImpl mailSender;

	@Resource
	private TaskExecutor taskExecutor;

	public void sendEmail(String from, List<String> to, String subject, String content, boolean html, boolean asyn) throws Exception {
		for (String s : to) {
			sendEmail(from, s, subject, content, html, asyn);
		}
	}

	public void sendEmail(String from, String to, String subject, String content, boolean html, boolean asyn) throws Exception {

		if (asyn)
			asynSendEmail(from, to, subject, content, html);
		else
			syncSendMail(from, to, subject, content, html);
	}

	/*
	 * public void sendEmail(List<String> to, String subject, String content,
	 * boolean html) throws Exception { for (String s : to) { sendEmail(s,
	 * subject, content, html); } }
	 * 
	 * public void sendEmail(String to, String subject, String content, boolean
	 * html) throws Exception { String from = configManager.getMailFrom();
	 * syncSendMail(from, to, subject, content, html); }
	 */

	private void asynSendEmail(final String from, final String to, final String subject, final String content, final boolean html)
			throws Exception {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				syncSendMail(from, to, subject, content, html);
			}
		});
	}

	private void syncSendMail(final String from, final String to, final String subject, final String content, final boolean html) {
		try {
			MimeMessage message = mailSender.createMimeMessage();

			String charset = "utf-8";
			MimeMessageHelper messageHepler = new MimeMessageHelper(message, false, charset);
			messageHepler.setFrom(from);
			messageHepler.setTo(to);
			messageHepler.setSubject(subject);
			messageHepler.setText(content, html);
			mailSender.send(message);
			if (logger.isInfoEnabled())
				logger.info(String.format("Send mail message to %s successfully, content:{}", to, content));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to send email", e);
		}
	}
}

