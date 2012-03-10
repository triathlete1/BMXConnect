package com.biomerieux.bmxconnect.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EmailServletTest {
	
	private static final String APPSPOTMAIL_DOMAIN = "appspotmail";
	private static final String GMAIL_SUFFIX = "@gmail.com";
	
	@Mock Logger logger;
	@Mock MimeMessage message;
	@Mock Address address;
	
	EmailServlet servlet;
	
	@Before
	public void setUp() throws Exception {
		servlet = new EmailServlet();
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetTargetAccountFromRecipients_correctFormatOneAddress() throws Exception {
		String testAccount1 = "testAccount";
		String testEmail1 = testAccount1 + "@" + APPSPOTMAIL_DOMAIN + ".com";
		setupRecipientEmailAddresses(testAccount1, testEmail1);
		
		String targetAccount = servlet.getTargetAccountFromRecipients(message);
		
		assertNotNull(targetAccount);
		assertEquals(testAccount1 + GMAIL_SUFFIX, targetAccount);
	}
	
	@Test
	public void testGetTargetAccountFromRecipients_correctFormatManyAddresses() throws Exception {
		String testAccount1 = "testAccount";
		String testEmail1 = "grover@sesame-street.com";
		String testEmail2 = testAccount1 + "@" + APPSPOTMAIL_DOMAIN + ".com";
		String testEmail3 = "elmo@" + APPSPOTMAIL_DOMAIN + ".com";
		setupRecipientEmailAddresses(testAccount1, testEmail1, testEmail2, testEmail3);
		
		String targetAccount = servlet.getTargetAccountFromRecipients(message);
		
		assertNotNull(targetAccount);
		assertEquals(testAccount1 + GMAIL_SUFFIX, targetAccount);
	}
	
	@Test
	public void testGetTargetAccountFromRecipients_correctFormatWithAngleBrackets() throws Exception {
		String testAccount1 = "testAccount";
		String testEmail1 = "Test User <" + testAccount1 + "@" + APPSPOTMAIL_DOMAIN + ".com>";
		String testEmail2 = "elmo@" + APPSPOTMAIL_DOMAIN + ".com";
		setupRecipientEmailAddresses(testAccount1, testEmail1, testEmail2);
		
		String targetAccount = servlet.getTargetAccountFromRecipients(message);
		
		assertNotNull(targetAccount);
		assertEquals(testAccount1 + GMAIL_SUFFIX, targetAccount);
	}
	
	@Test
	public void testGetTargetAccountFromRecipients_correctFormatWithMultipleAngleBrackets() throws Exception {
		String testAccount1 = "testAccount";
		String testEmail1 = "<Test User> <" + testAccount1 + "@" + APPSPOTMAIL_DOMAIN + ".com>";
		setupRecipientEmailAddresses(testAccount1, testEmail1);
		
		String targetAccount = servlet.getTargetAccountFromRecipients(message);
		
		assertNotNull(targetAccount);
		assertEquals(testAccount1 + GMAIL_SUFFIX, targetAccount);
	}

	@Test
	public void testGetTargetAccountFromRecipients_wrongDomain() throws Exception {
		String testAccount1 = "testAccount";
		String testEmail1 = testAccount1 + "@abcxyz123";
		String testEmail2 = testAccount1 + "@gmail.com";
		setupRecipientEmailAddresses(testAccount1, testEmail1, testEmail2);
		
		String targetAccount = servlet.getTargetAccountFromRecipients(message);
		
		assertNull(targetAccount);
	}

	////////////////////////////////////////////////////////////////////////
	
	private void setupRecipientEmailAddresses(String testAccount1, String... emails) throws MessagingException {
		List<Address> addressList = new ArrayList<Address>();
		for (String email : emails) {
			TestAddress address = new TestAddress();
			address.addressString = email;
			addressList.add(address);
		}
		Address[] addresses = addressList.toArray(new Address[0]);
		when(message.getAllRecipients()).thenReturn(addresses);
	}
	
	public class TestAddress extends Address {
		private static final long serialVersionUID = 1L;
		String addressString;
		
		@Override
		public boolean equals(Object arg0) {
			return false;
		}
		@Override
		public String getType() {
			return null;
		}
		@Override
		public String toString() {
			return addressString;
		}
	}
}
