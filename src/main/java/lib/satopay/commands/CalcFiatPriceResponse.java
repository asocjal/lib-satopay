package lib.satopay.commands;

import java.math.BigDecimal;

import bittech.lib.protocol.Response;
import bittech.lib.utils.Require;

public class CalcFiatPriceResponse implements Response {

	public final BigDecimal price;
	public final String calculationId;
	
	public CalcFiatPriceResponse(final BigDecimal price, final String calculationId) {
		this.price = Require.notNull(price, "price");
		this.calculationId = Require.notNull(calculationId, "calculationId");
	}

}
