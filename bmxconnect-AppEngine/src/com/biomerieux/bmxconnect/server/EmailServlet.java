package com.biomerieux.bmxconnect.server;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EmailServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(EmailServlet.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Properties props = new Properties();
		
		Session email = Session.getDefaultInstance(props, null);

		try {
			MimeMessage emailMessage = new MimeMessage(email, req.getInputStream());
			String subject = emailMessage.getSubject();
//TODO: Get the result date/time from the email message
			Date messageDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
//			String body = getText(emailMessage);
			Address[] addresses = emailMessage.getAllRecipients();
			String targetAccount = "unknown";
			for (Address add : addresses) {
				if (add.toString().contains("bmxconnectdev")) {
					String emailAddress = add.toString();
					int startIndex = emailAddress.indexOf('<');
					int atIndex = emailAddress.indexOf('@');
					if (startIndex > 0 && atIndex > 0) {
						targetAccount = emailAddress.substring(startIndex+1, atIndex);
					}
					break;
				}
			}
			targetAccount += "@gmail.com";
			
		    log.info("sending to: " + targetAccount + ", message: " + subject);
	        MessageSenderService.sendMessage(getServletContext(), targetAccount, subject, messageDateTime);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


    private boolean textIsHtml = false;

    
/** * Return the primary text content of the message. */

    private String getText(Part p) throws
                MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
}
