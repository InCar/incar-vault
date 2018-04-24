package com.incarcloud.auxiliary;

/**
 * 辅助用途静态方法
 * 所有方法皆为线程安全
 */
public class Helper {
    // 格式化异常的辅助方法,异常不会过于频繁的抛出,所以共享了一个StringBuilder对象
    private static final StringBuilder s_sbStackTrace = new StringBuilder();

    /**
     * 把异常的栈信息转换为字符串形式，以方便日志输出。
     * 内部共享了一个StringBuilder对象，因此不宜过于频繁的调用。
     * @param ex 异常
     * @return 栈信息
     */
    public static String printStackTrace(Throwable ex){
        synchronized (s_sbStackTrace) {
            Throwable exx = ex;
            while (exx != null) {
                s_sbStackTrace.append("\n    ");
                s_sbStackTrace.append(exx.toString());
                for (StackTraceElement e : exx.getStackTrace()) {
                    s_sbStackTrace.append("\n        ");
                    s_sbStackTrace.append(e.toString().trim());
                }
                exx = exx.getCause();
            }
            String result = s_sbStackTrace.toString();
            // clear for next using
            s_sbStackTrace.delete(0, s_sbStackTrace.length());
            return result;
        }
    }
}
