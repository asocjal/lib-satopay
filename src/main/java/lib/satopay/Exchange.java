package lib.satopay;

import java.util.List;

public class Exchange {
	public String name;
	public String domain;
	public boolean active;
	public List<String> banksSupported;
	
	public boolean supports(String bankId) {
		return banksSupported.contains(bankId);
	}
}