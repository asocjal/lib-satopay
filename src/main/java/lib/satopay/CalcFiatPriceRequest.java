package lib.satopay;

import bittech.lib.protocol.Request;
import bittech.lib.utils.Btc;
import bittech.lib.utils.Require;

public class CalcFiatPriceRequest implements Request {

	public final String calculationId;
	public Btc amount;

	public CalcFiatPriceRequest(final String calculationId, final Btc amount) {
		this.calculationId = Require.notNull(calculationId, "calculationId");
		this.amount = Require.notNull(amount, "amount");
	}
	

}
