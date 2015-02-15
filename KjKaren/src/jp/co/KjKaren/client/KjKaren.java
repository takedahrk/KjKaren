package jp.co.KjKaren.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class KjKaren implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	// 表示用エラー格納用ラベル
	final Label errorLabel = new Label();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// ログインリンク格納用HTML
		final HTML loginLinkHtml = new HTML();
		// トークン格納用hidden
		final Hidden tokenHidden = new Hidden();

		// 進捗
		final HorizontalPanel progressPanel = new HorizontalPanel();
		final ListBox progressListBox = new ListBox();
		progressListBox.addItem("オンスケ");
		progressListBox.addItem("遅延");

		progressPanel.add(progressListBox);
		RootPanel.get("progressContainer").add(progressPanel);

		// 体調
		final HorizontalPanel healthPanel = new HorizontalPanel();
		final ListBox healthListBox = new ListBox();
		healthListBox.addItem("良好");
		healthListBox.addItem("普通");
		healthListBox.addItem("不調");

		healthPanel.add(healthListBox);
		RootPanel.get("healthContainer").add(healthPanel);

		// 退勤時間
		// TODO 長いので別メソッドに出す
		final HorizontalPanel leaveListPanel = new HorizontalPanel();
		final ListBox leaveHourListBox = new ListBox();
		final ListBox leaveMinListBox = new ListBox();
		leaveHourListBox.addItem("00");
		leaveHourListBox.addItem("01");
		leaveHourListBox.addItem("02");
		leaveHourListBox.addItem("03");
		leaveHourListBox.addItem("04");
		leaveHourListBox.addItem("05");
		leaveHourListBox.addItem("06");
		leaveHourListBox.addItem("07");
		leaveHourListBox.addItem("08");
		leaveHourListBox.addItem("09");
		leaveHourListBox.addItem("10");
		leaveHourListBox.addItem("11");
		leaveHourListBox.addItem("12");
		leaveHourListBox.addItem("13");
		leaveHourListBox.addItem("14");
		leaveHourListBox.addItem("15");
		leaveHourListBox.addItem("16");
		leaveHourListBox.addItem("17");
		leaveHourListBox.addItem("18");
		leaveHourListBox.addItem("19");
		leaveHourListBox.addItem("20");
		leaveHourListBox.addItem("21");
		leaveHourListBox.addItem("22");
		leaveHourListBox.addItem("23");
		leaveMinListBox.addItem("00");
		leaveMinListBox.addItem("30");
		leaveHourListBox.setSelectedIndex(18);
		leaveMinListBox.setSelectedIndex(0);

		leaveListPanel.add(leaveHourListBox);
		leaveListPanel.add(leaveMinListBox);
		RootPanel.get("leaveHourContainer").add(leaveListPanel);

		// 詳細
		final TextArea detailTextArea = new TextArea();
		detailTextArea.setCharacterWidth(60);
		detailTextArea.setVisibleLines(5);
		detailTextArea.getElement().setPropertyString("placeholder",
				"具体的なタスク名や小ネタを入力(任意)");
		RootPanel.get("detailContainer").add(detailTextArea);

		// 進捗報告フォーム
		final Button reportButton = new Button("送信");
		RootPanel.get("reportContainer").add(reportButton);

		// ログインリンク格納
		greetingService.greetServer("", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				errorLabel.setText("ログインリンク取得エラー");
			}

			public void onSuccess(String result) {
				loginLinkHtml.setHTML(result);
			}
		});
		
		final PopupPanel loginPopup = new PopupPanel(false, true);
		loginPopup.setWidget(loginLinkHtml);
		loginPopup.getElement().setAttribute("id", "loginPopup");
		loginPopup.center();

		// リフレッシュトークン取得
		String code = com.google.gwt.user.client.Window.Location
				.getParameter("code");
		if (code != null) {
			greetingService.googleAuthServer(code, new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					errorLabel.setText("リフレッシュトークン取得エラー");
					loginPopup.hide();
				}

				public void onSuccess(String result) {
					tokenHidden.setValue(result);
					loadMailList(result);
					loginPopup.hide();
				}
			});
			RootPanel.get("tokenContainer").add(tokenHidden);
		}

		// 進捗報告ボタン用ハンドラー
		class ReportHandler implements ClickHandler {

			public void onClick(ClickEvent event) {
				sendMailDataToServer();
			}

			private void sendMailDataToServer() {
				// リフレッシュトークン取得
				String refreshToken = tokenHidden.getValue();
				// 進捗
				int progressIndex = progressListBox.getSelectedIndex();
				String progress = progressListBox.getValue(progressIndex);

				// 体調
				int healthIndex = healthListBox.getSelectedIndex();
				String health = healthListBox.getValue(healthIndex);

				// 退勤時間
				int hour = leaveHourListBox.getSelectedIndex();
				int min = leaveMinListBox.getSelectedIndex();
				String leave = leaveHourListBox.getValue(hour) + ":"
						+ leaveMinListBox.getValue(min);

				greetingService.sendMailServer(refreshToken, progress, health,
						leave, detailTextArea.getText(),
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								errorLabel.setText("進捗メール送信エラー");
							}

							public void onSuccess(String result) {
								loadMailList(result);
							}
						});
			}

		}

		// ボタンハンドラー追加
		ReportHandler reportHandler = new ReportHandler();
		reportButton.addClickHandler(reportHandler);

		// エラーラベル追加
		RootPanel.get("errorLabelContainer").add(errorLabel);

	}

	/**
	 * 進捗情報取得
	 * 
	 * @param refreshToken
	 *            リフレッシュトークン
	 */
	private void loadMailList(String refreshToken) {
		final HTML progressHtml = new HTML();
		// ローディングパネル
		final PopupPanel loadingPopup = new PopupPanel(false, true);
		final HTML loadingHtml = new HTML("Loading...");
		loadingPopup.setWidget(loadingHtml);
		loadingPopup.getElement().setAttribute("id", "loadingPopup");
		loadingPopup.center();

		greetingService.receiveMailServer(refreshToken,
				new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						errorLabel.setText("進捗メール受信エラー");
						loadingPopup.hide();
					}

					public void onSuccess(String result) {
						progressHtml.setHTML(result);
						loadingPopup.hide();
					}
				});
		RootPanel.get("mainContainer").clear();
		RootPanel.get("mainContainer").add(progressHtml);
	}
}
