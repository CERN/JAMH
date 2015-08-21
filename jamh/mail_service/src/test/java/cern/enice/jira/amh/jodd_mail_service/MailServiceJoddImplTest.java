package cern.enice.jira.amh.jodd_mail_service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;

import jodd.mail.EmailAddress;
import jodd.mail.EmailUtil;
import jodd.mail.ReceivedEmail;
import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.cm.ConfigurationException;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.jodd_mail_service.JoddMailService;

@RunWith(JUnitParamsRunner.class)
public class MailServiceJoddImplTest {
	protected JoddMailService msji;
	protected Configuration configuration;
	protected static ObjectMapper objectMapper = new ObjectMapper();

	public static String username;
	public static String password;
	public static String imapServer;
	public static String imapPort;
	public static String smtpServer;
	public static String smtpPort;
	
	

	@Before
	public void setup() {
		LogProvider logger = mock(LogProvider.class);
		MailServiceUtils msu = spy(new MailServiceUtils());
		msu.setLogger(logger);
		configuration = new Configuration();
		configuration.setMailServiceUtils(msu);
		msji = new JoddMailService();
		msji.setLogger(logger);
		msji.setConfiguration(configuration);
		msji.setMailServiceUtils(msu);
	}

	public static Object getObjectFromJsonFile(String filename) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonFileContent = IOUtils.toString(new FileInputStream(filename), "utf-8");
			return mapper.readValue(jsonFileContent, Object.class);
		} catch (IOException e) {
			System.out.println(e);
		}
		return null;
	}

	@Test
	@Ignore
	public void testConfigurationIsUpdated(Hashtable<String, String> config) {
		boolean failed = false;
		try {
			configuration.updated(config);
		} catch (ConfigurationException e) {
			System.out.println(e.getLocalizedMessage());
			failed = true;
		}

		assertFalse(failed);
	}

	@Parameters
	@Test
	public void testConfigurationUpdateFailed(Hashtable<String, String> config) {
		boolean failed = false;
		try {
			configuration.updated(config);
		} catch (ConfigurationException e) {
			failed = true;
		}

		assertTrue(failed);
	}

	private Object parametersForTestConfigurationUpdateFailed() throws IOException {
		String[] configParameters = new String[] { "imapServer", "imapPort", "imapUsername",
				"imapPassword", "smtpServer", "smtpPort", "smtpUsername", "smtpPassword",
				"pathToAttachments" };
		
		Hashtable<String, String>[] configs = new Hashtable[9];
		for (int i = 0; i < 9; i++) {
			Hashtable<String, String> config = new Hashtable<String, String>();
			config.put("imapServer", "imapServer");
			config.put("imapPort", "imapPort");
			config.put("imapUsername", "username");
			config.put("imapPassword", "password");
			config.put("smtpServer", "smtpServer");
			config.put("smtpPort", "smtpPort");
			config.put("smtpUsername", "username");
			config.put("smtpPassword", "password");
			config.put("pathToAttachments", "/path/to/attachments");
			config.remove(configParameters[i]);
			configs[i] = config;
		}
		return $($(configs[0]), $(configs[1]), $(configs[2]), $(configs[3]), $(configs[4]),
				$(configs[5]), $(configs[6]), $(configs[7]), $(configs[8]));
	}

	@Parameters
	@Test
	public void testSuccessfulConvert(ReceivedEmail receivedEmail, EMail expectedEmail) {
		MailServiceUtils msu = msji.getMailServiceUtils();
		doCallRealMethod().when(msu).getBestBodyPart(receivedEmail);
		EMail email = msji.convertToEMail(receivedEmail);
		assertThat(email.getTo(), is(expectedEmail.getTo()));
		assertThat(email.getFrom(), is(expectedEmail.getFrom()));
		assertThat(email.getSubject(), is(expectedEmail.getSubject()));
		assertThat(email.getBody(), is(expectedEmail.getBody()));
		assertThat(email.getCc(), is(expectedEmail.getCc()));
		assertThat(email.getBcc(), is(expectedEmail.getBcc()));
		assertThat(email.getReplyTo(), is(expectedEmail.getReplyTo()));
		assertThat(email.getAttachments(), is(expectedEmail.getAttachments()));
	}

	private Object parametersForTestSuccessfulConvert() throws FileNotFoundException,
			MessagingException {
		setup();
		List<EMailAddress> to, cc, bcc, replyTo;
		List<String> attachments;

		ReceivedEmail[] emails = new ReceivedEmail[2];
		emails[0] = EmailUtil.parseEML(new File("src/test/resources/test-email-1.eml"));
		emails[1] = EmailUtil.parseEML(new File("src/test/resources/test-email-3.eml"));
		
		EMailAddress from = msji.parseEmailAddress("Vladimir Vasilyev <djsuprin@gmail.com>");
		to = new ArrayList<EMailAddress>();
		to.add(msji.parseEmailAddress("icecontrols.test@cern.ch"));
		cc = new ArrayList<EMailAddress>();
		bcc = new ArrayList<EMailAddress>();
		replyTo = new ArrayList<EMailAddress>();

		EMail[] expectedEmails = new EMail[3];
		expectedEmails[0] = createExpectedEmail("NT-24 #issuetype=new feature #priority=minor",
				"#reporter=vvasilye\r\nMy comment body 2\r\n",
				from, to, cc, bcc, replyTo, null);
		
		expectedEmails[1] = createExpectedEmail("NT-7 #DELETE", 
				"tra la la\r\n",
				from, to, cc, bcc, replyTo, null);

		return $(
				$(emails[0], expectedEmails[0]), 
				$(emails[1], expectedEmails[1]));
	}
	
	private EMail createExpectedEmail(String subject, String body, EMailAddress from, 
			List<EMailAddress> to, List<EMailAddress> cc, List<EMailAddress> bcc, 
			List<EMailAddress> replyTo, List<String> attachments) {
		EMail expectedEmail = new EMail();
		expectedEmail.setSubject(subject);
		expectedEmail.setFrom(from);
		expectedEmail.setTo(to);
		expectedEmail.setCc(cc);
		expectedEmail.setBcc(bcc);
		expectedEmail.setReplyTo(replyTo);
		expectedEmail.setAttachments(attachments);
		expectedEmail.setBody(body);
		return expectedEmail;
	}
}
