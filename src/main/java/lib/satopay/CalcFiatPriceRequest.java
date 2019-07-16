package lib.satopay;

import bittech.lib.protocol.Request;
import bittech.lib.utils.Require;

public class CalcFiatPriceRequest implements Request {

	public String bankId;
	public int satoshis;

	public CalcFiatPriceRequest(final String bankName, final int satoshis) {
		this.bankId = Require.notNull(bankName, "bankName");
		this.satoshis = Require.inRange(satoshis, 0, 1000000, "satoshis");
	}
	

}
