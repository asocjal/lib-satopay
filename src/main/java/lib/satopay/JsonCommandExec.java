package lib.satopay;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import bittech.lib.protocol.Command;
import bittech.lib.protocol.ErrorResponse;
import bittech.lib.protocol.ListenersManager;
import bittech.lib.protocol.Message;
import bittech.lib.protocol.Request;
import bittech.lib.protocol.Response;
import bittech.lib.utils.Require;
import bittech.lib.utils.exceptions.StoredException;
import bittech.lib.utils.json.JsonBuilder;

public class JsonCommandExec {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonCommandExec.class);

	private final ListenersManager listenersManager;
	private Map<Long, Command<?, ?>> sentCommands = new ConcurrentHashMap<Long, Command<?, ?>>();

	public JsonCommandExec(final ListenersManager listenersManager) {
		this.listenersManager =Require.notNull(listenersManager, "listenersManager");
	}

	private synchronized String toJson(Message messageWithId) throws StoredException {
		try {

			Require.notNull(messageWithId, "messageWithId");
			Gson json = JsonBuilder.build();
			String output = json.toJson(messageWithId);
			output = output.replace("\n", "").replace("\r", "");

			if (JsonBuilder.isValid(output) == false) {
				throw new StoredException("Invalid json output creatd: " + output, null);
			}

			return output;

		} catch (Exception ex) {
			throw new StoredException("Cannot convert object to Json", ex);
		}
	}

	private String respError(long requestId, String commandName, ErrorResponse errorResponse) {
		return toJson(new Message(requestId, commandName, errorResponse));
	}

	@SuppressWarnings("unchecked")
	private void handleResponse(Message message) {
		Require.notNull(message, "message");
		if ("response".equals(message.type) == false) {
			throw new StoredException(
					"Cannot handle response. Given message is not response type, but it is: " + message.type, null);
		}

		@SuppressWarnings("rawtypes")
		Command command = (Command) sentCommands.get(message.id);

		if (command == null) {
			throw new StoredException("Cannot find command with id: " + message.id, null);
		}

		synchronized (command) {
			command.response = (Response) JsonBuilder.build().fromJson(message.body.toString(),
					command.getResponseClass());

			command.notify();
		}
	}

	@SuppressWarnings("unchecked")
	private void handleError(Message message) {
		Require.notNull(message, "message");
		if ("error".equals(message.type) == false) {
			throw new StoredException("Cannot handle error. Given message is not error type but it is: " + message.type,
					null);
		}
		@SuppressWarnings("rawtypes")
		Command command = (Command) sentCommands.get(message.id);

		if (command == null) {
			throw new StoredException("Cannot find command with id: " + message.id, null);
		}

		synchronized (command) {
			command.error = JsonBuilder.build().fromJson(message.body.toString(), ErrorResponse.class);
			command.notify();
		}
	}

	private String handleRequest(Message message) {
		Require.notNull(message, "message");
		if ("request".equals(message.type) == false) {
			throw new StoredException(
					"Cannot handle request. Given message is not request type but it is: " + message.type, null);
		}

		try {
			Method method;
			Class<?> clazzCmd;

			try {
				clazzCmd = Class.forName(message.name);
			} catch (Exception ex) {
				StoredException sex = new StoredException("Unknown command: " + message.name, ex);
				return respError(message.id, message.name, new ErrorResponse(sex.getId(), sex.getMessage()));

			}

			try {
				method = clazzCmd.getMethod("createStub");
			} catch (Exception ex) {
				StoredException sex = new StoredException("Command " + message.name + " do not have createStub method",
						ex);
				LOGGER.error("-----------------------------------" + sex.getMessage());
				return respError(message.id, message.name, new ErrorResponse(sex.getId(), sex.getMessage()));
			}

			@SuppressWarnings("unchecked")
			Command<Request, Response> command = (Command<Request, Response>) method.invoke(null);
			command.setTimeout(message.timeout);

			Message response;

//			if (authenticated == false && ((Command<?, ?>) command instanceof IntroduceCommand == false)) {
//				LOGGER.debug(peerName
//						+ " -> Not authenticated for command not Introduce. Preparing 'You are not authenticated' error response.");
//				ErrorResponse err = new ErrorResponse(0, "Cannot execute command " + command.type
//						+ ". You are not authenticated. Call IntroduceCommand first");
//
//				LOGGER.debug(peerName + " -> Create new message with error to sent later on");
//				response = new Message(message.id, command.getClass().getCanonicalName(), err);
//			} else {
			try {
				Request r = (Request) JsonBuilder.build().fromJson(message.body.toString(), command.getRequestClass());
				command.request = r;

				long time = (new Date()).getTime();

				Thread th = new Thread(() -> {
					try {
						listenersManager.commandReceived("peerName", command);

					} catch (Exception e) {
						StoredException sex = new StoredException("Cannot process command", e);
						command.response = null;
						command.error = new ErrorResponse(sex);
					}
				});

				th.start();
				th.join(command.getTimeout());

				long timeLambda = (new Date()).getTime() - time;
				if (timeLambda > command.getTimeout()) {

					th.interrupt();

					throw new Exception(
							"Command execution timeout (took long than " + command.getTimeout() + " milisec");
				}
			} catch (Exception ex) {
				StoredException sex = new StoredException("listener.commandReceived thrown: " + ex.getMessage(), ex);
				command.error = new ErrorResponse(sex);
			}

			if (command.response == null && command.error == null) {

				command.error = new ErrorResponse(new StoredException("Cannot process command", new Exception("Listener didn't added response or error to command")));
			}

			if (command.response != null && command.error != null) { // TODO:
																		// Warning
																		// only?
				LOGGER.warn(
						"Listener added both response and error to command: " + JsonBuilder.build().toJson(command));
			}

			if (command.response != null) {
				response = new Message(message.id, command.getClass().getCanonicalName(), command.response);
			} else if (command.error != null) {
				response = new Message(message.id, command.getClass().getCanonicalName(), command.error);
			} else {

				StoredException sex = new StoredException(
						"Internal error. Listener didn't add response or error to command", null);
				command.error = new ErrorResponse(sex.getId(), sex.getMessage());

				response = new Message(message.id, command.getClass().getCanonicalName(), command.error);
			}

//			}
			return toJson(response);
		} catch (Exception ex) {
			throw new StoredException("Handle request faild", ex); // TODO: Is this OK?
		}
	}

	public String onReceived(String input) {

		try {

			if (JsonBuilder.isValid(input) == false) {
				return respError(0, "Unknown", new ErrorResponse(0, "This is not valid json"));
			}
			Message message = null;
			try {
				message = JsonBuilder.build().fromJson(input, Message.class);
				if (message == null) {
					return respError(0, "Unknown", new ErrorResponse(0, "Json not match protocol standard"));
				}
			} catch (Exception ex) {
				return respError(0, "Unknown", new ErrorResponse(0, "Json not match protocol standard"));
			}

			if ("response".equals(message.type)) {
				handleResponse(message);
				return null;
			} else if ("error".equals(message.type)) {
				handleError(message);
				return null;
			} else if ("request".equals(message.type)) {
				return handleRequest(message);
			} else {
				StoredException sex = new StoredException("Unknown object type received: " + message
						+ ". Only Commands and responses and errors are allowed", null);

				Message err = new Message(message.id, message.name, new ErrorResponse(sex.getId(), sex.getMessage()));
				return toJson(err);

			}

		} catch (Exception ex) {
			throw new StoredException("Cannot process message: " + input, ex);
		}

	}

}
