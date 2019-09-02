package lib.satopay;

import bittech.lib.protocol.Request;
import bittech.lib.utils.Require;

public class CalcFiatPriceRequest implements Request {

	public final String calculationId;
	public int satoshis;

	public CalcFiatPriceRequest(final String calculationId, final int satoshis) {
		this.calculationId = Require.notNull(calculationId, "calculationId");
		this.satoshis = Require.inRange(satoshis, 0, 1000000, "satoshis");
	}
	

}
