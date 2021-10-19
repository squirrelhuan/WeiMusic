package cn.demomaster.sporthealth_library;

public class AMapUtils {

    public static float calculateLineDistance(RecordPoint var0, RecordPoint var1) {
        if (var0 != null && var1 != null) {
            try {
                double var2 = var0.longitude;
                double var4 = var0.latitude;
                double var6 = var1.longitude;
                double var8 = var1.latitude;
                var2 *= 0.01745329251994329D;
                var4 *= 0.01745329251994329D;
                var6 *= 0.01745329251994329D;
                var8 *= 0.01745329251994329D;
                double var10 = Math.sin(var2);
                double var12 = Math.sin(var4);
                double var14 = Math.cos(var2);
                double var16 = Math.cos(var4);
                double var18 = Math.sin(var6);
                double var20 = Math.sin(var8);
                double var22 = Math.cos(var6);
                double var24 = Math.cos(var8);
                double[] var28 = new double[3];
                double[] var29 = new double[3];
                var28[0] = var16 * var14;
                var28[1] = var16 * var10;
                var28[2] = var12;
                var29[0] = var24 * var22;
                var29[1] = var24 * var18;
                var29[2] = var20;
                return (float)(Math.asin(Math.sqrt((var28[0] - var29[0]) * (var28[0] - var29[0]) + (var28[1] - var29[1]) * (var28[1] - var29[1]) + (var28[2] - var29[2]) * (var28[2] - var29[2])) / 2.0D) * 1.27420015798544E7D);
            } catch (Throwable var26) {
                var26.printStackTrace();
                return 0.0F;
            }
        } else {
            try {
                throw new Exception("非法坐标值");
            } catch (Exception var27) {
                var27.printStackTrace();
                return 0.0F;
            }
        }
    }
}
