package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.math3.analysis.solvers.RegulaFalsiSolver;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginDialog extends Dialog{
	
	final private Context mContext;
	final private SharedPreferences pref;
	
	public LoginDialog(Context context, int cv) {
		super(context, android.R.style.Theme_Black);
		
		mContext = context;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		
		setContentView(R.layout.dialog_login);
		
		Button register = (Button) findViewById(R.id.login_register);
		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContentView(R.layout.dialog_register);
				
				final EditText username = (EditText) findViewById(R.id.register_username);
				username.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						username.setError(null);
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					@Override
					public void afterTextChanged(Editable s) {}
				});
				final EditText email = (EditText) findViewById(R.id.register_email);
				email.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						email.setError(null);
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					@Override
					public void afterTextChanged(Editable s) {}
				});
				final EditText password = (EditText) findViewById(R.id.register_password);
				password.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						password.setError(null);
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					@Override
					public void afterTextChanged(Editable s) {}
				});
				final EditText password2 = (EditText) findViewById(R.id.register_password_check);
				password2.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						password2.setError(null);
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					@Override
					public void afterTextChanged(Editable s) {}
				});
				
				final TextView tv = (TextView) findViewById(R.id.register_tos);
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				
				final Button commit = (Button) findViewById(R.id.register_commit);
				commit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AsyncTask<Void, Void, Integer>() {
							private final int WHAT = -1;
							private final int SUCCESS = 0;
							private final int USERNAME_TAKEN = 1;
//							private final int WRONG_INFO = 2;
							private final int EXCEPTION = 3;
							
							private ProgressDialog diag;
							
							@Override
							protected void onPreExecute() {
								//check if all fields are valid
								if(username.getText().length() < 1){
									username.setError(mContext.getString(R.string.register_usernametooshort));
									cancel(true);
								} else if(username.getText().length() > 20){
									username.setError(mContext.getString(R.string.register_usernametoolong));
									cancel(true);
								} else if (password.getText().length() < 1){
									password.setError(mContext.getString(R.string.register_passwordempty));
									cancel(true);
								} else if (password2.getText().length() < 1){
									password2.setError(mContext.getString(R.string.register_passwordempty));
									cancel(true);
								} else if (!password.getText().toString().equals(password2.getText().toString())){
									password.setError(mContext.getString(R.string.register_passworddoesnotmatch));
									password2.setError(mContext.getString(R.string.register_passworddoesnotmatch));
									cancel(true);
								} else if(email.getText().length() < 1){
									email.setError(mContext.getString(R.string.register_emailempty));
									cancel(true);
								} else if(!StringValidityChecker.isValidEmail(email.getText().toString())){
									//check email format
									email.setError(mContext.getString(R.string.register_emailinvalid));
									cancel(true);
								} else{
									diag = new ProgressDialog(mContext);
									diag.setCancelable(false);
									diag.setMessage(mContext.getString(R.string.progressdialog_registering));
									diag.setIndeterminate(true);
									diag.show();
								}
							}
							@Override
							protected Integer doInBackground(Void... params) {
								int resultcode = WHAT;
								
								String url = "http://plash2.iis.sinica.edu.tw/api/SignUp.php?username=" + InternetUtility.encode(username.getText().toString()) + "&password=" + InternetUtility.getMD5(password.getText().toString()) + "&email=" + InternetUtility.encode(email.getText().toString());
								
//								Log.e("register", "url=" + url);
								
								HttpGet getRequest = new HttpGet(url);
								
								HttpParams httpParameters = new BasicHttpParams();
								// Set the timeout in milliseconds until a connection is established. 
								// The default value is zero, that means the timeout is not used.
								HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
								// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
								int timeoutSocket = 10000;
								HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
								
								DefaultHttpClient client = new DefaultHttpClient(httpParameters);
								try{
									
									HttpResponse response = client.execute(getRequest);
									
									Integer statusCode = response.getStatusLine().getStatusCode();
									if (statusCode == 200) {
										
										BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
										JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{").replace("});", "}")));
										in.close();
										if(result.has("code")){
											if((result.getInt("code")==200) && result.has("userid")){
												pref.edit().putString("userid", result.getString("userid")).putString("username", username.getText().toString()).commit();
												resultcode = SUCCESS;
											} else if((result.getInt("code")==400)){
												resultcode = USERNAME_TAKEN;
											}
										} else {
											resultcode = WHAT;
										}
									}
									
								} catch(JSONException e){
									e.printStackTrace();
									resultcode = EXCEPTION;
								} catch (ClientProtocolException e) {
									e.printStackTrace();
									resultcode = EXCEPTION;
								} catch (IOException e) {
									//XXX also includes connection timeout and socket timeout
									e.printStackTrace();
									resultcode = EXCEPTION;
								} finally{
									client.getConnectionManager().shutdown();
								}
								
								return resultcode;
							}
							
							@Override
							protected void onPostExecute(Integer result) {
								diag.dismiss();
								switch(result){
								case SUCCESS:
									//XXX show success toast and dismiss dialog
									dismiss();
									Toast.makeText(mContext, R.string.register_success, Toast.LENGTH_SHORT).show();
									break;
								case USERNAME_TAKEN:
									//XXX show error on username textbox
									Toast.makeText(mContext, R.string.register_usernametaken, Toast.LENGTH_SHORT).show();
									username.setError(mContext.getString(R.string.register_usernametrydifferent));
									break;
								case EXCEPTION:
									//XXX show exception and ask user to try again later
									Toast.makeText(mContext, R.string.register_connectionerror, Toast.LENGTH_LONG).show();
									break;
								case WHAT:
								default:
									//XXX say server is busy, try again later
									Toast.makeText(mContext, R.string.register_serverbusy, Toast.LENGTH_LONG).show();
									break;
								}
							}
						}.execute(); 
						
					}
				});
				final Button cancel = (Button) findViewById(R.id.register_cancel);
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(mContext, R.string.toast_canceled, Toast.LENGTH_SHORT).show();
						LoginDialog.this.dismiss();
					}
				});
				((CheckBox) findViewById(R.id.register_checkbox))
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							commit.setEnabled(isChecked);
						}
					}
				);
			}
		});
		
		final EditText username = (EditText) findViewById(R.id.login_username);
		username.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				username.setError(null);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		final EditText password = (EditText) findViewById(R.id.login_password);
		password.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				password.setError(null);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		//commit button
		((Button) findViewById(R.id.login_commit)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(username.getText().length() < 4){
					//username too short
					username.setError("username too short, must > 4 characters");
				} else if(username.getText().length() > 20){
					//username too long
					username.setError("username too long, must < 20 characters");
				} else if(password.getText().length() < 1){
					//password empty
					password.setError("password is empty");
				} else if(!isNetworkAvailable()){
					//no internet
					new AlertDialog.Builder(mContext)
						.setTitle(R.string.error)
						.setMessage(R.string.alertdialog_nointernet_nologin)
						.setNeutralButton(R.string.alertdialog_sorrybutton, null)
						.show();
				} else{
					//all good, proceed to normal login
					mlogin = new login(username.getText().toString(), password.getText().toString());
					mlogin.execute();
				}
			}
		});
		
		//cancel button
		((Button) findViewById(R.id.login_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, R.string.toast_canceled, Toast.LENGTH_SHORT).show();
				LoginDialog.this.dismiss();
			}
		});
		
