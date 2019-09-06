package lib.satopay;

import bittech.lib.protocol.Command;
import bittech.lib.utils.Btc;

public class CalcFiatPriceCommand extends Command<CalcFiatPriceRequest, CalcFiatPriceResponse> {

	public static CalcFiatPriceCommand createStub() {
		return new CalcFiatPriceCommand("", Btc.fromSat(10000));
	}
	
	public CalcFiatPriceCommand(final String calculationId, final Btc amount) {
		this.request = new CalcFiatPriceRequest(calculationId, amount);
	}

}
