package lib.satopay;

import java.math.BigDecimal;
import java.util.Map;

import bittech.lib.protocol.Response;
import bittech.lib.utils.Require;

public class CalcFiatPriceResponse implements Response {

	public Map<String, BigDecimal> prices; // prices per bank
	
	public CalcFiatPriceResponse(final Map<String, BigDecimal> prices) {
		this.prices = Require.notNull(prices, "prices");
	}

}
