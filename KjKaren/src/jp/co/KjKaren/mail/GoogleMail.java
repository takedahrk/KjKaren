package jp.co.KjKaren.mail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GoogleMail {

	// authorized user.
	private static final String USER = "me";

	/**
	 * List all Messages of the user's mailbox matching the query.
	 *
	 * @param service
	 *            Authorized Gmail API instance.
	 * @param query
	 *            String used to filter the Messages listed.
	 * @throws IOException
	 * @throws MessagingException
	 */
	public List<MimeMessage> listMessagesMatchingQuery(Gmail service,
			String query) throws IOException, MessagingException {
		ListMessagesResponse response = service.users().messages().list(USER)
				.setQ(query).execute();

		// Messageオブジェクトリストを取得
		List<Message> messages = new ArrayList<Message>();
		if(response.getMessages() != null) {
			messages.addAll(response.getMessages());
		}

		// MimeMessageオブジェクトに変換し、リストにつめて取得
		List<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
		for (Message message : messages) {
			mimeMessages.add(getMessage(service, USER, message.getId()));
		}

		return mimeMessages;
	}

	/**
	 * Get Message with given ID.
	 *
	 * @param service
	 *            Authorized Gmail API instance.
	 * @param userId
	 *            User's email address. The special value "me" can be used to
	 *            indicate the authenticated user.
	 * @param messageId
	 *            ID of Message to retrieve.
	 * @return Message Retrieved Message.
	 * @throws IOException
	 * @throws MessagingException
	 */
	public MimeMessage getMessage(Gmail service, String userId, String messageId)
			throws IOException, MessagingException {
		Message message = service.users().messages().get(userId, messageId)
				.setFormat("raw").execute();
		byte[] emailBytes = Base64.decodeBase64(message.getRaw());

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(
				emailBytes));

		return email;
	}

	// 進捗送信
	public void sendReportMail(Gmail service, String progress, String health,
			String leave, String detail) throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage mimeMessage = new MimeMessage(session);

		List<String> addressList = new ArrayList<String>();
		addressList.addAll(getAddressList());

		for (String address : addressList) {
			mimeMessage.addRecipients(javax.mail.Message.RecipientType.TO,
					InternetAddress.parse(address));
		}

		mimeMessage.setSubject("[KjKaren]");
		mimeMessage.setText(progress + "," + health + "," + leave + ","
				+ detail);
		mimeMessage.setHeader("Content-Transfer-Encoding", "7bit");

		sendMessage(service, USER, mimeMessage);

	}

	// メール送信
	public void sendMessage(Gmail service, String userId, MimeMessage email)
			throws MessagingException, IOException {
		Message message = createMessageWithEmail(email);
		message = service.users().messages().send(userId, message).execute();

	}

	// Eメール作成
	public static Message createMessageWithEmail(MimeMessage email)
			throws MessagingException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		email.writeTo(baos);
		String encodedEmail = Base64.encodeBase64URLSafeString(baos
				.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);

		return message;
	}

	// 宛先アドレス取得
	public List<String> getAddressList() throws IOException {
		List<String> addressList = new ArrayList<String>();
		String filePath = GoogleMail.class.getResource("address_list.txt")
				.getPath();
		File file = new File(URLDecoder.decode(filePath, "UTF-8"));
		BufferedReader br = new BufferedReader(new FileReader(file));

		String str = br.readLine();
		while (str != null) {
			addressList.add(str);
			str = br.readLine();
		}
		br.close();

		return addressList;
	}

}
