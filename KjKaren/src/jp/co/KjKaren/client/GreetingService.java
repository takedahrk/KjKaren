package jp.co.KjKaren.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	String greetServer(String input) throws IllegalArgumentException;

	String googleAuthServer(String input) throws IllegalArgumentException;

	String receiveMailServer(String input) throws IllegalArgumentException;

	String sendMailServer(String input, String progress, String health,
			String leave, String detail) throws IllegalArgumentException;
}
