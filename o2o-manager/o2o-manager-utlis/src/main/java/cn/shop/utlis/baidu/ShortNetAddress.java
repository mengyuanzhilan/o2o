package cn.shop.utlis.baidu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShortNetAddress {
	private static Logger log = LoggerFactory.getLogger(ShortNetAddress.class);

	public static int TIMEOUT = 30 * 1000;
	public static String ENCODING = "UTF-8";

	/**
	 * JSON get value by key
	 * Json 依据传入的key获取value
	 * @param replyText
	 * @param key
	 * @return
	 */
	private static String getValueByKey_JSON(String replyText, String key) {
		ObjectMapper mapper = new ObjectMapper();
		//定义json节点
		JsonNode node;
		String tinyUrl = null;
		try {
			//把调用返回的消息串转成json对象
			node = mapper.readTree(replyText);
			//依据key从json对象里获取对应的值
			tinyUrl = node.get(key).asText();
		} catch (JsonProcessingException e) {
			log.error("getValueByKey_JSON error:" + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			log.error("getValueByKey_JSON error:" + e.toString());
		}

		return tinyUrl;
	}

	/**
	 * 通过HttpConnection 获取返回的字符串
	 * 
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	private static String getResponseStr(HttpURLConnection connection)
			throws IOException {
		StringBuffer result = new StringBuffer();
		//从链接中获取http状态码
		int responseCode = connection.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			//如果返回的状态码是OK的，那么取出链接的是输入流
			InputStream in = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, ENCODING));
			String inputLine = "";
			while ((inputLine = reader.readLine()) != null) {
				//将消息逐行读入结果中
				result.append(inputLine);
			}
		}
		//将结果转换成string并返回
		return String.valueOf(result);
	}

	/**
	 * 根据传入的url，通过访问百度短视频的接口，将其转换成短的url
	 * @param originURL
	 * @return
	 */
	public static String getShortURL(String originURL) {
		String tinyUrl = null;
		try {
			//指定百度短链接的接口
			URL url = new URL("http://dwz.cn/create.php");
			//建立连接
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			// POST Request Define:
			//设置连接的参数
			//设置连接进行输出
			connection.setDoOutput(true);
			//使用连接进行输出
			connection.setDoInput(true);
			//不适用缓冲
			connection.setUseCaches(false);
			//设置连接超时时间为30秒
			connection.setConnectTimeout(TIMEOUT);
			//设置请求模式为post
			connection.setRequestMethod("POST");
			//设置post信息，这里传入的原始url
			String postData = URLEncoder.encode(originURL.toString(), "utf-8");
			//输出原始的url
			connection.getOutputStream().write(("url=" + postData).getBytes());
			//连接百度短视频接口
			connection.connect();
			//获取返回的字符串
			String responseStr = getResponseStr(connection);
			log.info("response string: " + responseStr);
			//在字符串里获取tinyurl，即短链接
			tinyUrl = getValueByKey_JSON(responseStr, "tinyurl");
			System.out.println("tinyUrl:"+tinyUrl);
			log.info("tinyurl: " + tinyUrl);
			//关闭连接
			connection.disconnect();
		} catch (IOException e) {
			log.error("getshortURL error:" + e.toString());
		}
		return tinyUrl;

	}

	/**
	 * ‘ 百度短链接接口 无法处理不知名网站，会安全识别报错
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		getShortURL("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx8c3a841c6efd97e9&redirect_uri=https://myo2o.mynatapp.cc/wechatlogin/logincheck&response_type=code&scope=snsapi_userinfo&state=2#wechat_redirect&connect_redirect=1");
	}
}
