package za.co.telkom.bpmn.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import za.co.telkom.bpmn.handler.ApiException;


public class SendEmail
{
	private static final String SMTP_SERVER = "165.143.128.26";

	public String mailSender(String mailBody, String customerEmail)
	{
		// Mention the Recipient's email address
	    String to = customerEmail;

	    // Mention the Sender's email address
	    String from = "Noreply@telkom.co.za";

	    // Get system properties
	    Properties properties = System.getProperties();

	    // Setup mail server
		properties.put("mail.smtp.host", "165.143.128.26");
	    properties.put("mail.smtp.port", "25");

	    // Get the Session object and pass username and password
	    Session session = Session.getInstance(properties);

	    try {
	        // Create a default MimeMessage object.
	        MimeMessage message = new MimeMessage(session);

	        // Set From: header field of the header.
	        message.setFrom(new InternetAddress(from));

	        // Set To: header field of the header.
	        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	        // Set Subject: header field
	        //message.setSubject("This is the Subject Line! Okay");
	        message.setSubject("SmartBroadband Wireless is replacing DSL");

	        // Now set the actual message
	        //String msg = "<div style=\"color:red;\">BRIDGEYE</div>";
	        String msg = mailBody;
	        //message.setContent(msg, "text/html; charset=UTF-8");
	        message.setContent(msg, "text/html; charset=ISO-8859-1");
	        message.setSentDate(new Date());

	        System.out.println("sending...");
	        // Send message
	        Transport.send(message);
	        //System.out.println("Sent message successfully to " + customerEmail);



	    } catch (MessagingException mex) {
	        mex.printStackTrace();
	    }

	    return "Sent message successfully to " + customerEmail;
	}


	public String mailSender(String mailBody, String customerEmail, String subject)
	{
		// Mention the Recipient's email address
	    String to = customerEmail;

	    // Mention the Sender's email address
	    String from = "Noreply@telkom.co.za";

		processEmail(mailBody, from, subject, to);

	    return "Sent message successfully to " + customerEmail;
	}

	public String mailSenderName(String mailBody, String customerEmail, String emailSubject, String emailName)
	{
		// Mention the Recipient's email address
		String to = customerEmail;

		// Mention the Sender's email address
		String from = emailName;

		processEmail(mailBody, from, emailSubject, to);

		return "Sent message successfully to " + customerEmail;
	}

	public String mailSender(String mailBody, List<String> receivers, String subject) {
		// Mention the Sender's email address
		String from = "Noreply@telkom.co.za";

		for (String to : receivers) {
			processEmail(mailBody, from, subject, to);
		}

		return "Sent message successfully to " + String.join(", ", receivers);
	}


	public String mailSender(String mailBody, String customerEmail, String subject, String fileName) throws Exception
	{
		// Mention the Recipient's email address
	    String to = customerEmail;

	    // Mention the Sender's email address
	    String from = "Noreply@telkom.co.za";

	    // Get system properties
	  //Get the session object
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", SMTP_SERVER);
        Session session = Session.getDefaultInstance(properties);



	    try {
	        // Create a default MimeMessage and Multipart object.
	        MimeMessage message = new MimeMessage(session);
	    	//Message message = new MimeMessage(session);
	        Multipart multipart = new MimeMultipart();
	        // Set From: header field of the header.
	        message.setFrom(new InternetAddress(from));

	        // Set To: header field of the header.
	        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	        // Set Subject: header field
	        message.setSubject("This is the Subject Line! Okay");
	        message.setSubject(subject);
	        message.setSentDate(new Date());

	        // Now set the actual message
	        String msg = mailBody;

	        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			if (fileName!=null) {
				attachmentBodyPart.attachFile(fileName);
				multipart.addBodyPart(attachmentBodyPart);
			}

	        MimeBodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(msg)));
	        messageBodyPart.setContent(msg, "text/html");

		    // message.setSentDate(new Date());

	        // creates multi-part

	        multipart.addBodyPart(messageBodyPart);

	        // sets the multipart as message's content
	        //message.setContent(msg, "text/html");

	        //messageBodyPart.attachFile(fileName);
	        //messageBodyPart.setContent(msg, "text/html");

	        //multipart.addBodyPart(messageBodyPart);
	        message.setContent(multipart);

