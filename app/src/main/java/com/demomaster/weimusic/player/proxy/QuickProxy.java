package com.demomaster.weimusic.player.proxy;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.AudioStation.DOWNLOAD_SUCCESS;
import static com.demomaster.weimusic.constant.AudioStation.QUEUE_CHANGED;

public class QuickProxy extends Thread {

    final static private String PROXY_SERVER_IP = "127.0.0.1";
    private static int PROXY_SERVER_PORT = 9090;
    ServerSocket proxyServerSocket = null;
    String requestUrl;//原始请求url
    String savePath;
    Context context;
    public QuickProxy(Context context, String url,String savePath) {
        this.context = context.getApplicationContext();
        this.requestUrl = url;
        this.savePath = savePath;


        String dataPath = requestUrl;
        if(!TextUtils.isEmpty(requestUrl)&&requestUrl.startsWith("https:")){
            dataPath= requestUrl.replaceFirst("https:","http:");
        }
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(context,Uri.parse(dataPath));
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // 播放时长单位为毫秒
            QDLogger.println( "title:" + title+",album:" + album+",artist:" + artist+",duration:"+duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pre();
    }

    public void pre() {
        try {
            if (proxyServerSocket != null ) {
                try {
                    if(!proxyServerSocket.isClosed()) {
                        proxyServerSocket.close();
                    }
                } catch (Exception e1) {
                    QDLogger.e(e1);
                }finally {
                    proxyServerSocket = null;
                }
            }
                //创建本地socket服务器，用来监听mediaplayer请求和给mediaplayer提供数据
                proxyServerSocket = new ServerSocket();
                //proxySocket.setReuseAddress(true);
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(PROXY_SERVER_IP), PROXY_SERVER_PORT);
                proxyServerSocket.bind(socketAddress);
                proxyServerSocket.setReuseAddress(true);
                QDLogger.i("代理端口:" + PROXY_SERVER_PORT);
        } catch (Exception e) {
            QDLogger.e(e);
            if (proxyServerSocket != null ) {
                try {
                    if(!proxyServerSocket.isClosed()) {
                        proxyServerSocket.close();
                    }
                } catch (Exception e1) {
                    QDLogger.e(e1);
                }finally {
                    proxyServerSocket = null;
                }
            }
            PROXY_SERVER_PORT--;
            try {
                proxyServerSocket = new ServerSocket(PROXY_SERVER_PORT, 0, InetAddress.getByName(PROXY_SERVER_IP));
                proxyServerSocket.setReuseAddress(true);
                QDLogger.i("代理端口:" + PROXY_SERVER_PORT);
            } catch (Exception e2) {
                QDLogger.e(e2);
                pre();
            }
        }

    }

    final static private int HTTP_PORT = 80;
    int socketTimeoutTime = 15000;

    @Override
    public void run() {
        OutputStream clientOutput = null;
        InputStream clientInput = null;
        InputStream proxyInput = null;
        OutputStream proxyOutput = null;
        Socket proxySocket = null;
        Socket clientSocket = null;
        //开启代理serversocket
        try {
            final URI originalURI = URI.create(requestUrl);
            final String remoteHost = originalURI.getHost();
            InetSocketAddress remoteAddress = null;
            if (!TextUtils.isEmpty(remoteHost)) {
                if (originalURI.getPort() != -1) {//URL带Port
                    remoteAddress = new InetSocketAddress(remoteHost, originalURI.getPort());
                } else {//URL不带Port
                    remoteAddress = new InetSocketAddress(remoteHost, HTTP_PORT);//使用80端口
                }
            }

            clientSocket = proxyServerSocket.accept();
            clientSocket.setSoTimeout(socketTimeoutTime);
            clientInput = clientSocket.getInputStream();
            clientOutput = clientSocket.getOutputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientInput));
            String line;
            byte[] data = new byte[1024];
            StringBuilder headStr = new StringBuilder();
            StringBuilder headStr2 = new StringBuilder();
            double d = Math.random();
            String host = null;
            //读取HTTP请求头，并拿到HOST请求头和method
            while ((line = bufferedReader.readLine()) != null) {
                QDLogger.i("请求信息("+d+"):" + line+"-"+Arrays.toString(line.getBytes()));
                headStr.append(line + "\r\n");
                if (!TextUtils.isEmpty(line)) {
                    String[] temp = line.split(" ");
                    if (line.contains("Host")) {
                        host = temp[1];
                        headStr2.append(temp[0] + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "\r\n");
                    } else {
                        headStr2.append(line + "\r\n");
                    }
                } else {
                    headStr2.append(line + "\r\n");
                    break;
                }
            }

            QDLogger.i("代理host("+d+"):" + host);
            QDLogger.i("生成请求数据:" + headStr2);
            //连接到目标服务器
            proxySocket = new Socket();
            proxySocket.connect(new InetSocketAddress(remoteAddress.getHostName(), remoteAddress.getPort()), socketTimeoutTime);
            QDLogger.i("连接到目标服务器:" + remoteAddress);
            proxyOutput = proxySocket.getOutputStream();
            proxyOutput.write(headStr2.toString().getBytes());
            proxyOutput.flush();

