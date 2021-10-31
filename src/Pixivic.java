import jdk.internal.util.xml.impl.Input;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pixivic {
    public Pixivic(){ }

    public String[] GetVerifiCode(){
        try{
            HttpsURLConnection connection = (HttpsURLConnection)new URL("https://pix.ipv4.host/verificationCode").openConnection();
            connection.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            connection.setRequestProperty("referer","https://sharemoe.net/");
            connection.setRequestProperty("origin","https://sharemoe.net");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String temp;
            while((temp = br.readLine()) != null)
                buffer.append(temp+"\n");
            System.out.println("从返回信息中切割出有用的部分...顺便给图片拼接一点信息");
            String[] data = new String[]{buffer.toString().split("\"")[9],buffer.toString().split("\"")[13]};
            System.out.println("vid:"+data[0]+"\nVerifiBase64Encode："+data[1]);
            return data;
        }catch(Exception e){System.out.println(e.toString());return null;}
    }

    public void DownloadVerifiImage(String image){
        try{
            byte[] imageData = Base64.getDecoder().decode(image);
            FileOutputStream out = new FileOutputStream("Verifi.png");
            out.write(imageData);
            out.close();
        }catch(Exception e){System.err.println(e.toString());}
    }

    public void SendOPTIONS(String vid,String check){
        try{
            HttpsURLConnection connection = (HttpsURLConnection)new URL("https://pix.ipv4.host/users/token?vid="+vid+"&value="+check).openConnection();
            connection.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            connection.setRequestProperty("access-control-request-headers","content-type");
            connection.setRequestProperty("access-control-request-method","POST");
            connection.setRequestProperty("referer","https://sharemoe.net/");
            connection.setRequestMethod("OPTIONS");
            System.out.println("NextLine Action.");
        }catch(Exception e){System.err.println(e.toString());}
    }

    public String HTTPSRequest(String vid,String check){
        try{
            HttpsURLConnection connection = (HttpsURLConnection)new URL("https://pix.ipv4.host/users/token?vid="+vid+"&value="+check).openConnection();
            connection.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            connection.setRequestProperty("content-type","application/json;charset=UTF-8");
            connection.setRequestProperty("referer","https://sharemoe.net/");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            System.out.println("ConnectWeb:"+connection.getURL());
            String data = "{\"username\":\"你的账号\",\"password\":\"你的密码\"}";
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(data);
            out.flush();
            out.close();
            //
            System.out.println("RequestCode:"+connection.getResponseCode());
            BufferedReader br;
            if(connection.getResponseCode() != 200){
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }else {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            String temp;
            while((temp = br.readLine()) != null)
                System.out.println(temp+"\n");
            if(connection.getResponseCode() != 200)
                return null;
            return connection.getHeaderField("authorization");
        }catch(Exception e){System.err.println(e.toString());}
        return null;
    }

    public List<String> GetImageList(String authorization, int page, String field,String mode){
        List<String> list = new ArrayList<String>();
        HttpsURLConnection connection = null;
        System.out.println("select "+mode);
        try{
            switch(mode){
                case "0":
                    connection = (HttpsURLConnection)new URL("https://pix.ipv4.host/illustrations?illustType=illust&searchType=original&maxSanityLevel=3&page="+page+"&keyword=="+URLEncoder.encode(field,"UTF-8")+"&pageSize=30").openConnection(); break;
                case "1":
                    connection = (HttpsURLConnection)new URL("https://pix.ipv4.host/artists/"+field+"/illusts/illust?page="+page+"&pageSize=30&maxSanityLevel=3").openConnection();break;
                case "2":
                    connection = (HttpsURLConnection)new URL("https://pix.ipv4.host/ranks?page="+page+"&date="+field+"&mode=day&pageSize=30").openConnection();
            }
            System.out.println("Connect to:"+connection.getURL());
            connection.setRequestProperty("authorization",authorization);
            connection.setRequestProperty("referer","https://sharemoe.net/");
            connection.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            if(connection.getResponseCode() != 200){
                System.err.println("可能因连接不上或已经达到页数末尾,决定停止连接.");return null;
            }
            StringBuffer buffer = new StringBuffer();
            String temp;
            while((temp = br.readLine()) != null)
                buffer.append(temp);
            System.out.println(buffer);
            Pattern pattern = Pattern.compile("imageUrls\":(.*?)}");
            Matcher matcher = pattern.matcher(buffer.toString());
            while(matcher.find())
                list.add(matcher.group().split("\"original\":\"")[1].split("\"")[0]);
            System.out.println(list);
            return list;
        }catch(Exception e){System.err.println(e.toString());}
        return null;
    }

    public void DownloadImage(String url,String authorization){
        try{
            String fileName = url.split("/")[url.split("/").length-1];
            url = url.replace("i.pximg.net","o.acgpic.net")+"?Authorization="+authorization;
            System.out.println("Connect ImageURL:"+url);
            HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
            connection.setRequestProperty("Referer","https://sharemoe.net/");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);
            System.out.println("下载图片:"+fileName+"...");
            int j = 0;
            while((j = inputStream.read()) != -1)
                outputStream.write(j);
            outputStream.close();
            System.out.println("下载完成");
        }catch(Exception e){System.err.println(e.toString()+"\n"+"下载失败");}
    }

    public static void main(String[] args) {
        try{
            Pixivic pixivic = new Pixivic();
            String a[] = pixivic.GetVerifiCode();
            pixivic.DownloadVerifiImage(a[1]);
            Scanner scanner = new Scanner(System.in);
            System.out.println("检查你的图片,上面写着什么.");
            String b = scanner.nextLine();
            pixivic.SendOPTIONS(a[0],b);
            String authorization = pixivic.HTTPSRequest(a[0],b);
            if(authorization == null)
                return;
            System.out.println("获取登入验证authorization:"+authorization);
            System.out.println("选择搜索模式:(0)查找关键词(1)通过ID查找画师(2)随机日榜");
            String mode = scanner.nextLine();
            while(!mode.equals("0") && !mode.equals("1") && !mode.equals("2")){
                System.out.println("确保你输入正确.");
                mode = scanner.nextLine();
            }
            switch(mode){
                case "0":
                    System.out.println("搜索一个关键词#例子(输入关键词:风景)");break;
                case "1":System.out.println("搜索一个画师ID #例子(输入画师ID:87186298");break;
                case "2":System.out.println("随机日榜 #例子(输入日期：2021-08-27");break;
            }
            String field = scanner.nextLine();
            int page = 1;
            while(true){
                System.out.println("[Webcrawler]:内容第"+page+"页");
                List<String> list = pixivic.GetImageList(authorization,page,field,mode);
                if(list.isEmpty())
                    return;
                for(String url : list){
                    pixivic.DownloadImage(url,authorization);Thread.sleep(600);
                }
                page+=1;
            }
        }catch(Exception e){System.err.println(e.toString());}
    }
}