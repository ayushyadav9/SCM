package com.contact.manager.helper;


import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendMail(String subject,String message,String to) {
		boolean f = false;
		
		String host = "smtp.gmail.com";
		Properties properties = System.getProperties();
		
		//setting imp info to properties
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", 465);
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//step 1: get session object
		Session session = Session.getInstance(properties,new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("rahuladwe4@gmail.com", "dkklfappdhgbvhda");
			}
		});
//		session.setDebug(true);
		
		//step 2: compose the message
		MimeMessage m = new MimeMessage(session);
		
		try {
			//from email
			m.setFrom("rahuladwe4@gmail.com");
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding content
			m.setText(message);
			
			//send
			Transport.send(m);
			f=true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
		
	}
}