            int readLenth;
            long fileTotalLength=-1;
            String infoStr = "";//保存MediaPlayer的真实HTTP请求
            proxyInput = proxySocket.getInputStream();
            boolean readHeader =true;
            File file = null;
            while ((readLenth = proxyInput.read(data, 0, data.length)) != -1) {
                byte[] data1 = new byte[readLenth];
                System.arraycopy(data, 0, data1, 0, data1.length);
                infoStr = new String(data1);
                if(readHeader){
                    //首先从数据中获得文件总长度
                    try {
                            Pattern pattern = Pattern.compile("Content-Length:\\s*(\\d+)");
                            Matcher matcher = pattern.matcher(infoStr);
                            if (matcher.find()) {
                                //获取数据的大小
                                fileTotalLength = Long.parseLong(matcher.group(1));
                            }
                            if(fileTotalLength>0) {
                                Pattern pattern2 = Pattern.compile("Content-Type:(.*);");
                                String contentType = null;
                                //获取文件类型
                                Matcher matcher2 = pattern2.matcher(infoStr);
                                if (matcher2.find()) {
                                    contentType = matcher2.group(1);
                                }
                                pattern2 = Pattern.compile("Content-Type:(.*)\r");
                                matcher2 = pattern2.matcher(infoStr);
                                if (matcher2.find()) {
                                    contentType = matcher2.group(1);
                                }
                                String resourceName = null;
                                String resourceFileType = null;
                                pattern2 = Pattern.compile("x-nos-object-name:(.*)\r");
                                matcher2 = pattern2.matcher(infoStr);
                                if (matcher2.find()) {
                                    resourceName = matcher2.group(1);
                                    if(!TextUtils.isEmpty(resourceName)){
                                       String[] arr = resourceName.split("\\.");
                                       if(arr!=null&&arr.length>0){
                                           resourceFileType = arr[arr.length-1];
                                       }
                                    }
                                }
                                if(TextUtils.isEmpty(resourceFileType)){
                                    if(requestUrl.contains(".")){
                                        String[] arr = requestUrl.split("\\.");
                                        if(arr!=null&&arr.length>0){
                                            String str = arr[arr.length-1];
                                            if(!TextUtils.isEmpty(str)&&str.trim().length()<6){
                                                resourceFileType = str.trim();
                                            }
                                        }
                                    }
                                }
                                QDLogger.i("content类型:" +contentType);
                                QDLogger.i("资源名称:" +resourceName);
                                QDLogger.i("资源文件类型:" +resourceFileType);
                                QDLogger.i("头信息:" + infoStr);
                                //QDLogger.i("头信息2:" + Arrays.toString(infoStr.getBytes()));
                                QDLogger.e("文件长度:" + fileTotalLength);
                                if(file==null) {
                                    file = new File(Environment.getExternalStorageDirectory(), savePath + "/" + System.currentTimeMillis() + "." + resourceFileType);
                                    QDFileUtil.createFile(file);
                                }
                            }
                    } catch (Exception e) {
                        QDLogger.e(e);
                    }
                }
                int bodyStartIndex=0;//有效内容起始索引
                String endTag= "\r\n\r\n";
                if(infoStr.contains(endTag)){
                    readHeader = false;
                    bodyStartIndex = infoStr.indexOf(endTag)+4;
                    /*QDLogger.i("bodyStartIndex:" + bodyStartIndex);
                    QDLogger.i("头信息1111111:" + infoStr);
                    QDLogger.i("头信息2222222:" + infoStr.substring(bodyStartIndex,infoStr.length()-1));*/
                }

                if(!readHeader&&file!=null&&file.exists()) {
                    byte[] fileData = new byte[data1.length-bodyStartIndex];
                    System.arraycopy(data1,bodyStartIndex,fileData,0,fileData.length);
                    FileOutputStream fileOutputStream = new FileOutputStream(file,true);
                    if (fileOutputStream != null&&file.length()<fileTotalLength) {
                      /*  if((file.length()+fileData.length)>fileTotalLength){
                            fileOutputStream.write(fileData,0, (int) (fileTotalLength-file.length()));
                        }else {*/
                            fileOutputStream.write(fileData);
                        //}
                        fileOutputStream.flush();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }
                QDLogger.d("文件存储长度:" + file.length());
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientOutput.write(data1);
                    clientOutput.flush();
                }
                if(file!=null&&file.length()==fileTotalLength){
                    QDLogger.d("文件保存完成:"+file.getAbsolutePath());
                    EventBus.getDefault().post(new EventMessage(DOWNLOAD_SUCCESS.value()));
                    QDFileUtil.updateMediaFile(context, file.getAbsolutePath(), new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            EventBus.getDefault().post(new EventMessage(QUEUE_CHANGED.value()));
                        }
                    });
                    break;
                }
            }

            QDLogger.i("代理结束");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (proxySocket != null) {
                try {
                    proxySocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (clientOutput != null) {
                try {
                    clientOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (proxyServerSocket != null) {
            try {
                    proxyServerSocket.close();
                    proxyServerSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        }
    }

    private String requestPrefix = "";//请求前缀，ip+端口
    public String getProxyUrl() {
        String localProxyUrl = "";
        final URI originalURI = URI.create(requestUrl);
        final String remoteHost = originalURI.getHost();
        if (!TextUtils.isEmpty(remoteHost)) {
            if (originalURI.getPort() != -1) {//URL带Port
                requestPrefix = remoteHost + ":" + originalURI.getPort();
            } else {//URL不带Port
                requestPrefix = remoteHost;
            }
            localProxyUrl = requestUrl.replaceFirst(requestPrefix, PROXY_SERVER_IP + ":" + PROXY_SERVER_PORT);
        }
        if(!TextUtils.isEmpty(localProxyUrl)&&localProxyUrl.startsWith("https:")){
            localProxyUrl= localProxyUrl.replaceFirst("https:","http:");
        }
        return localProxyUrl;
    }
}