//		((Button) findViewById(R.id.login_facebook)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(mContext, R.string.toast_function_coming_soon, Toast.LENGTH_SHORT).show();
//			}
//		});
	}
	
	private login mlogin;
	private class login extends AsyncTask<Void, Void, Integer>{
		
		private ProgressDialog diag;
		
		final private String uname;
		final private String pword;
		
		public login(String uname, String pword) {
			this.uname = uname;
			this.pword = pword;
		}
		
		@Override
		protected void onPreExecute() {
			diag = new ProgressDialog(mContext);
			diag.setCancelable(true);
			diag.setCanceledOnTouchOutside(false);
			diag.setIndeterminate(true);
			diag.setMessage(mContext.getResources().getString(R.string.progressdialog_logginin));
			diag.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if(mlogin != null){
						mlogin.cancel(true);
					}
				}
			});
			diag.show();
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			
				String url = "http://plash2.iis.sinica.edu.tw/api/login.php?username=" + InternetUtility.encode(uname) + "&password=" + InternetUtility.getMD5(pword);
//				String url = "http://plash2.iis.sinica.edu.tw/api/login.php?username=xsirh82&password=44b91147489c44542d1e17b141bd5c4d";
				
				HttpGet getRequest = new HttpGet(url);
				
				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established. 
				// The default value is zero, that means the timeout is not used.
				HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
				// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				try{
				HttpResponse response = client.execute(getRequest);
				
				Integer statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					if(result.getString("sid").equals("0")){
						//login failed
						return -3;
					} else{
						//otherwise assume login is good
						pref.edit().putString("userid", result.getString("sid")).putString("username", uname).commit();
						return 0;
					}
				} else {
					
					
					// connection error
					return -2;
				}
			} catch(IOException e){
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				client.getConnectionManager().shutdown();
			}
			return -1;
		}
		
		@Override
		protected void onCancelled() {
			Toast.makeText(mContext, R.string.toast_login_canceled, Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			diag.dismiss();
			switch(result){
			case 0:
				Toast.makeText(mContext, R.string.toast_login_success, Toast.LENGTH_LONG).show();
				LoginDialog.this.dismiss();
				break;
			case -1:
			case -2:
			case -3:
			default:
				Toast.makeText(mContext, R.string.toast_login_failed, Toast.LENGTH_LONG).show();
				break;
			
			}
		}
	}
	
	private register mregister;
	private class register extends AsyncTask<Void, Void, Integer>{
		
		private ProgressDialog diag;
		
		final private String uname;
		final private String email;
		final private String pword;
		
		public register(String uname, String email, String pword) {
			this.uname = uname;
			this.email = email;
			this.pword = pword;
		}
		
		@Override
		protected void onPreExecute() {
			diag = new ProgressDialog(mContext);
			diag.setCancelable(true);
			diag.setCanceledOnTouchOutside(false);
			diag.setIndeterminate(true);
			diag.setMessage(mContext.getResources().getString(R.string.progressdialog_registering));
			diag.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if(mregister != null){
						mregister.cancel(true);
					}
				}
			});
			diag.show();
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			
				String url = "http://plash2.iis.sinica.edu.tw/api/SignUp.php?username=" + InternetUtility.encode(uname) + "&password=" + InternetUtility.getMD5(pword) + "&email=" + InternetUtility.encode(email);
