import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by Andy_and_Natalia on 22.10.2014.
 */
public class Bot {
    /*
    Массив с адресами серваков игры
     */
    private static final String[] SERVERS_IP = {"5.178.83.91", "5.178.83.92"};
    /*
    Идентификатор пользователя в ВК. Подставить свой
     */
    private static final String USER_SN_ID = "13466551";
    /*
    Хэш в игре.
     */
    private static final String USER_HASH = "6028d84bef14d25f3c7edd6756fede6b";
    private static final String START_BATTLE = "[{\"request\":\"start_boss\",\"params\":{\"boss\":\"0\",\"mode\":\"0_0\"}}]";
    private static final String HIT_BY_QUICK = "[{\"request\":\"hit_boss\",\"params\":{\"weapon\":\"0\",\"power\":\"0\"}}]";
    private static final String HIT_BY_KNIFE = "[{\"request\":\"hit_boss\",\"params\":{\"weapon\":\"1\",\"power\":\"0\"}}]";
    private static final String HIT_BY_MASSED = "[{\"request\":\"hit_boss\",\"params\":{\"weapon\":\"2\",\"power\":\"0\"}}]";
    private static final String FATALITY = "[{\"request\":\"kill_boss\",\"params\":{}}]";
    private static final String FINISH_BATTLE = "[{\"request\":\"finish_boss\",\"params\":{}}]";
    private static final HttpClient httpClient = HttpClients.createDefault();
    private static String getString = "";


    private static String getServerIp() {
        if (Math.round(Math.random()) == 1) {
            return SERVERS_IP[1];
        } else {
            return SERVERS_IP[0];
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        makeGetString();
        JSONObject obj = new JSONObject(getString);
        int bossHealth = obj.getInt("boss_health");
        int bossesLimit = 15 + obj.getJSONObject("player").getJSONObject("static_resources")
                .getInt("boss_limit_boost");
        if (inBattle()) {
            if (bossHealth == 0) {
                Thread.sleep(3000);
                killBoss();
            } else {
                Thread.sleep(3000);
                checkBossStatus();
            }
        }
        makeGetString();
        Thread.sleep(3000);
        obj = new JSONObject(getString);
        int currentWins = obj.getJSONObject("player").getJSONObject("expiring_resources")
                .getJSONObject("boss_limit").getInt("amount");

        for (int i = currentWins; i <= bossesLimit; i++) {
            Thread.sleep(15000);
            startBattle();

        }

    }

    private static void startBattle() throws IOException, InterruptedException {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("requests", START_BATTLE));
        JSONObject object = new JSONObject(getResponse(getPackage(), pairs));
        if (object.getString("result").equals("ok")) {
            System.out.println("start battle");
            Thread.sleep(3000);
            pairs.clear();
            pairs.add(new BasicNameValuePair("requests", HIT_BY_MASSED));
            object = new JSONObject(getResponse(getPackage(), pairs));
            if (object.getString("result").equals("ok")) {
                Thread.sleep(3000);
                checkBossStatus();

                }
            }

        }


    private static void checkBossStatus() throws IOException, InterruptedException {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("ts", String.valueOf(new Date().getTime())));
        JSONObject object;
        while (true) {
            Thread.sleep(3000);
            pairs.clear();
            pairs.add(new BasicNameValuePair("ts", String.valueOf(new Date().getTime())));
            object = new JSONObject(getResponse(getBoss(), pairs));
            if (object.getInt("boss_health") == 0) {
                Thread.sleep(3000);
                killBoss();
            }
            Thread.sleep(20000);
        }
    }

    private static void killBoss() throws IOException, InterruptedException {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("requests", FATALITY));
        JSONObject object = new JSONObject(getResponse(getPackage(), pairs));
        if ("ok".equals(object.getString("result"))) {
            Thread.sleep(2000);
            finishBoss();
        } else {
            Thread.sleep(2000);
            killBoss();

        }

    }

    private static void finishBoss() throws IOException, InterruptedException {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("requests", FINISH_BATTLE));
        JSONObject object = new JSONObject(getResponse(getPackage(), pairs));
        if (!("ok".equals(object.getString("result")))) {
            Thread.sleep(600);
            finishBoss();
            System.out.println("Finished battle");
        }
    }

    private static boolean inBattle() throws InterruptedException {
        JSONObject obj = new JSONObject(getString);
        JSONObject x = null;
        try {
            Thread.sleep(5000);
            x = obj.getJSONObject("player").getJSONObject("current_boss");
        } catch (JSONException e) {

        }
        if (x == null) {
            System.out.println("not in battle");
            return false;
        } else {
            System.out.println("in battle");
            return true;
        }

    }

    public static String getPackage() {

        return getSB().append("package").toString();
    }

    public static String getBoss() {
        return getSB().append("get_boss").toString();
    }


    public static String get() {
        return getSB().append("get").toString();
    }

    private static StringBuilder getSB() {
        StringBuilder sb = new StringBuilder();
        sb = sb.append("http://").append(getServerIp()).append("/").append(USER_SN_ID).append("/").append(USER_HASH).append("/");
        return sb;
    }

    private static void makeGetString() throws IOException {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("ts", String.valueOf(new Date().getTime())));
        getString = getResponse(get(), pairs);
    }

    private static HttpEntity makeRequest(String s, ArrayList<NameValuePair> params) throws IOException {
        HttpPost httpPost = new HttpPost(s);

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return entity;
    }

    private static String getResponse(String s, ArrayList<NameValuePair> params) throws IOException {
        InputStream instream = makeRequest(s, params).getContent();
        Scanner sc = new Scanner(instream);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNext()) {
            sb.append(sc.nextLine());
        }
        sc.close();
        System.out.println(sb.toString());
        return sb.toString();
    }


}
