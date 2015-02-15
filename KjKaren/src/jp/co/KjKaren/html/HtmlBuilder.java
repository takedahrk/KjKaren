package jp.co.KjKaren.html;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class HtmlBuilder {

	/**
	 * 進捗HTMLリスト作成
	 * 
	 * @throws Exception
	 */
	public List<String> listProgressesHtml(List<MimeMessage> mimeMessages)
			throws Exception {
		List<String> progresses = new ArrayList<String>();

		// mimeMessageから必要なデータを抜きとりHTML整形
		for (MimeMessage mimeMessage : mimeMessages) {
			progresses.add(progressHtml(mimeMessage));
		}

		return progresses;
	}

	/**
	 * 進捗HTMLデータ作成
	 * 
	 * @throws Exception
	 */
	public String progressHtml(MimeMessage mimeMessage) throws Exception {
		// 進捗HTMLデータ
		String html = "";

		// TODO:固定値0番目
		// Fromデータ
		InternetAddress addrFrom = (InternetAddress) mimeMessage.getFrom()[0];
		// 受信日時
		Date recievedDate = mimeMessage.getSentDate();
		// From:メールアドレス
		String address = addrFrom.getAddress();
		// メール本文
		String content = getContent(mimeMessage.getContent());

		// 進捗表示パーツ
		// TODO HTML外部ファイル化
		html = "<article class='row'>"
				+ "<div class='col-md-2 col-sm-2 hidden-xs'>"
				+ "<figure class='thumbnail'>"
				+ "<img class='img-responsive' src='./default.jpg' />"
				+ "</figure>" + "</div>" + "<div class='col-md-10 col-sm-10'>"
				+ "<div class='panel panel-default arrow left'>"
				+ "<div class='panel-body'>" + "<header class='text-left'>"
				+ "<div class='comment-user'><i class='fa fa-user'></i><b> "
				+ address + "</b></div>"
				+ "<time class='comment-date'><i class='fa fa-clock-o'></i> "
				+ recievedDate + "</time>" + "</header>"
				+ "<div class='comment-post'>" + "<p>" + contentHtml(content)
				+ "</p>" + "</div>" + "</div></div></div></article>";

		return html;
	}

	/**
	 * 本文データ取得
	 * 
	 * @throws Exception
	 */
	private static String getContent(Object content) throws Exception {
		String text = null;
		StringBuffer sb = new StringBuffer();

		if (content instanceof String) {
			sb.append((String) content);
		} else if (content instanceof Multipart) {
			Multipart mp = (Multipart) content;
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart bp = mp.getBodyPart(i);
				sb.append(getContent(bp.getContent()));
			}
		}

		text = sb.toString();
		return text;
	}

	/**
	 * 本文データHTMLデータ作成
	 */
	private static String contentHtml(String content) {
		String html = "";

		// 詳細本文
		String detail = "";
		// アコーディオン用一意値
		String uniqId = UUID.randomUUID().toString();

		// TODO null値考慮
		// TODO 改行コード考慮
		String[] contents = content.split(",");

		// TOOD 固定値
		if (contents.length == 4) {
			detail = contents[3].replace("\n", "<BR>");
		}

		// TODO 固定値
		String progressColor = ("遅延".equals(contents[0])) ? "color: #f96145;"
				: "";
		String healthColor = ("不調".equals(contents[1])) ? "color: #f96145;"
				: "";

		// HTMLデータ作成
		// TODO HTMLテンプレートの外部ファイル化
		html = "<div class='row'>"
				+ "<div class='col-xs-4 text-center'>"
				+ "<span class='glyphicon glyphicon-fire' style='font-size: 3rem;"
				+ progressColor
				+ "'></span>"
				+ "<p>進捗:"
				+ contents[0]
				+ "</p>"
				+ "</div>"
				+ "<div class='col-xs-4 text-center'>"
				+ "<span class='glyphicon glyphicon-heart' style='font-size: 3rem;"
				+ healthColor
				+ "'></span>"
				+ "<p>体調:"
				+ contents[1]
				+ "</p>"
				+ "</div>"
				+ "<div class='col-xs-4 text-center' >"
				+ "<span class='glyphicon glyphicon-time' style='font-size: 3rem;'></span>"
				+ "<p>退勤時間" + contents[2] + "</p>" + "</div>" + "</div>";

		// TODO 固定値
		if (!"".equals(detail)) {
			html += "<div id='collapse"
					+ uniqId
					+ "' class='panel-collapse collapse'>"
					+ detail
					+ "</div>"
					+ "<a data-toggle='collapse' data-parent='#accordion' href='#collapse"
					+ uniqId
					+ "'>"
					+ "<span class='glyphicon glyphicon-menu-hamburger'></span>"
					+ "詳細</a>";
		}

		return html;
	}
}
