package com.jgoetsch.tradeframework.etrade;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentEtradeOAuthClient extends EtradeOAuthClient {
	private Logger log = LoggerFactory.getLogger(PersistentEtradeOAuthClient.class);

	private Path storePath = FileSystems.getDefault().getPath("oauth.properties");

	@Override
	public void executeAuthenticationFlow() {
		try {
			Properties oauthProps = new Properties();
			Reader propReader = Files.newBufferedReader(storePath);
			oauthProps.load(propReader);
			propReader.close();
			log.info("Found stored oauth token in {}", storePath);

			if (oauthProps.containsKey("token") && oauthProps.containsKey("tokenSecret")) {
				super.setAccessTokens(oauthProps.getProperty("token"), oauthProps.getProperty("tokenSecret"));
				renewAccessToken();
			}
			else
				throw new IOException("Required token values not present in " + storePath);
		}
		catch (Exception e) {
			if (e instanceof FileSystemException)
				log.info("Could not find stored oauth token, continuing with authentication flow: {}", e.toString());
			else if (e instanceof AuthenticationFailureException)
				log.info("Stored oauth token is invalid or expired, continuing with authentication flow...");
			else
				log.error("Failure using stored oauth token, continuing with authentication flow...", e);
			super.executeAuthenticationFlow();
		}
	}

	@Override
	protected void setAccessTokens(String token, String tokenSecret) {
		try {
			Properties oauthProps = new Properties();
			oauthProps.setProperty("token", token);
			oauthProps.setProperty("tokenSecret", tokenSecret);
			Writer propWriter = Files.newBufferedWriter(storePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			oauthProps.store(propWriter, null);
			propWriter.close();
		}
		catch (IOException e) {
			log.warn("Failed to write oauth token to file {}", storePath, e);
		}
		finally {
			super.setAccessTokens(token, tokenSecret);
		}
	}

	public void setStorePath(Path storePath) {
		this.storePath = storePath;
	}
}
