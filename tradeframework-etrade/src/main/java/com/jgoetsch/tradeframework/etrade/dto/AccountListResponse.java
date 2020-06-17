package com.jgoetsch.tradeframework.etrade.dto;

import java.util.List;
import java.util.Map;

import com.google.api.client.util.Key;

public class AccountListResponse {
	public static class Accounts {
		@Key("Account") public List<Map<String, Object>> account;		
	}
	@Key("Accounts") public Accounts accounts;
}
