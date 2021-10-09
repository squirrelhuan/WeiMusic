package com.demomaster.weimusic.player;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;

import cn.demomaster.qdlogger_library.QDLogger;

public class MediaPlayerProxy extends Thread {

    final static private String LOCAL_IP_ADDRESS = "127.0.0.1";
    private static int local_ip_port = 9090;

    final static private int HTTP_PORT = 80;
    //public static List<String> bufferingMusicUrlList = new ArrayList<>();//正在缓存的网络音乐地址
    private OnCaChedProgressUpdateListener mOnCaChedProgressUpdateListener;


    private ServerSocket localServer = null;
    public SocketAddress remoteAddress;
    int socketTimeoutTime = 5000;
    public String writeFileName = "";
    String requestUrl;//原始请求url
    boolean writeFile = true;//是否缓存播放文件
    String musicKey = "";//音乐对象的key
    public int currPlayDegree = 0;//当前音乐播放进度
    public boolean proxyFail = false;//代理播放失败了

    private long cachedFileLength = 0;//已缓存的文件长度
    private long fileTotalLength = 0;//要缓存的文件总长度
    public int currMusicCachedProgress = 0;//当前的音乐缓冲值（seekbar上的缓冲值）

    public MediaPlayerProxy(String url, String writeFileName, boolean writeFile) {
        this.requestUrl = url;
        this.writeFile = writeFile;
        this.writeFileName = writeFileName;
        try {
            if (localServer == null || localServer.isClosed()) {
                //创建本地socket服务器，用来监听mediaplayer请求和给mediaplayer提供数据
                localServer = new ServerSocket();
                localServer.setReuseAddress(true);
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(LOCAL_IP_ADDRESS), local_ip_port);
                localServer.bind(socketAddress);
            }
        } catch (Exception e) {
            QDLogger.e(e);
            try {
                local_ip_port--;
                localServer = new ServerSocket(local_ip_port, 0, InetAddress.getByName(LOCAL_IP_ADDRESS));
                localServer.setReuseAddress(true);
            } catch (Exception e2) {
                QDLogger.e(e2);
            }
        }
    }

    private String requestPrefix = "";//请求前缀，ip+端口

    /**
     * 把网络URL转为本地URL，127.0.0.1替换网络域名,且设置远程的socket连接地址
     *
     * @return 代理URL
     */
    public String getProxyUrl() {
        String localProxyUrl = "";
        final URI originalURI = URI.create(requestUrl);
        final String remoteHost = originalURI.getHost();
        if (!TextUtils.isEmpty(remoteHost)) {
            if (originalURI.getPort() != -1) {//URL带Port
                requestPrefix = remoteHost + ":" + originalURI.getPort();
                localProxyUrl = requestUrl.replaceFirst(requestPrefix, LOCAL_IP_ADDRESS + ":" + local_ip_port);
            } else {//URL不带Port
                requestPrefix = remoteHost;
                localProxyUrl = requestUrl.replaceFirst(requestPrefix, LOCAL_IP_ADDRESS + ":" + local_ip_port);
            }
        }
        return localProxyUrl;
    }

    public byte[] convert(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            int a = data[i] & 0xff;
            data[i] = (byte) a;
        }
        return data;
    }

    //获得真实的socket请求信息
    public void getRequestInfo(Socket localSocket) throws Exception {
        QDLogger.i("请求信息111:" + Arrays.toString(requestUrl.getBytes()));
        byte[] data = new byte[1024 * 4];
        try {
            int readLenth;
            String infoStr = "";//保存MediaPlayer的真实HTTP请求
            InputStream in_localSocket = localSocket.getInputStream();
            while ((readLenth = in_localSocket.read(data, 0, data.length)) != -1) {
                //sendRemoteRequest(local_request);
                byte[] data1 = new byte[readLenth];
                System.arraycopy(data, 0, data1, 0, data1.length);
                infoStr = new String(data1, 0, data1.length);
                data1 = convert(data1);
                //QDLogger.i("请求信息:" + Arrays.toString(data1));
                //QDLogger.i("请求信息2:" + Arrays.toString("GET".getBytes()));
               // QDLogger.i("请求信息3:" + Arrays.toString("\r\n\r\n".getBytes()));
                QDLogger.i("infoStr:" + infoStr);
                //创建远程socket用来请求网络数据
                remoteSocket.getOutputStream().write(data1);
                remoteSocket.getOutputStream().flush();
                QDLogger.i("flush");
                break;
               /* if (infoStr.contains("GET") && infoStr.contains("\r\n\r\n")) {
                    //把request中的本地ip改为远程ip
                    infoStr = infoStr.replace(LOCAL_IP_ADDRESS + ":" + local_ip_port, requestPrefix);
                    //如果用户拖动了进度条，因为拖动了滚动条还有Range则表示本地歌曲还未缓存完，不再保存
                    if (infoStr.contains("Range")) {
                        QDLogger.d("=Range=");
                        writeFile = false;
                    }
                    break;
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Socket remoteSocket;

    public void initRemoteSocket() throws IOException {
        //保证创建了远程socket地址再进行下一步
        final URI originalURI = URI.create(requestUrl);
        final String remoteHost = originalURI.getHost();
        if (!TextUtils.isEmpty(remoteHost)) {
            if (originalURI.getPort() != -1) {//URL带Port
                remoteAddress = new InetSocketAddress(remoteHost, originalURI.getPort());
            } else {//URL不带Port
                remoteAddress = new InetSocketAddress(remoteHost, HTTP_PORT);//使用80端口
            }
        }
        if (remoteSocket == null || remoteSocket.isClosed()) {
            remoteSocket = new Socket();
        }

        if (!remoteSocket.isConnected()) {
            remoteSocket.connect(remoteAddress, socketTimeoutTime);
            QDLogger.i("初始化remoteSocket connect");
        }
    }

    //处理真实请求信息, 把网络服务器的反馈发到MediaPlayer，网络服务器->代理服务器->MediaPlayer
    public void processTrueRequestInfo(Socket localSocket) {
        QDLogger.i("processTrueRequestInfo");
        //如果要写入本地文件的实例声明
        FileOutputStream fileOutputStream = null;
        File theFile = null;
        try {
            //获取音乐网络数据
            InputStream in_remoteSocket = remoteSocket.getInputStream();
            if (in_remoteSocket == null) return;

            OutputStream out_localSocket = localSocket.getOutputStream();
            if (out_localSocket == null) return;

            //如果要写入文件，配置相关实例
            if (writeFile) {
                File dirs = new File(Environment.getExternalStorageDirectory() + File.separator + "clearlee_music");
                dirs.mkdirs();
                theFile = new File(dirs + File.separator + writeFileName + ".m4a");
                QDLogger.i(theFile.getAbsoluteFile() + " exists:" + theFile.exists());
                fileOutputStream = new FileOutputStream(theFile);
            }

            try {
                int readLenth;
                byte[] remote_reply = new byte[4096];
                boolean firstData = true;//是否循环中第一次获得数据

                //当从远程还能取到数据且播放器还没切换另一首网络音乐
                while ((readLenth = in_remoteSocket.read(remote_reply, 0, remote_reply.length)) != -1) {
                    QDLogger.i("拉取到远程数据:" + new String(remote_reply, "utf-8"));
                    //首先从数据中获得文件总长度
                    /*try {
                        if (firstData) {
                            firstData = false;
                            String str = new String(remote_reply, "utf-8");
                            Pattern pattern = Pattern.compile("Content-Length:\\s*(\\d+)");
                            Matcher matcher = pattern.matcher(str);
                            if (matcher.find()) {
                                //获取数据的大小
                                fileTotalLength = Long.parseLong(matcher.group(1));
                            }
                        }
                    } catch (Exception e) {
                        QDLogger.e(e);
                    }*/

                    //把远程sokcet拿到的数据用本地socket写到mediaplayer中播放
                    try {
                        out_localSocket.write(remote_reply, 0, readLenth);
                        out_localSocket.flush();
                        QDLogger.i("向请求方写入数据");
                    } catch (Exception e) {
                        QDLogger.e(e);
                    }

                    //计算当前播放时，其在seekbar上的缓冲值,并刷新进度条
                    try {
                        cachedFileLength += readLenth;
                        if (fileTotalLength > 0) {
                            currMusicCachedProgress = (int) (Common.div(cachedFileLength, fileTotalLength, 5) * 100);
                            if (mOnCaChedProgressUpdateListener != null && currMusicCachedProgress <= 100) {
                                mOnCaChedProgressUpdateListener.updateCachedProgress(currMusicCachedProgress);
                            }
                        }
                    } catch (Exception e) {
                        QDLogger.e(e);
                    }

                    //如果需要缓存数据到本地，就缓存到本地
                    if (writeFile) {
                        try {
                            if (fileOutputStream != null) {
                                fileOutputStream.write(remote_reply);
                                fileOutputStream.flush();
                            }
                        } catch (Exception e) {
                            QDLogger.e(e);
                        }
                    }
                }

                QDLogger.e("结束");
                //如果是因为切换音乐跳出循环的，当前音乐播放进度，小于 seekbar最大值的1/4,就把当前音乐缓存在本地的数据清除了
                if (currPlayDegree < 25) {
                    // bufferingMusicUrlList.remove(remotUrl);
                    if (theFile != null) {
                        Common.deleteFile(theFile.getPath());
                    }
                }
            } catch (Exception e) {
                QDLogger.e(e);
                if (theFile != null) {
                    Common.deleteFile(theFile.getPath());
                }
                // bufferingMusicUrlList.remove(remotUrl);
            } finally {
                in_remoteSocket.close();
                out_localSocket.close();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    //音频文件缓存完后处理
                    if (theFile != null && Common.checkFileExist(theFile.getPath())) {
                        conver2RightAudioFile(theFile);
                        if (musicControlInterface != null) {
                            musicControlInterface.updateBufferFinishMusicPath(musicKey, theFile.getPath());
                            //bufferingMusicUrlList.remove(remotUrl);
                        }
                    }
                }
                localSocket.close();
                remoteSocket.close();
            }

        } catch (Exception e) {
            QDLogger.e(e);
            if (theFile != null) {
                Common.deleteFile(theFile.getPath());
            }
            //bufferingMusicUrlList.remove(remotUrl);
        }
    }

    public interface MusicControlInterface {
        void updateBufferFinishMusicPath(String musicKey, String localPath);
    }

    public static MusicControlInterface musicControlInterface;

    public interface OnCaChedProgressUpdateListener {
        void updateCachedProgress(int progress);
    }

    public void setOnCaChedProgressUpdateListener(OnCaChedProgressUpdateListener listener) {
        this.mOnCaChedProgressUpdateListener = listener;
    }

    /**
     * 启动代理服务器
     */
    public void startProxy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //监听MediaPlayer的请求，MediaPlayer->代理服务器
                    QDLogger.i("开启代理");
                    Socket localSocket = localServer.accept();
                    QDLogger.i("接收到网络请求");
                    initRemoteSocket();
                    //获得请求信息
                    getRequestInfo(localSocket);
                    //处理真实请求信息
                    processTrueRequestInfo(localSocket);
                } catch (Exception e) {
                    QDLogger.e(e);
                    proxyFail = true;
                } finally {
                    //最后释放本地代理serversocket
                    if (localServer != null) {
                        try {
                            localServer.close();
                            localServer = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    //转换为正确的音频文件
    public void conver2RightAudioFile(File file) {
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = new FileInputStream(file);
            int read = 0;
            while (read > -1) {
                int newRead = inputStream.read();
                if (read == 0 && newRead == 0) {
                    byte[] bs = new byte[inputStream.available() + 2];
                    inputStream.read(bs, 2, bs.length - 2);
                    fos = new FileOutputStream(file);
                    fos.write(bs);
                    fos.flush();
                    break;
                }
                read = newRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (fos != null) fos.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void run() {
        super.run();

    }
}