package lib.satopay;

import bittech.lib.protocol.Command;

public class CalcFiatPriceCommand extends Command<CalcFiatPriceRequest, CalcFiatPriceResponse> {

	public static CalcFiatPriceCommand createStub() {
		return new CalcFiatPriceCommand("", 1000);
	}
	
	public CalcFiatPriceCommand(final String bankName, final int satoshis) {
		this.request = new CalcFiatPriceRequest(bankName, satoshis);
	}

}
