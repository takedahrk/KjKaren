package jp.co.KjKaren.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void googleAuthServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void receiveMailServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void sendMailServer(String input, String progress, String health,
			String leave, String detail, AsyncCallback<String> callback)
			throws IllegalArgumentException;
}
