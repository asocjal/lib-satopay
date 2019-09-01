package lib.satopay;

import bittech.lib.protocol.Command;
import bittech.lib.protocol.common.NoDataResponse;

public class RegisterExchangeCommand extends Command<RegisterExchangeRequest, NoDataResponse> {

	public static RegisterExchangeCommand createStub() {
		return new RegisterExchangeCommand(new Exchange());
	}
	
	public RegisterExchangeCommand(Exchange exchange) {
		this.request = new RegisterExchangeRequest(exchange);
	}

}
