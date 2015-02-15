package jp.co.KjKaren.server;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.mail.internet.MimeMessage;

import jp.co.KjKaren.auth.GoogleAuth;
import jp.co.KjKaren.client.GreetingService;
import jp.co.KjKaren.html.HtmlBuilder;
import jp.co.KjKaren.mail.GoogleMail;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.gmail.Gmail;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	/**
	 * メールにアクセスするためのオブジェクト取得
	 * 
	 * @param input あとで消す
	 * @throws IllegalArgumentException
	 */
	public String greetServer(String input) throws IllegalArgumentException {
		GoogleAuth auth = new GoogleAuth();
		String url = "";
		String html = "";

		try {
			url = auth.getGoogleOAuthURL();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		// 認証ダイアログ用HTML
		html = "<span class='glyphicon glyphicon-send' style='font-size: 10rem;color: #f96145;'></span>"
				+ "<h2>KjKaren</h2><h4>-おきがる進捗報告-</h4>"
				+ "<div class='box'>"
				+ "<a class='btn btn-danger' href="
				+ url
				+ ">Googleアカウントでログイン</a></div>";

		return html;
	}

	/**
	 * リフレッシュトークン取得
	 * 
	 * @param input 認証コード
	 */
	public String googleAuthServer(String input) {
		// リダイレクト後のレスポンス取得
		String code = input;

		// Gmail認証用クラス
		GoogleAuth auth = new GoogleAuth();

		// Gmailオブジェクト取得
		GoogleTokenResponse response = new GoogleTokenResponse();
		try {
			response = auth.getGoogleResponse(code);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response.getRefreshToken();
	}

	/**
	 * 進捗情報取得
	 * 
	 * @param input リフレッシュトークン
	 */
	public String receiveMailServer(String input) {
		// リダイレクト後のレスポンス取得
		String refreshToken = input;
		// HTMLコード
		String html = "";

		// Gmail認証用クラス
		GoogleAuth auth = new GoogleAuth();
		// メール情報取得用クラス
		GoogleMail mail = new GoogleMail();
		// 進捗情報取得用クラス
		HtmlBuilder builder = new HtmlBuilder();

		try {
			// Gmailオブジェクト取得
			GoogleCredential credential = auth
					.getGoogleCredential(refreshToken);
			Gmail gmail = auth.getGmailClient(credential);

			// メッセージリスト取得
			List<MimeMessage> messages = mail.listMessagesMatchingQuery(gmail,
					"subject:[KjKaren]");

			// 進捗リスト取得
			List<String> progresses = builder.listProgressesHtml(messages);

			// HTML取得
			for (String progress : progresses) {
				html += progress;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return html;
	}

	/**
	 * メール送信処理
	 * 
	 * @param input リフレッシュトークン
	 * @param porgress 進捗
	 * @param health 体調
	 * @param leave 退勤時間
	 * @param detail 詳細
	 */
	public String sendMailServer(String input, String progress, String health,
			String leave, String detail) {
		// リダイレクト後のレスポンス取得
		String refreshToken = input;

		// Gmail認証用クラス
		GoogleAuth auth = new GoogleAuth();
		// メール情報取得用クラス
		GoogleMail mail = new GoogleMail();

		try {
			// Gmailオブジェクト取得
			GoogleCredential credential = auth
					.getGoogleCredential(refreshToken);
			Gmail gmail = auth.getGmailClient(credential);

			// メッセージリスト取得
			mail.sendReportMail(gmail, progress, health, leave, detail);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return refreshToken;
	}

}
