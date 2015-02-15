package jp.co.KjKaren.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;

/**
 * Google ApisのAuth認証用Utils
 */

public class GoogleAuth {

	private Properties configuration = new Properties();
	private static HttpTransport HTTP_TRANSPORT;
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	// リダイレクトURL
	private String REDIRECT_URL;
	// アプリケーション名
	private String APPLICATION_NAME;

	/**
	 * アプリケーション名、リダイレクトURL取得
	 */
	public GoogleAuth() {
		try {
			String filePath = GoogleAuth.class.getResource("info.properties")
					.getPath();
			InputStream inputStream = new FileInputStream(new File(
					URLDecoder.decode(filePath, "UTF-8")));
			configuration.load(inputStream);

			REDIRECT_URL = configuration.getProperty("REDIRECT_URL");
			APPLICATION_NAME = configuration.getProperty("APPLICATION_NAME");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * スコープの設定
	 */
	public GoogleAuthorizationCodeFlow getFlow() throws IOException,
			GeneralSecurityException {

		HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		// スコープの設定
		Set<String> scopes = new HashSet<String>();
		scopes.add("https://www.googleapis.com/auth/gmail.modify");

		GoogleClientSecrets clientSecrets = getClientSecrets();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes)
				.setAccessType("offline").setApprovalPrompt("force").build();

		return flow;
	}

	/**
	 * client_secretファイルから設定値取得
	 */
	public GoogleClientSecrets getClientSecrets() throws IOException {
		String filePath = GoogleAuth.class.getResource("client_secret.json")
				.getPath();
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
				JSON_FACTORY,
				new FileReader(URLDecoder.decode(filePath, "UTF-8")));

		return clientSecrets;
	}

	/**
	 * 認証URL取得
	 */
	public String getGoogleOAuthURL() throws IOException,
			GeneralSecurityException {
		GoogleAuthorizationCodeFlow flow = getFlow();
		return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URL).build();
	}

	/**
	 * コールバック後、レスポンス取得
	 * 
	 * @param code Google認証コード
	 */
	public GoogleTokenResponse getGoogleResponse(String code)
			throws IOException, GeneralSecurityException {
		GoogleAuthorizationCodeFlow flow = getFlow();
		return flow.newTokenRequest(code).setRedirectUri(REDIRECT_URL)
				.execute();
	}

	/**
	 * レスポンスから認証情報取得
	 * 
	 * @param refresh_token リフレッシュトークン
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public GoogleCredential getGoogleCredential(String refresh_token)
			throws IOException, GeneralSecurityException {
		// GoogleCredential credential = new
		// GoogleCredential().setFromTokenResponse(response);

		if (HTTP_TRANSPORT == null) {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		}

		GoogleClientSecrets secrets = getClientSecrets();
		GoogleCredential credential = new GoogleCredential.Builder()
				.setClientSecrets(secrets.getDetails().getClientId(),
						secrets.getDetails().getClientSecret())
				.setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT)
				.build();
		credential.setRefreshToken(refresh_token);
		credential.refreshToken();

		return credential;
	}

	/**
	 * メールにアクセスするためのオブジェクト取得
	 * 
	 * @param credential Google証明情報
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public Gmail getGmailClient(GoogleCredential credential)
			throws GeneralSecurityException, IOException {
		if (HTTP_TRANSPORT == null) {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		}

		return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}
}