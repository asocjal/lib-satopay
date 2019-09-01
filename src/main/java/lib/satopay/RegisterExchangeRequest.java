package lib.satopay;

import bittech.lib.protocol.Request;
import bittech.lib.utils.Require;

public class RegisterExchangeRequest implements Request {
	
	public Exchange exchange;
	
	public RegisterExchangeRequest() {
		
	}
			
	public RegisterExchangeRequest(Exchange exchange) {
		this.exchange = Require.notNull(exchange, "exchange");
	}

}
