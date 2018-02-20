package org.forgerock.openam.vericlouds;

import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.json.*;
import java.security.MessageDigest;
import com.sun.identity.shared.debug.Debug;

//https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
//Please goto above url if you got "Illegal key size" exception.
public class CredVerify {
	String api_url;
	String api_key;
	String api_secret;
	// private final static Debug debug = Debug.getInstance("VeriClouds");

	public static boolean empty(final String s){
		return s == null || s.trim().isEmpty();
	}

	private static byte[] hexToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private static String sha(String password, String algorithm) throws Exception{
		byte byteHash[];
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		digest.update(password.getBytes("utf-8"));
		byteHash = digest.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteHash.length; i++) {
			sb.append(Integer.toString((byteHash[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString().toUpperCase();
	}

	private String decrypt(String encrypted) throws Exception{
		if (empty(encrypted) || encrypted.indexOf(':') <0){
			throw new Exception("VeriClouds.decrypt: encrypted is empty or of wrong format: " + encrypted);
		}
		String[] strs = encrypted.split(":");
		if (strs.length != 2){
			throw new Exception("VeriClouds.decrypt: encrypted is of wrong format: " + encrypted);
		}
		if (empty(strs[0])){
			throw new Exception("VeriClouds.decrypt: password is empty");
		}
		if (empty(strs[1])){
			throw new Exception("VeriClouds.decrypt: iv is empty");
		}

		IvParameterSpec iv = new IvParameterSpec(hexToByteArray(strs[1]));
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		byte[] key = hexToByteArray(api_secret);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
		return new String(cipher.doFinal(hexToByteArray(strs[0])));
	}

	public CredVerify(String api_url, String api_key, String api_secret) throws Exception{
		if (empty(api_url)){
			throw new Exception("VeriClouds::CredVerify api_url is not defined");
		}
		if (empty(api_key)){
			throw new Exception("VeriClouds::CredVerify api_key is not defined");
		}
		if (empty(api_secret)){
			throw new Exception("VeriClouds::CredVerify api_secret is not defined");
		}
		this.api_url = api_url;
		this.api_key = api_key;
		this.api_secret = api_secret;
	}

	public JSONObject callPasswordAPI(String passwordHash) throws Exception {
		String template = "mode=search_leaked_passwords_with_hash_segment&api_key=%s&api_secret=%s&hash_segment=%s&coverage=all_passwords";
		String params = String.format(template, api_key, api_secret, passwordHash);
		return callRestAPI(params);
	}

	public JSONObject callUserIdAPI(String userid, String userid_type) throws Exception {
		if (userid_type.equals("hashed_email")){
			userid = sha(userid, "SHA-256");
		}
		else if (userid_type.equals("auto_detect")){
			int indexOf = userid.indexOf('@');
			// must be "hello@world", not "@hello", "hello@" or "hello"
			if (indexOf > 0 && indexOf != userid.length() - 1){
				userid_type = "email";
			}
			else{
				userid_type = "username";
			}
		}

		String template = "mode=search_leaked_password_with_userid&api_key=%s&api_secret=%s&userid=%s&userid_type=%s";
		String params = String.format(template, api_key, api_secret, URLEncoder.encode(userid, "utf-8"), userid_type);
		return callRestAPI(params);
	}

	public JSONObject callRestAPI(String params) throws Exception {
		OutputStream out = null;
		BufferedReader in = null;
		JSONObject result = null;
		try{
			HttpURLConnection conn = (HttpURLConnection) new URL(api_url).openConnection();
			// for POST request
			conn.setDoOutput(true);
			out = conn.getOutputStream();
			
			out.write(params.getBytes());
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			// https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			// get json response
			result = new JSONObject(response.toString());
		}
		catch(Exception expt){
			throw expt;
		}
		finally{
			//https://stackoverflow.com/questions/11056088/do-i-need-to-call-httpurlconnection-disconnect-after-finish-using-it
			if (out != null){
				out.close();
			}
			if (in != null){
				in.close();
			}
			return result;
		}
	}

	public boolean verify(String password) throws Exception {
		// password should not be empty or longer than 255
		if (empty(password) || password.length() > 255){
			throw new Exception("VeriClouds::verify password is invalid");
		}
		// use all lower case increase the possibility of matching
		String passwordHash = sha(password.toLowerCase(), "SHA-1");
		// only partial of the hashed password is sent to API
		JSONObject result = callPasswordAPI(passwordHash.substring(0, 6));
		if (result == null){
			throw new Exception("VeriClouds.verify: callPasswordAPI returns null");
		}
		
		// the result is the hashed passwords found by API
		if (result.getString("result").equals("succeeded")) {
			JSONArray password_hashes = result.getJSONArray("password_hashes");
			if (password_hashes == null){
				throw new Exception("VeriClouds::verify password_hashes is null");
			}

			for (int i =0; i < password_hashes.length(); i++) {
				String hash = password_hashes.getString(i);
				if (!empty(hash)){
					if (hash.toUpperCase().equals(passwordHash))
						return true;
				}
			}
			// no hash match found
			return false;
		} else {
			throw new Exception("VeriClouds.verify: callPasswordAPI failed: " + result.getString("reason"));
		}
	}

	public boolean verify(String email, String password, String userid_type) throws Exception {
		// neither email or password should be empty or longer than 255
		if (empty(email) || email.length() > 255){
			throw new Exception("VeriClouds.verify: email is empty");
		}

		if (empty(password) || password.length() > 255){
			throw new Exception("VeriClouds.verify: password is empty");
		}

		JSONObject result = callUserIdAPI(email, userid_type);
		if (result == null){
			throw new Exception("VeriClouds.verify: callUserIdAPI returns null");
		}
		
		if (result.getString("result").equals("succeeded")) {
			JSONArray passwords = result.getJSONArray("passwords_encrypted");
			if (passwords == null){
				throw new Exception("VeriClouds.verify: passwords is null");
			}
			// for (Object obj : passwords.myArrayList) {
			for (int i =0; i<passwords.length(); i++) {
				
				String encrypted = passwords.getString(i);
				if (!empty(encrypted)){
					String masked = decrypt(encrypted);
					int length = masked.length();
					// do case-insensitive comparison on purpose
					password = password.toLowerCase();
					masked = masked.toLowerCase();

					if (length == password.length()
						&& masked.charAt(0) == password.charAt(0)
						&& masked.charAt(length - 1) == password.charAt(length - 1))
						// match to the masked password such as 1******8
						return true;
				}
			}
			// no match found
			return false;
		} else {
			throw new Exception("VeriClouds.verify: callUserIdAPI failed: " + result.getString("reason"));
		}
	}
}