	        System.out.println("sending...");
	        // Send message
	        Transport.send(message);
	        //System.out.println("Sent message successfully to " + customerEmail);



	    } catch (MessagingException mex) {
	        mex.printStackTrace();
	        throw new ApiException(mex.getMessage());
	    }

	    return "Sent message successfully to " + customerEmail;
	}


	public String mailReceiver(String mailBody, List<String> recipientEmails, String subject, String fileName) throws Exception {
		// Mention the Sender's email address
		String from = "Noreply@telkom.co.za";

		// Get system properties
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", SMTP_SERVER);
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage and Multipart object.
			MimeMessage message = new MimeMessage(session);
			Multipart multipart = new MimeMultipart();

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set Subject: header field
			message.setSubject(subject);
			message.setSentDate(new Date());

			// Now set the actual message
			String msg = mailBody;

			// Create a MimeBodyPart for the message body
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(msg)));
			messageBodyPart.setContent(msg, "text/html");
			multipart.addBodyPart(messageBodyPart);

			// Create a MimeBodyPart for the attachment
			if (fileName != null) {
				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				attachmentBodyPart.attachFile(fileName);
				multipart.addBodyPart(attachmentBodyPart);
			}

			// Set the content of the message as the multi-part
			message.setContent(multipart);

			// Add recipients to the message
			for (String recipientEmail : recipientEmails) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
			}

			// Send the message
			Transport.send(message);

			return "Sent message successfully to " + recipientEmails;
		} catch (MessagingException mex) {
			mex.printStackTrace();
			throw new ApiException(mex.getMessage());
		}
	}


	public String mailSenderWithFrom(String mailBody, String from, String customerEmail, String subject)
	{
		// Mention the Recipient's email address
		String to = customerEmail;
		processEmail(mailBody, from, subject, to);

		return "Sent message successfully to " + customerEmail;
	}

	private void processEmail(String mailBody, String from, String subject, String to) {
		// Get system properties
		//Get the session object
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", SMTP_SERVER);
		Session session = Session.getDefaultInstance(properties);


		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			//message.setSubject("This is the Subject Line! Okay");
			message.setSubject(subject);

			// Now set the actual message
			//String msg = "<div style=\"color:red;\">BRIDGEYE</div>";
			String msg = mailBody;
			message.setDataHandler(new DataHandler(new HTMLDataSource(msg)));
			message.setContent(msg, "text/html; charset=UTF-8");
			message.setSentDate(new Date());

			System.out.println("sending...");
			// Send message
			Transport.send(message);
			//System.out.println("Sent message successfully to " + customerEmail);



		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	public String mailSenderWithFromAndCC(String mailBody, String from, String customerEmail, String ccEmails,  String subject, String fileName) throws Exception
	{

		// Mention the Recipient's email address
		String to = customerEmail;

		// Get system properties
		//Get the session object
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", SMTP_SERVER);
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage and Multipart object.
			MimeMessage message = new MimeMessage(session);
			//Message message = new MimeMessage(session);
			Multipart multipart = new MimeMultipart();
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setRecipients(Message.RecipientType.CC, ccEmails);

			// Set Subject: header field
			message.setSubject("This is the Subject Line! Okay ");
			message.setSubject(subject);
			message.setSentDate(new Date());

			// Now set the actual message
			String msg = mailBody;

			MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			if (fileName!=null) {
				attachmentBodyPart.attachFile(fileName);
				multipart.addBodyPart(attachmentBodyPart);
			}

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(msg)));
			messageBodyPart.setContent(msg, "text/html");

			// creates multi-part

			multipart.addBodyPart(messageBodyPart);

			//multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);

			System.out.println("sending...");
			// Send message
			Transport.send(message);
		} catch (MessagingException mex) {
			mex.printStackTrace();
			throw new ApiException(mex.getMessage());
		}
		return "Sent message successfully to  " + customerEmail;
	}

	static class HTMLDataSource implements DataSource {

        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (html == null) throw new IOException("html message is null!");
            return new ByteArrayInputStream(html.getBytes());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        @Override
        public String getContentType() {
            return "text/html";
        }

        @Override
        public String getName() {
            return "HTMLDataSource";
        }
    }


}
