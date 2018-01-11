package com.example.administrator.xmlweather37;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.json.JSONArray;
import org.xmlpull.v1.XmlPullParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends AppCompatActivity{
    HttpURLConnection httpURLConnection = null;

     AutoCompleteTextView  value;
   Button find;
    LinearLayout body;
    ArrayList<WeatherInf> weatherInfs = new ArrayList<>();
    String cityname = "广州";
    //PHP接口地址
    private String weburl = "http://10.0.2.2:8080/phptest/area_api.php";//我的localhost是8080端口,如果端口是默认80的，则把：8080去掉即可读取接口数据库
  //如果在自己笔记本上http://10.0.2.2/phptest/area_api.php(如果端口是默认80的)连接不上接口，显示网络错误，不慌，把10.0.2.2换成你电脑的ip地址即可哦
    //本人多次试验得出的哈，然后数据就会显示出来，这时你可以把刚刚你的ip地址重新换为0.0.2.2，你将会发现哎呦，本来不行的突然变可以了，哈哈，我也不知道为什么，
    //反正可以就OK了哈
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气查询XML");
        GetArea getArea = new GetArea();
        getArea.start();

        find = (Button) findViewById(R.id.find);
        body = (LinearLayout) findViewById(R.id.my_body);


        value = (AutoCompleteTextView) findViewById(R.id.value);


        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                body.removeAllViews();
                cityname = value.getText().toString();
                Toast.makeText(MainActivity.this,"正在查询天气信息...",Toast.LENGTH_LONG).show();
                GetXml gx = new GetXml(cityname);
                gx.start();
            }
        });
    }

   class WeatherInf {
        String date;
        String high;
        String low;
        M day;
        M night;
    }
    class M{
        String type;
        String fengxiang;
        String fengli;
        public String inf(){
            String str = type + "风向：" + fengxiang + "风力：" + fengli;
            return str;
        }
    }

    private final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    show();
                    break;
                case 8080:
                    ArrayAdapter adapter = (ArrayAdapter)msg.obj;
                    value.setAdapter(adapter);
                    value.setThreshold(1);//设置输入一个字即触发提示

            }
            super.handleMessage(msg);
        }
    };
    class GetArea extends Thread{
        private String area="";
        public GetArea(){
            try {
                this.area = URLEncoder.encode(area,"UTF-8");
            }catch (Exception ee){

            }
        }
        @Override
        public void run() {
            try {
                URL url = new URL(weburl);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                int code = httpURLConnection.getResponseCode();
                if(code==200){
                    InputStream in = httpURLConnection.getInputStream();
                    InputStreamReader din = new InputStreamReader(in);
                    BufferedReader bdin = new BufferedReader(din);
                    StringBuffer sbf = new StringBuffer();
                    String line = null;

                    while((line=bdin.readLine())!=null){
                        sbf.append(line);
                    }

                    String jsonData =new String(sbf.toString().getBytes(),"UTF-8") ; //此句非常重要！把字符串转为utf8编码，因为String 默认是unicode编码的。
                    JSONArray jsonArray = new JSONArray(jsonData);
                    List<String> list = new ArrayList<String>();
                    for(int i=0;i<jsonArray.length();i++){
                        String pro = jsonArray.opt(i).toString();
                        list.add(pro);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Message msg = new Message();
                    msg.obj = adapter;
                    msg.what = 8080;
                    handler.sendMessage(msg);



                    // sp_province.setAdapter(adapter);//线程不能访问主线程activity的控件
                }else{
                    Looper.prepare();
                    Toast.makeText(MainActivity.this,"网址不可访问",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }catch (Exception ee){
                Looper.prepare();
                Toast.makeText(MainActivity.this,"网络异常",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }
    }


    class GetXml extends Thread{
        private String urlstr =  "http://wthrcdn.etouch.cn/WeatherApi?city=";
        public GetXml(String cityname){
            try{
                urlstr = urlstr+ URLEncoder.encode(cityname,"UTF-8");
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }

        @Override
        public void run() {

            for(int i = 0; i<weatherInfs.size();i++){
                weatherInfs.clear();
            }
            InputStream din = null;
            try{
                URL url = new URL(urlstr);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                din = httpURLConnection.getInputStream();
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(din,"UTF-8");
                WeatherInf pw = null;
                M m = null;
                int eveType = xmlPullParser.getEventType();
                while(eveType != XmlPullParser.END_DOCUMENT){
                    //开始节点
                    if(eveType == XmlPullParser.START_TAG){
                        String tag = xmlPullParser.getName();
                        if(tag.equalsIgnoreCase("weather")){
                            pw = new WeatherInf();
                        }
                        //下个节点
                        if(tag.equalsIgnoreCase("date")){
                            if(pw != null){
                                pw.date = xmlPullParser.nextText();
                            }
                        }
                        //下一个节点，以此类推
                        if(tag.equalsIgnoreCase("high")){
                            if(pw != null){
                                pw.high = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("low")){
                            if(pw != null){
                                pw.low = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("day")){
                            m = new M();
                        }
                        if(tag.equalsIgnoreCase("night")){
                            m = new M();
                        }
                        if(tag.equalsIgnoreCase("type")){
                            if(m != null){
                                m.type = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("fengxiang")){
                            if(m != null){
                                m.fengxiang = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("fengli")){
                            if(m != null){
                                m.fengli = xmlPullParser.nextText();
                            }
                        }
                    }
                    //后节点
                    else if(eveType == XmlPullParser.END_TAG){
                        String tag = xmlPullParser.getName();
                        if (tag.equalsIgnoreCase("weather")){
                            weatherInfs.add(pw);
                            pw = null;
                        }

                        if(tag.equalsIgnoreCase("day")){
                            pw.day = m;
                            m = null;
                        }
                        if(tag.equalsIgnoreCase("night")){
                            pw.night = m;
                            m = null;
                        }
                    }
                    eveType = xmlPullParser.next();
                }

                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public void show(){
        //显示
        body.removeAllViews();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        for(int i = 0; i<weatherInfs.size();i++){
            //日期
            TextView dateView = new TextView(this);
            dateView.setGravity(Gravity.CENTER_HORIZONTAL);
            dateView.setLayoutParams(params);
            dateView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            dateView.setText("日期："+weatherInfs.get(i).date);
            body.addView(dateView);
            //白天和夜间
            TextView mView = new TextView(this);
            mView.setLayoutParams(params);
            String str = "高温：" + weatherInfs.get(i).high+",低温：" + weatherInfs.get(i).low + "\n";
            str = str + "白天：" + weatherInfs.get(i).day.inf() + "\n";
            str = str + "夜间：" +weatherInfs.get(i).night.inf();
            mView.setText(str);
            body.addView(mView);
        }

    }

}