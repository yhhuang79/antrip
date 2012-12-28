package tw.plash.antrip.offline;

import java.util.regex.Pattern;

public class StringValidityChecker {
	
	public static boolean isValidUsername(String username) {
//		String expression = "^[A-Za-z0-9_]{6,20}$";
//		return Pattern.compile("^[A-Za-z0-9_]{1,20}$").matcher(username).matches();
		return Pattern.compile("^[A-Za-z0-9_]{1,}$").matcher(username).matches();
	}
	
	public static boolean isValidPassword (String password){
//		String expression = "^[A-Za-z0-9!@#$%^&*()_]{6,20}$";
//		return Pattern.compile("^[A-Za-z0-9!@#$%^&*()_]{1,20}$").matcher(password).matches();
		return Pattern.compile("^[A-Za-z0-9!@#$%^&*()_]{1,}$").matcher(password).matches();
	}
	
	public static boolean isValidEmail(String email){
		//^\w+[-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$
//		String expression = "^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\\.([a-zA-Z])+([a-zA-Z])+";
		return Pattern.compile("^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\\.([a-zA-Z])+([a-zA-Z])+").matcher(email).matches();
	}
}