//				String url = "http://plash2.iis.sinica.edu.tw/api/login.php?username=xsirh82&password=44b91147489c44542d1e17b141bd5c4d";
				
				HttpGet getRequest = new HttpGet(url);
				
				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established. 
				// The default value is zero, that means the timeout is not used.
				HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
				// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				try{
				HttpResponse response = client.execute(getRequest);
				
				Integer statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					if(result.getString("sid").equals("0")){
						//login failed
						return -3;
					} else{
						//otherwise assume login is good
						pref.edit().putString("userid", result.getString("sid")).putString("username", uname).commit();
						return 0;
					}
				} else {
					
					
					// connection error
					return -2;
				}
			} catch(IOException e){
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				client.getConnectionManager().shutdown();
			}
			return -1;
		}
		
		@Override
		protected void onCancelled() {
			Toast.makeText(mContext, R.string.toast_register_canceled, Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			diag.dismiss();
			switch(result){
			case 0:
				Toast.makeText(mContext, R.string.toast_register_success, Toast.LENGTH_LONG).show();
				LoginDialog.this.dismiss();
				break;
			case -1:
			case -2:
			case -3:
			default:
				Toast.makeText(mContext, R.string.toast_register_failed, Toast.LENGTH_LONG).show();
				break;
			
			}
		}
	}
	
	private boolean isNetworkAvailable(){
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni != null){ //in airplane mode, there are no "Active Network", networkInfo will be null
			return ni.isConnected();
		} else{
			return false;
		}
	}
}