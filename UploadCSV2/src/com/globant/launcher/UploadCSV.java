package com.globant.launcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.globant.talent.utils.CSVDataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.model.globant.ResponseJson;

public class UploadCSV {

	
	private static String useInstructions = "this program must be run as follow : \n java uploadCSV.jar <path to the file> <columns to validate (separated by ,)>"
			+ " <user> <password>";
	
	
	public static void main(String[] args)  {
		
		StringBuilder strb = new StringBuilder();
		Properties prop = new Properties();
		InputStream input = null;

		
		validateArguments(args);
		
		int errores = 0;
		
		try {
			errores = validateFile(args[0], args[1], strb);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			
		}
			
		
		if(errores >= 100){
			
			System.out.println("there has been too many errors in the receiving file. there must be less than 100 errors .");
			System.out.println(strb.toString());
			return ;
			
		}
		
		
			System.out.println("Init, loading config.");
			try {
				input = new FileInputStream("./config.properties");
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				prop.load(input);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String URL = prop.getProperty("URL");
			String keyStore = prop.getProperty("keyStore");
			String passwordKeyStore = prop.getProperty("passwordKeyStore");
			String proxy = prop.getProperty("proxy");
			String portProxy = prop.getProperty("portProxy");
			
			String urlLogin ="https://qa.starmeup.com/starmeup-api/v2/sec/authenticateuser";
			
			SSLContext sslContext = null;
			File f = new File(keyStore);
			// Trust own CA and all self-signed certs
			try {
				sslContext = SSLContexts.custom().loadTrustMaterial(f, passwordKeyStore.toCharArray(), new TrustSelfSignedStrategy()).build();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

			HttpHost hostProxy = new HttpHost(proxy, Integer.parseInt(portProxy));
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(hostProxy);
			CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setRoutePlanner(routePlanner).build();
			
			CloseableHttpClient httpclientLogin = HttpClients.custom().setSSLSocketFactory(sslsf).setRoutePlanner(routePlanner).build();
			
			HttpPost httpPost = new HttpPost(URL);
			
			HttpPost httpPostLogin = new HttpPost(urlLogin);
			
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("email", args[2]));  //user1@qadavivienda.com
			formparams.add(new BasicNameValuePair("password", args[3])); //rootuserstarmeup
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
			
			httpPostLogin.setEntity(entity);
			
			
			
			
			
			ResponseHandler<ResponseJson> rh = new ResponseHandler<ResponseJson>() {

			  

				@Override
				public ResponseJson handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					 StatusLine statusLine = response.getStatusLine();
				        HttpEntity entity = response.getEntity();
				        if (statusLine.getStatusCode() >= 300) {
				            throw new HttpResponseException(
				                    statusLine.getStatusCode(),
				                    statusLine.getReasonPhrase());
				        }
				        if (entity == null) {
				            throw new ClientProtocolException("Response contains no content");
				        }
				        Gson gson = new GsonBuilder().create();
				        ContentType contentType = ContentType.getOrDefault(entity);
				        Charset charset = contentType.getCharset();
				        Reader reader = new InputStreamReader(entity.getContent(), charset);
				        return gson.fromJson(reader, ResponseJson.class);
				}
			};
			
			ResponseJson myjson=null;
			try {
				myjson = httpclientLogin.execute(httpPostLogin, rh);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!myjson.getStatus().equals("OK")){
				
				System.out.println("there was a problem with the credentials.. please check if they are the right ones");
				System.out.println(useInstructions);
				return;
			
			}
			
			
			File file = new File(args[0]);   //"C:/Users/mario.fernandez/Downloads/DemoImport5.csv"
			FileBody bin = new FileBody(file);
			
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", bin)
                    
                    .addTextBody("contentType", "application/x-www-form-urlencoded; charset=utf-8")
                    
                    .addTextBody("token", myjson.getToken())
                    .addTextBody("fileName", "DemoImport5.csv")
                    .build();

    		
            httpPost.setEntity(reqEntity);
			
			
			
			
			System.out.println("Executing request " + httpPost.getRequestLine());

			CloseableHttpResponse response=null;
			try {
				response = httpclient.execute(httpPost);
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				HttpEntity responseEntity = response.getEntity();

				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				EntityUtils.consume(responseEntity);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					response.close();
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
	
	
}
	
	
	private static void validateArguments(String[] args){
		
		if (args.length!=4){
			
			System.out.println(useInstructions);
			
			System.exit(0);
		}
		
		
		
		
		String pathToFile = args[0];

		
		File file2 = new File(pathToFile);
		
		if (!file2.exists()){
			
			System.out.println("the specified path is incorrect or it does not exist.");
			System.out.println(useInstructions);
		}
		
	}
	
	
	private static int  validateFile(String filePath, String columnsToValidate, StringBuilder strb) throws IOException{
		
		strb.append("the followinr errors have been found : \n") ;
		
		int errores = 0;
		
		String [] columns = columnsToValidate.split(",");
		
		File file = new File(filePath);
		
		FileInputStream fis = new FileInputStream(file);
		
		CSVDataSource csv = new CSVDataSource(fis);
		
		int line = 1;
		
		while (csv.hasNext()){
			
			Map<String,String> map = csv.getNextRecord();
			
			for (int i = 0; i < columns.length; i++) {
				
				 String dato = map.get(columns[i]);
				
				 if (dato!=null | !dato.equals("")) {
					 
					 errores++;
					 
					 strb.append("line: "+ line  + " missing field in column "+ columns[i]+" \n");
					 
				 }
				 line ++;
				 errores = dato!=null | !dato.equals("") ? errores++ : errores; 
				
			}
			
			
		}
		
		
		
		
		return errores;
	}
	
}
